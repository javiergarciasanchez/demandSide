package optimalPrice;

import java.util.Optional;

import org.apache.commons.math3.analysis.UnivariateFunction;

import consumers.Consumers;
import firms.Offer;

public class ExpectedMargin implements UnivariateFunction {

	double q;
	double cost;
	Optional<Offer> loOffer, hiOffer;

	public ExpectedMargin(double q, double cost, Optional<Offer> loOf, Optional<Offer> hiOf) {

		this.q = q;
		this.cost = cost;
		this.loOffer = loOf;
		this.hiOffer = hiOf;

	}

	@Override
	public double value(double p) {
		return (p - cost) * Consumers.expectedQuantity(p, q, loOffer, hiOffer);
	}

}
