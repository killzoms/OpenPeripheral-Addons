package openperipheral.addons.glasses.drawable;

import net.minecraft.client.renderer.Tessellator;
import openperipheral.api.AdapterSourceName;
import openperipheral.api.CallbackProperty;
import openperipheral.api.LuaObject;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@LuaObject
@AdapterSourceName("glasses_box")
public class SolidBox extends Drawable {
	@CallbackProperty
	public short width;

	@CallbackProperty
	public short height;

	@CallbackProperty
	public int color;

	@CallbackProperty
	public float opacity;

	SolidBox() {}

	public SolidBox(short x, short y, short width, short height, int color, float opacity) {
		super(x, y);
		this.width = width;
		this.height = height;
		this.color = color;
		this.opacity = opacity;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void drawContents(float partialTicks) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_I(color, (int)(opacity * 255));

		tessellator.addVertex(0, 0, 0);
		tessellator.addVertex(0, height, 0);

		tessellator.addVertex(width, height, 0);
		tessellator.addVertex(width, 0, 0);

		tessellator.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	public Type getTypeEnum() {
		return Type.BOX;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean isVisible() {
		return opacity > 0;
	}
}