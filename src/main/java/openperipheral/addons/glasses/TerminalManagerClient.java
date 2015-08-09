package openperipheral.addons.glasses;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import openmods.geometry.Box2d;
import openperipheral.addons.glasses.GlassesEvent.GlassesChangeBackgroundEvent;
import openperipheral.addons.glasses.GlassesEvent.GlassesSetDragParamsEvent;
import openperipheral.addons.glasses.GlassesEvent.GlassesSetGuiVisibilityEvent;
import openperipheral.addons.glasses.GlassesEvent.GlassesSetKeyRepeatEvent;
import openperipheral.addons.glasses.GlassesEvent.GlassesStopCaptureEvent;
import openperipheral.addons.glasses.TerminalEvent.TerminalClearEvent;
import openperipheral.addons.glasses.TerminalEvent.TerminalDataEvent;
import openperipheral.addons.glasses.drawable.Drawable;
import openperipheral.addons.glasses.utils.RenderState;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class TerminalManagerClient {

	public static class DrawableHitInfo {
		public final int id;
		public final boolean isPrivate;
		public final float dx;
		public final float dy;
		public final int z;

		public DrawableHitInfo(int id, boolean isPrivate, float dx, float dy, int z) {
			this.id = id;
			this.isPrivate = isPrivate;
			this.dx = dx;
			this.dy = dy;
			this.z = z;
		}
	}

	public static final TerminalManagerClient instance = new TerminalManagerClient();

	private TerminalManagerClient() {}

	private final Table<Long, String, SurfaceClient> surfaces = HashBasedTable.create();

	private void tryDrawSurface(long guid, String player, float partialTicks, ScaledResolution resolution) {
		SurfaceClient surface = surfaces.get(guid, player);
		if (surface != null) {
			GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_ENABLE_BIT);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			final RenderState renderState = new RenderState();
			renderState.forceKnownState();

			for (Drawable drawable : surface.getSortedDrawables())
				if (drawable.shouldRender()) drawable.draw(resolution, renderState, partialTicks);
			GL11.glPopAttrib();
		}
	}

	private static String getSurfaceName(boolean isPrivate) {
		return isPrivate? TerminalUtils.PRIVATE_MARKER : TerminalUtils.GLOBAL_MARKER;
	}

	public class ForgeBusListener {

		@SubscribeEvent
		public void onRenderGameOverlay(RenderGameOverlayEvent.Pre evt) {
			if (evt.type == ElementType.ALL) {
				GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

				if (gui instanceof GuiCapture) {
					final GuiCapture capture = (GuiCapture)gui;
					// this must be here, since there are some elements (like food bar) that are overriden every tick
					capture.forceGuiElementsState();
				}
			}
		}

		@SubscribeEvent
		public void onRenderGameOverlay(RenderGameOverlayEvent.Post evt) {
			if (evt.type == ElementType.HELMET) {
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				Long guid = TerminalUtils.tryGetTerminalGuid(player);
				if (guid != null) {
					tryDrawSurface(guid, TerminalUtils.GLOBAL_MARKER, evt.partialTicks, evt.resolution);
					tryDrawSurface(guid, TerminalUtils.PRIVATE_MARKER, evt.partialTicks, evt.resolution);
				}
			}
		}

		@SubscribeEvent
		public void onTerminalData(TerminalDataEvent evt) {
			String surfaceName = getSurfaceName(evt.isPrivate);
			SurfaceClient surface = surfaces.get(evt.terminalId, surfaceName);

			if (surface == null) {
				surface = new SurfaceClient(evt.terminalId, evt.isPrivate);
				surfaces.put(evt.terminalId, surfaceName, surface);
			}

			surface.interpretCommandList(evt.commands);
		}

		@SubscribeEvent
		public void onTerminalClear(TerminalClearEvent evt) {
			String surfaceName = getSurfaceName(evt.isPrivate);
			surfaces.remove(evt.terminalId, surfaceName);
		}

		@SubscribeEvent
		public void onBackgroundChange(GlassesChangeBackgroundEvent evt) {
			GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

			if (gui instanceof GuiCapture) {
				final GuiCapture capture = (GuiCapture)gui;
				long guid = capture.getGuid();
				if (guid == evt.guid) capture.setBackground(evt.backgroundColor);
			}
		}

		@SubscribeEvent
		public void onKeyRepeatSet(GlassesSetKeyRepeatEvent evt) {
			GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

			if (gui instanceof GuiCapture) {
				final GuiCapture capture = (GuiCapture)gui;
				long guid = capture.getGuid();
				if (guid == evt.guid) capture.setKeyRepeat(evt.repeat);
			}
		}

		@SubscribeEvent
		public void onDragParamsSet(GlassesSetDragParamsEvent evt) {
			GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

			if (gui instanceof GuiCapture) {
				final GuiCapture capture = (GuiCapture)gui;
				long guid = capture.getGuid();
				if (guid == evt.guid) capture.setDragParameters(evt.threshold, evt.period);
			}
		}

		@SubscribeEvent
		public void onGuiVisibilitySet(GlassesSetGuiVisibilityEvent evt) {
			GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

			if (gui instanceof GuiCapture) {
				final GuiCapture capture = (GuiCapture)gui;
				long guid = capture.getGuid();
				if (guid == evt.guid) capture.updateGuiElementsState(evt.visibility);
			}
		}

		@SubscribeEvent
		public void onCaptureForce(GlassesStopCaptureEvent evt) {
			GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

			if (gui instanceof GuiCapture) {
				long guid = ((GuiCapture)gui).getGuid();
				if (guid == evt.guid) FMLCommonHandler.instance().showGuiScreen(null);
			}
		}
	}

	public Object createForgeBusListener() {
		return new ForgeBusListener();
	}

	public class FmlBusListener {

		@SubscribeEvent
		public void onDisconnect(ClientDisconnectionFromServerEvent evt) {
			surfaces.clear();
		}

	}

	public Object createFmlBusListener() {
		return new FmlBusListener();
	}

	public DrawableHitInfo findDrawableHit(long guid, ScaledResolution resolution, float x, float y) {
		DrawableHitInfo result = findDrawableHit(guid, resolution, x, y, false);
		if (result != null) return result;

		return findDrawableHit(guid, resolution, x, y, true);
	}

	private DrawableHitInfo findDrawableHit(long guid, ScaledResolution resolution, float x, float y, boolean isPrivate) {
		final String surfaceName = getSurfaceName(isPrivate);
		SurfaceClient surface = surfaces.get(guid, surfaceName);

		if (surface == null) return null;

		for (Drawable d : Lists.reverse(surface.getSortedDrawables())) {
			if (!d.isClickable()) continue;
			final float scaledX = d.getX(resolution);
			final float scaledY = d.getY(resolution);

			final float dx = x - scaledX;
			final float dy = y - scaledY;

			final Box2d bb = d.getBoundingBox();
			if (0 <= dx && 0 <= dy && dx < bb.width && dy < bb.height) { return new DrawableHitInfo(d.getId(), isPrivate, dx, dy, d.z); }
		}

		return null;
	}
}