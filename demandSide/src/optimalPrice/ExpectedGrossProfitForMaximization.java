package optimalPrice;

import java.util.Optional;

import org.apache.commons.math3.analysis.UnivariateFunction;

import consumers.Consumers;
import firms.Offer;

public class ExpectedGrossProfitForMaximization implements UnivariateFunction {

	private double q;
	private double cost;
	private double knownByPerc;
	Optional<Offer> loOffer, hiOffer;

	public ExpectedGrossProfitForMaximization(double q, double cost, double knownByPerc, Optional<Offer> loOf, Optional<Offer> hiOf) {

		this.q = q;
		this.cost = cost;
		this.knownByPerc = knownByPerc;
		this.loOffer = loOf;
		this.hiOffer = hiOf;

	}

	@Override
	public double value(double p) {
		double expDemand = Consumers.getExpectedQuantityWExpecDistrib(new Offer(p, q), loOffer, hiOffer) * knownByPerc;

		// In order to choose a price where expected demand is higher than minExpDemand
		// we set margin to zero when expected demand is below min
		if (expDemand < Consumers.getMinExpectedDemand())
			return 0.0;
		else
			return (p - cost) * expDemand;
	}

}
