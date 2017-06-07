package optimalPrice;

import org.apache.commons.math3.analysis.UnivariateFunction;
import consumers.Consumers;
import offer.Offer;

public class ExpectedMargin implements UnivariateFunction {

	double q;
	double cost;
	Offer loOffer, hiOffer;

	public ExpectedMargin(double q, double cost, Offer loOffer, Offer hiOffer) {

		this.q = q;
		this.cost = cost;
		this.loOffer = loOffer;
		this.hiOffer = hiOffer;

	}

	@Override
	public double value(double p) {
		return (p - cost) * Consumers.expectedQuantity(new Offer(p, q), loOffer, hiOffer);
	}

}
