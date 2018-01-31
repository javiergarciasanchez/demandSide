package graphs;

import java.math.BigDecimal;

import firms.Firm;
import firms.Firms;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class Firms2DProjection {

	private static final double MAX_X = 100, MAX_Y = 100;
	private static final double MIN_X = 0, MIN_Y = 0;

	private ContinuousSpace<Firm> space;

	public Firms2DProjection(Firms firms) {

		double[] dims = new double[2];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + 0.1;

		space = new ContextSpace<Firm>("Firms2DProjection", new SimpleCartesianAdder<Firm>(), new StickyBorders(),
				dims);

		firms.addProjection(space);

	}

	public void update(Firm firm) {
		space.moveTo(firm, priceToCoord(firm.getPrice()), qualityToCoord(firm.getQuality()));
	}

	private double priceToCoord(BigDecimal price) {
		return (price.doubleValue() - Scale.getMinPrice()) / (Scale.getMaxPrice() - Scale.getMinPrice())
				* (MAX_X - MIN_X) + MIN_X;
	}

	private double qualityToCoord(BigDecimal quality) {
		return (quality.doubleValue() - Scale.getMinQuality())
				/ (Scale.getMaxQuality() - Scale.getMinQuality()) * (MAX_Y - MIN_Y) + MIN_Y;
	}

}
