package optimalPrice;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import consumers.Consumers;
import consumers.UtilityFunction;
import firms.Firm;
import firms.Offer;
import firms.ExpectedMarket;

public class OptimalPrice {

	public static Optional<OptimalPriceResult> get(BigDecimal perceivedQ, double cost, double knownByPerc,
			ExpectedMarket expMkt) {

		Neighbors currNeighbors;
		OptimalPriceResult returnResult = null;

		BigDecimal minPrice = Offer.getMinPrice(cost, perceivedQ);
		BigDecimal maxPrice = UtilityFunction.getMaxPriceToHaveMinimumExpectedDemand(perceivedQ, knownByPerc);

		// Remove firms that have price to expel higher than maxprice
		expMkt.removeIf(f -> {
			Optional<BigDecimal> pToE = expMkt.getPriceToExpel(perceivedQ, Optional.of(f));
			if (pToE.isPresent())
				return pToE.get().compareTo(maxPrice) >= 0;
			else
				return false;
		});

		// Get first optimal price (ie without expelling any neighbor
		try {
			currNeighbors = new Neighbors(expMkt, perceivedQ, minPrice, Optional.of(maxPrice));
		} catch (NoMarketSegmentForFirm e) {
			return Optional.empty();
		}
		returnResult = getSegmentOptimalResult(perceivedQ, cost, knownByPerc, currNeighbors);

		// Note that a neighbor that is expelled with any price has an empty
		// priceToBeExpelled
		// They are taken out of the iterator
		Iterator<ToBeExpelled> itToBeExpelled;
		itToBeExpelled = expMkt.stream().map(new AddPriceToBeExpelled(expMkt, perceivedQ))
				.filter(toBeExp -> toBeExp.optPriceToBeExpelled.isPresent())
				.filter(toBeExp -> toBeExp.getPriceToBeExpelled().compareTo(minPrice) >= 0)
				.sorted(new CompareByPriceToExpel()).collect(Collectors.toList()).iterator();

		Optional<BigDecimal> prevPriceToBeExpelled;
		ToBeExpelled toBeExpelled;
		while (itToBeExpelled.hasNext()) {

			toBeExpelled = itToBeExpelled.next();
			prevPriceToBeExpelled = toBeExpelled.optPriceToBeExpelled;
			expMkt.remove(toBeExpelled.f);

			try {
				currNeighbors = new Neighbors(expMkt, perceivedQ, minPrice, prevPriceToBeExpelled);
			} catch (NoMarketSegmentForFirm e) {
				continue;
			}

			OptimalPriceResult tempResult = getSegmentOptimalResult(perceivedQ, cost, knownByPerc, currNeighbors);
			if (tempResult.expInf.grossProfit > returnResult.expInf.grossProfit)
				returnResult = tempResult;

		}

		return Optional.of(returnResult);

	}

	private static OptimalPriceResult getSegmentOptimalResult(BigDecimal perceivedQ, double cost, double knownByPerc,
			Neighbors currNeighbors) {

		assert currNeighbors.getLoPriceLimit().compareTo(currNeighbors.getHiPriceLimit()) < 0;

		OptimalPriceResult result = new OptimalPriceResult();

		result.price = getSegmentOptimalPrice(perceivedQ, cost, knownByPerc, currNeighbors);

		Optional<Offer> loOffer = currNeighbors.getLoF().map(Firm::getPerceivedOffer);
		Optional<Offer> hiOffer = currNeighbors.getHiF().map(Firm::getPerceivedOffer);

		// Collect expected data
		result.expInf.demand = Consumers.getExpectedQuantityWExpecDistrib(new Offer(result.price, perceivedQ),
				loOffer, hiOffer) * knownByPerc;
		result.expInf.grossProfit = result.expInf.demand * (result.price.doubleValue() - cost);
		
		// Collect
		Optional<Offer> currOf = Optional.of(new Offer(result.price, perceivedQ));
		result.expInf.loLimit = Consumers.limitingWelfareParamPerceivedByFirms(loOffer, currOf);
		result.expInf.hiLimit = Consumers.limitingWelfareParamPerceivedByFirms(currOf, hiOffer);		
		
		return result;

	}

	private static BigDecimal getSegmentOptimalPrice(BigDecimal perceivedQ, double cost, double knownByPerc,
			Neighbors currNeighbors) {

		BigDecimal loPriceLimit = currNeighbors.getLoPriceLimit();
		BigDecimal hiPriceLimit = currNeighbors.getHiPriceLimit();

		assert loPriceLimit.compareTo(hiPriceLimit) < 0;

		BigDecimal retval;

		Optional<Offer> loOffer = currNeighbors.getLoF().map(Firm::getPerceivedOffer);
		Optional<Offer> hiOffer = currNeighbors.getHiF().map(Firm::getPerceivedOffer);

		// This function returns zero when expected demand is smaller than
		// minExpectedDemand
		ExpectedGrossProfitForMaximization expectedGrossProfitForMaxim = new ExpectedGrossProfitForMaximization(
				perceivedQ.doubleValue(), cost, knownByPerc, loOffer, hiOffer);

		// Setting the optimizer
		double rel = 1.e-12; // Relative threshold.
		double abs = 1.e-12; // Absolute threshold.

		BrentOptimizer optim = new BrentOptimizer(rel, abs);

		MaxEval maxEval = new MaxEval(1000);
		MaxIter maxIter = new MaxIter(1000);

		try {
			UnivariatePointValuePair optimRetval = optim.optimize(
					new SearchInterval(loPriceLimit.doubleValue(), hiPriceLimit.doubleValue()), GoalType.MAXIMIZE,
					new UnivariateObjectiveFunction(expectedGrossProfitForMaxim), maxIter, maxEval);
			
			double optPrice = optimRetval.getPoint();
			assert (loPriceLimit.doubleValue() <= optPrice) && ( optPrice <= hiPriceLimit.doubleValue());
			
			retval = BigDecimal.valueOf(optPrice);

		} catch (TooManyEvaluationsException e) {
			// it should return best value obtained, but we don't have it
			// returning the middle value of segment
			retval = loPriceLimit.add(hiPriceLimit).divide(BigDecimal.valueOf(2.0), Offer.getPriceScale(),
					Offer.getPriceRounding());
		}

		return retval.setScale(Offer.getPriceScale(), Offer.getPriceRounding());

	}

}