package optimalPrice;

import java.util.Iterator;
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

	public static Optional<OptimalPriceResult> get(Firm firm, double realQ, ExpectedMarket expMkt) {

		Consumers consumers = firm.market.consumers;

		double perceivedQ = firm.getPerceivedQuality(realQ);
		double cost = firm.getUnitCost(realQ);

		Neighbors currNeighbors;
		OptimalPriceResult returnResult = null;

		double minPrice = consumers.getMinPrice(cost, perceivedQ);
		double maxPrice = consumers.getMaxPriceForRichestConsumer(firm, perceivedQ);

		/*
		 * Remove firms that are not going to be considered as neighbors
		 *
		 * It includes
		 * 
		 * - firms that are expelled at any price: price to be expelled is empty
		 * 
		 * - firms that have price to be expelled higher than maxprice
		 * 
		 */
		expMkt.removeIf(f -> {
			Optional<Double> pToE = expMkt.getPriceToExpel(perceivedQ, Optional.of(f));
			if (pToE.isPresent())
				return pToE.get().compareTo(maxPrice) >= 0;
			else
				return false;
		});

		// Get first optimal price (ie without expelling any neighbor)
		try {
			currNeighbors = new Neighbors(firm, expMkt, perceivedQ, minPrice, Optional.of(maxPrice));
		} catch (NoMarketSegmentForFirm e) {
			return Optional.empty();
		}
		returnResult = getSegmentOptimalResult(firm, realQ, currNeighbors);

		// Note that a neighbor that is expelled with any price has an empty
		// priceToBeExpelled
		// They are taken out of the iterator
		Iterator<ToBeExpelled> itToBeExpelled;
		itToBeExpelled = expMkt.stream().map(new AddPriceToBeExpelled(expMkt, perceivedQ))
				.filter(toBeExp -> toBeExp.optPriceToBeExpelled.isPresent())
				.filter(toBeExp -> toBeExp.getPriceToBeExpelled() >= minPrice)
				.sorted(new CompareByPriceToExpel())
				.iterator();
		

		Optional<Double> prevPriceToBeExpelled;
		ToBeExpelled toBeExpelled;
		while (itToBeExpelled.hasNext()) {

			toBeExpelled = itToBeExpelled.next();
			prevPriceToBeExpelled = toBeExpelled.optPriceToBeExpelled;
			expMkt.remove(toBeExpelled.f);

			try {
				currNeighbors = new Neighbors(firm, expMkt, perceivedQ, minPrice, prevPriceToBeExpelled);
			} catch (NoMarketSegmentForFirm e) {
				continue;
			}

			OptimalPriceResult tempResult = getSegmentOptimalResult(firm, realQ, currNeighbors);
			if (tempResult.expInf.profit > returnResult.expInf.profit)
				returnResult = tempResult;

		}

		return Optional.of(returnResult);

	}

	private static OptimalPriceResult getSegmentOptimalResult(Firm firm, double realQ, Neighbors currNeighbors) {

		Consumers consumers = firm.market.consumers;

		assert (currNeighbors.getLoPriceLimit() < currNeighbors.getHiPriceLimit());

		double perceivedQ = firm.getPerceivedQuality(realQ);
		double cost = firm.getUnitCost(realQ);

		OptimalPriceResult result = new OptimalPriceResult();

		result.price = getSegmentOptimalPrice(firm, realQ, currNeighbors);

		Optional<Offer> loOffer = currNeighbors.getLoF().map(f -> firm.getCompetitorPerceivedOffer(f));
		Optional<Offer> hiOffer = currNeighbors.getHiF().map(f -> firm.getCompetitorPerceivedOffer(f));

		// Collect expected data
		double fullDemand = consumers.getExpectedQuantity(new Offer(result.price, perceivedQ), loOffer, hiOffer);
		result.expInf.demand = firm.getAdjustedDemand(fullDemand);

		result.expInf.profit = Firm.calcProfit(result.price, cost, result.expInf.demand, firm.getFixedCost());

		// Collect
		Optional<Offer> currOf = Optional.of(new Offer(result.price, perceivedQ));
		result.expInf.loLimit = consumers.limitingWelfareParamPerceivedByFirms(loOffer, currOf);
		result.expInf.hiLimit = consumers.limitingWelfareParamPerceivedByFirms(currOf, hiOffer);

		return result;

	}

	private static double getSegmentOptimalPrice(Firm firm, double realQ, Neighbors currNeighbors) {

		double loPriceLimit = currNeighbors.getLoPriceLimit();
		double hiPriceLimit = currNeighbors.getHiPriceLimit();

		assert (loPriceLimit < hiPriceLimit);

		double retval;

		Optional<Offer> loOffer = currNeighbors.getLoF().map(f -> firm.getCompetitorPerceivedOffer(f));
		Optional<Offer> hiOffer = currNeighbors.getHiF().map(f -> firm.getCompetitorPerceivedOffer(f));

		// This function returns zero when expected demand is smaller than
		// minExpectedDemand
		ExpectedProfitForMaximization expectedGrossProfitForMaxim = new ExpectedProfitForMaximization(firm, realQ,
				loOffer, hiOffer);

		// Setting the optimizer
		double rel = 1.e-12; // Relative threshold.
		double abs = 1.e-12; // Absolute threshold.

		BrentOptimizer optim = new BrentOptimizer(rel, abs);

		MaxEval maxEval = new MaxEval(1000);
		MaxIter maxIter = new MaxIter(1000);

		try {
			UnivariatePointValuePair optimRetval = optim.optimize(
					new SearchInterval(loPriceLimit, hiPriceLimit), GoalType.MAXIMIZE,
					new UnivariateObjectiveFunction(expectedGrossProfitForMaxim), maxIter, maxEval);

			double optPrice = optimRetval.getPoint();
			assert (loPriceLimit <= optPrice) && (optPrice <= hiPriceLimit);

			retval = optPrice;

		} catch (TooManyEvaluationsException e) {
			// it should return best value obtained, but we don't have it
			// returning the middle value of segment
			retval = loPriceLimit + hiPriceLimit / 2.0;
		}

		return retval;

	}

}