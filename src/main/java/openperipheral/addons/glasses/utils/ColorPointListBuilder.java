package openperipheral.addons.glasses.utils;

import com.google.common.collect.Lists;
import java.util.List;
import openmods.geometry.BoundingBoxBuilder;
import openmods.geometry.Box2d;
import org.lwjgl.opengl.GL11;

public class ColorPointListBuilder implements IPointListBuilder<ColorPoint2d> {

	private static class PointListImpl extends PointList<ColorPoint2d> {

		public PointListImpl(List<ColorPoint2d> points) {
			super(points);
		}

		@Override
		protected void drawPoint(RenderState renderState, ColorPoint2d p) {
			renderState.setColor(p.rgb, p.opacity);
			GL11.glVertex2f(p.x, p.y);
		}

	}

	private final List<ColorPoint2d> points = Lists.newArrayList();

	private final BoundingBoxBuilder bbBuilder = BoundingBoxBuilder.create();

	@Override
	public void add(ColorPoint2d point) {
		bbBuilder.addPoint(point.x, point.y);
		points.add(point);
	}

	private static ColorPoint2d toBoundingBox(Box2d bb, ColorPoint2d point) {
		return new ColorPoint2d(point.x - bb.left, point.y - bb.top, point.rgb, point.opacity);
	}

	@Override
	public IPointList<ColorPoint2d> buildPointList() {
		final Box2d bb = bbBuilder.build();

		List<ColorPoint2d> relPoints = Lists.newArrayList();
		for (ColorPoint2d p : points)
			relPoints.add(toBoundingBox(bb, p));

		return new PointListImpl(relPoints);
	}

	@Override
	public Box2d buildBoundingBox() {
		return bbBuilder.build();
	}
}
