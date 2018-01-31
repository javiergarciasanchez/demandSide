package improvingOffer;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;

import firms.ExpectedMarket;
import offer.Offer;

public class PriceDerivative implements UnivariateDifferentiableFunction {

	ExpectedMarket seg;
	double q;
	Offer loLimitOffer, hiLimitOffer;
	double cost;
	double loLimit, hiLimit;

	public PriceDerivative(ExpectedMarket seg, double q, double cost, Offer loLimitOffer, Offer hiLimitOffer,
			double loLimit, double hiLimit) {
		this.seg = seg;
		this.q = q;
		this.cost = cost;
		this.loLimitOffer = loLimitOffer;
		this.hiLimitOffer = hiLimitOffer;
		this.loLimit = loLimit;
		this.hiLimit = hiLimit;

	}

	@Override
	public double value(double p) {

		// Need to check if price is meaningfull, otherwise return infinity
		// value
		if (p < loLimit)
			return Double.POSITIVE_INFINITY;

		else if (p > hiLimit)
			return Double.NEGATIVE_INFINITY;

		else {
			Offer of = new Offer(p, q);
			return MarginalProfit.respectToPrice(of, cost, loLimitOffer, hiLimitOffer);
		}

	}

	@Override
	public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException {

		double[] f = new double[2];
		double p = t.getValue();

		f[0] = value(p);

		Offer of = new Offer(p, q);
		f[1] = MarginalProfit.respectToPriceTwice(of, cost, loLimitOffer, hiLimitOffer);

		return t.compose(f);
	}

}
