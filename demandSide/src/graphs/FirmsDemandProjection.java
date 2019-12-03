package graphs;

import java.math.BigDecimal;

import demandSide.Market;
import firms.Firm;
import repast.simphony.context.space.continuous.ContextSpace;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StickyBorders;

public class FirmsDemandProjection {

	private static final double MAX_X = 100., MAX_Y = 100., MAX_Z = 100.;
	private static final double MIN_X = 0., MIN_Y = 0., MIN_Z = 0.;

	private ContinuousSpace<Firm> space;
	private Market market;

	public FirmsDemandProjection(Market market) {

		double[] dims = new double[3];
		dims[0] = MAX_X + 0.1;
		dims[1] = MAX_Y + 0.1;
		dims[2] = MAX_Z + 0.1;

		space = new ContextSpace<Firm>("FirmsDemandProjection",
				new SimpleCartesianAdder<Firm>(), new StickyBorders(), dims);

		market.firms.addProjection(space);

	}

	public void update(Firm firm) {
		space.moveTo(firm, priceToCoord(firm.getPrice()),
				demandToCoord(firm.getDemand()),
				qualityToCoord(firm.getQuality()));
	}

	private double priceToCoord(BigDecimal price) {
		return (price.doubleValue() - Scale.getMinPrice())
				/ (Scale.getMaxPrice() - Scale.getMinPrice()) * (MAX_X - MIN_X)
				+ MIN_X;
	}

	private double demandToCoord(int demand) {
		return (double)demand / (double) market.consumers.getMarketSize() * (MAX_Y - MIN_Y) + MIN_Y;
	}

	private double qualityToCoord(BigDecimal quality) {
		return MAX_Z - ((quality.doubleValue() - Scale.getMinQuality())
				/ (Scale.getMaxQuality() - Scale.getMinQuality())
				* (MAX_Z - MIN_Z) + MIN_Z);
	}

}
