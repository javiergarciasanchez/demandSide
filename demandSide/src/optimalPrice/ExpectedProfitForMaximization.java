package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.math3.analysis.UnivariateFunction;

import consumers.Consumers;
import firms.Firm;
import firms.Offer;

public class ExpectedProfitForMaximization implements UnivariateFunction {

	private double perceivedQ;
	private double cost;
	private double knownByPerc;
	private double fixedCost;
	Optional<Offer> loOffer, hiOffer;

	public ExpectedProfitForMaximization(Firm firm, BigDecimal realQ, Optional<Offer> loOf, Optional<Offer> hiOf) {

		this.perceivedQ = firm.getPerceivedQuality(realQ).doubleValue();
		this.cost = firm.getUnitCost(realQ);
		this.knownByPerc = firm.getKnownByPerc();
		this.fixedCost = firm.getFixedCost();
		this.loOffer = loOf;
		this.hiOffer = hiOf;

	}

	@Override
	public double value(double p) {
		double expDemand = Consumers.getExpectedQuantityWExpecDistrib(new Offer(p, perceivedQ), loOffer, hiOffer)
				* knownByPerc;

		// In order to choose a price where expected demand is higher than minExpDemand
		// we set margin to zero when expected demand is below min
		if (expDemand < Consumers.getMinExpectedDemand())
			return 0.0;
		else
			return (p - cost) * expDemand - fixedCost;
	}

}
