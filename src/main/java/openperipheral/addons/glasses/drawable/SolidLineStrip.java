package openperipheral.addons.glasses.drawable;

import openmods.structured.StructureField;
import openperipheral.addons.glasses.utils.IPointListBuilder;
import openperipheral.addons.glasses.utils.Point2d;
import openperipheral.addons.glasses.utils.PointListBuilder;
import openperipheral.addons.glasses.utils.RenderState;
import openperipheral.api.adapter.AdapterSourceName;
import openperipheral.api.adapter.Property;
import openperipheral.api.adapter.method.ScriptObject;

@ScriptObject
@AdapterSourceName("glasses_line_strip")
public class SolidLineStrip extends LineStrip<Point2d> {

	@Property
	@StructureField
	public int color = 0xFFFFFF;

	@Property
	@StructureField
	public float opacity = 1.0f;

	public SolidLineStrip() {}

	public SolidLineStrip(int color, float opacity, Point2d... points) {
		super(points);

		this.color = color;
		this.opacity = opacity;
	}

	@Override
	protected void drawContents(RenderState renderState, float partialTicks) {
		renderState.setColor(color, opacity);
		super.drawContents(renderState, partialTicks);
	}

	@Override
	protected IPointListBuilder<Point2d> createBuilder() {
		return new PointListBuilder();
	}

	@Override
	protected DrawableType getTypeEnum() {
		return DrawableType.LINE_STRIP;
	}

}
