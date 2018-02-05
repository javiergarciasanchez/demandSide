package optimalPrice;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import consumers.Consumers;
import firms.Firm;
import firms.Offer;
import firms.ExpectedMarket;

public class OptimalPrice {

	public static Optional<OptimalPriceResult> get(BigDecimal perceivedQ, double cost, ExpectedMarket expMkt) {

		Neighbors currNeighbors;
		NeighborsByPriceToExpel neighborsByPriceToExpel;
		OptimalPriceResult returnResult = null, tempResult = null;

		BigDecimal minPrice = Offer.getMinPrice(cost, perceivedQ);

		try {
			neighborsByPriceToExpel = new NeighborsByPriceToExpel(expMkt, perceivedQ, minPrice);
		} catch (NoPrice e) {
			return Optional.empty();
		}
		
		currNeighbors = neighborsByPriceToExpel.currNeighbors;

		returnResult = getSegmentOptimalResult(expMkt, perceivedQ, cost, currNeighbors);

		while (neighborsByPriceToExpel.hasNext()) {

			currNeighbors = neighborsByPriceToExpel.next();

			if (currNeighbors != null) {
				tempResult = getSegmentOptimalResult(expMkt, perceivedQ, cost, currNeighbors);
				if (tempResult.margin > returnResult.margin)
					returnResult = tempResult;
			}
		}		
		
		return Optional.of(returnResult);

	}

	private static OptimalPriceResult getSegmentOptimalResult(ExpectedMarket expMkt, BigDecimal perceivedQ, double cost,
			Neighbors currNeighbors) {

		Optional<Firm> loF = currNeighbors.getLoF();
		Optional<Firm> hiF = currNeighbors.getHiF();
		BigDecimal loPriceLimit = currNeighbors.loPriceLimit;
		BigDecimal hiPriceLimit = currNeighbors.hiPriceLimit;

		assert loPriceLimit.compareTo(hiPriceLimit) < 0;

		OptimalPriceResult result = new OptimalPriceResult(BigDecimal.ZERO, 0);

		result.price = getSegmentOptimalPrice(expMkt, perceivedQ, cost, currNeighbors);

		Optional<Offer> loOffer = loF.map(Firm::getPerceivedOffer);
		Optional<Offer> hiOffer = hiF.map(Firm::getPerceivedOffer); 				

		result.margin = Consumers.expectedQuantity(new Offer(result.price, perceivedQ), loOffer, hiOffer)
				* (result.price.doubleValue() - cost);

		return result;

	}

	private static BigDecimal getSegmentOptimalPrice(ExpectedMarket expMkt, BigDecimal perceivedQ, double cost,
			Neighbors currNeighbors) {

		Optional<Firm> loF = currNeighbors.getLoF();
		Optional<Firm> hiF = currNeighbors.getHiF();
		BigDecimal loPriceLimit = currNeighbors.loPriceLimit;
		BigDecimal hiPriceLimit = currNeighbors.hiPriceLimit;

		assert loPriceLimit.compareTo(hiPriceLimit) < 0;

		double lambda = Consumers.getLambda();
		BigDecimal retval;

		if (!hiF.isPresent()) {

			BigDecimal loP = loF.map(Firm::getPrice).orElse(BigDecimal.ZERO);

			// retval = (lambda * cost - loP) / (lambda - 1.0)
			retval = BigDecimal.valueOf(lambda * cost).subtract(loP).divide(BigDecimal.valueOf(lambda - 1.0),
					MathContext.DECIMAL64);
			retval = retval.max(loPriceLimit);
			retval = retval.min(hiPriceLimit);

		} else {

			Optional<Offer> loOffer = loF.map(Firm::getPerceivedOffer); 
			Optional<Offer> hiOffer = hiF.map(Firm::getPerceivedOffer);

			ExpectedMargin expectedMargin = new ExpectedMargin(perceivedQ.doubleValue(), cost, loOffer, hiOffer);

			// Setting the optimizer
			double rel = 1.e-12; // Relative threshold.
			double abs = 1.e-12; // Absolute threshold.

			BrentOptimizer optim = new BrentOptimizer(rel, abs);

			MaxEval maxEval = new MaxEval(100);
			MaxIter maxIter = new MaxIter(100);

			try {
				UnivariatePointValuePair optimRetval = optim.optimize(
						new SearchInterval(loPriceLimit.doubleValue(), hiPriceLimit.doubleValue()), GoalType.MAXIMIZE,
						new UnivariateObjectiveFunction(expectedMargin), maxIter, maxEval);
				retval = BigDecimal.valueOf(optimRetval.getPoint());

			} catch (TooManyEvaluationsException e) {
				// it should return best value obtained, but we don't have it
				// returning the middle value of segment
				retval = loPriceLimit.add(hiPriceLimit).divide(BigDecimal.valueOf(2.0), Offer.getPriceScale(),
						Offer.getPriceRounding());
			}

		}

		return retval.setScale(Offer.getPriceScale(), Offer.getPriceRounding());

	}

}