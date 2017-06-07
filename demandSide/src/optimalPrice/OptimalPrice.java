package optimalPrice;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import firms.Firm;
import firms.FirmsPerceivedQSegments;
import offer.Offer;

public class OptimalPrice {

	public static double get(double perceivedQ, double cost, FirmsPerceivedQSegments seg) throws NoPrice {

		Neighbors currNeighbors;
		NeighborsByPriceToExpel neighborsByPriceToExpel;
		OptimalPriceResult returnResult = null, tempResult = null;

		neighborsByPriceToExpel = new NeighborsByPriceToExpel(seg, perceivedQ, cost);
		currNeighbors = neighborsByPriceToExpel.currNeighbors;

		returnResult = getSegmentOptimalResult(seg, perceivedQ, cost, currNeighbors);

		while (neighborsByPriceToExpel.hasNext()) {

			currNeighbors = neighborsByPriceToExpel.next();

			tempResult = getSegmentOptimalResult(seg, perceivedQ, cost, currNeighbors);
			if (tempResult.margin > returnResult.margin)
				returnResult = tempResult;

		}

		return returnResult.price;

	}

	private static OptimalPriceResult getSegmentOptimalResult(FirmsPerceivedQSegments seg, double perceivedQ,
			double cost, Neighbors currNeighbors) {

		Firm loF = currNeighbors.getLoF();
		Firm hiF = currNeighbors.getHiF();
		double loLimit = currNeighbors.loLimit;
		double hiLimit = currNeighbors.hiLimit;

		assert loLimit < hiLimit;

		OptimalPriceResult result = new OptimalPriceResult(0, 0);

		result.price = getSegmentOptimalPrice(seg, perceivedQ, cost, currNeighbors);

		Offer loOffer = (loF == null ? null : Firm.getPerceivedOffer(loF));
		Offer hiOffer = (hiF == null ? null : Firm.getPerceivedOffer(hiF));
		result.margin = Consumers.expectedQuantity(new Offer(result.price, perceivedQ), loOffer, hiOffer)
				* (result.price - cost);

		return result;

	}

	private static double getSegmentOptimalPrice(FirmsPerceivedQSegments seg, double perceivedQ, double cost,
			Neighbors currNeighbors) {

		Firm loF = currNeighbors.getLoF();
		Firm hiF = currNeighbors.getHiF();
		double loLimit = currNeighbors.loLimit;
		double hiLimit = currNeighbors.hiLimit;

		assert loLimit < hiLimit;

		double lambda = Consumers.getLambda();
		double retval;

		if (hiF == null) {

			double loP;
			if (loF != null)
				loP = loF.getPrice();
			else
				loP = 0;

			retval = (lambda * cost - loP) / (lambda - 1.0);
			retval = FastMath.max(retval, loLimit);
			retval = FastMath.min(retval, hiLimit);

		} else {

			Offer loOffer = null;

			if (loF != null)
				loOffer = Firm.getPerceivedOffer(loF);

			Offer hiOffer = Firm.getPerceivedOffer(hiF);

			ExpectedMargin expectedMargin = new ExpectedMargin(perceivedQ, cost, loOffer, hiOffer);

			// Setting the optimizer
			double rel = 1.e-12; // Relative threshold.
			double abs = 1.e-12; // Absolute threshold.

			BrentOptimizer optim = new BrentOptimizer(rel, abs);

			MaxEval maxEval = new MaxEval(100);
			MaxIter maxIter = new MaxIter(100);

			try {
				UnivariatePointValuePair optimRetval = optim.optimize(new SearchInterval(loLimit, hiLimit),
						GoalType.MAXIMIZE, new UnivariateObjectiveFunction(expectedMargin), maxIter, maxEval);
				retval = optimRetval.getPoint();

			} catch (TooManyEvaluationsException e) {
				// it should return best value obtained, but we don't have it
				// returning initial value
				return (loLimit + hiLimit) / 2.0;
			}

		}

		return retval;

	}

}