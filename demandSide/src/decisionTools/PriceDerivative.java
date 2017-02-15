package decisionTools;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;

import firms.FirmsSegments;
import offer.Offer;

public class PriceDerivative implements UnivariateDifferentiableFunction {

	FirmsSegments seg;
	double q;
	Offer loLimitOffer, hiLimitOffer;
	double cost;

	public PriceDerivative(FirmsSegments seg, double q, double cost, Offer loLimitOffer, Offer hiLimitOffer) {
		this.seg = seg;
		this.q = q;
		this.cost = cost;
		this.loLimitOffer = loLimitOffer;
		this.hiLimitOffer = hiLimitOffer;
	}

	@Override
	public double value(double p) {
		Offer of = new Offer(p, q);
		return MarginalProfit.respectToPrice(of, cost, loLimitOffer, hiLimitOffer);
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
