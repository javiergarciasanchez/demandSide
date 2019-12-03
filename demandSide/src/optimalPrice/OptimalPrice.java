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
import firms.Firm;
import firms.Offer;
import firms.ExpectedMarket;

public class OptimalPrice {

	public static Optional<OptimalPriceResult> get(Firm firm, BigDecimal realQ, ExpectedMarket expMkt) {
		
		Consumers consumers = firm.market.consumers;

		BigDecimal perceivedQ = firm.getPerceivedQuality(realQ);
		double cost = firm.getUnitCost(realQ);

		Neighbors currNeighbors;
		OptimalPriceResult returnResult = null;

		BigDecimal minPrice = consumers.getMinPrice(cost, perceivedQ);
		BigDecimal maxPrice = consumers.getMaxPriceForRichestConsumer(firm, perceivedQ);

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
			Optional<BigDecimal> pToE = expMkt.getPriceToExpel(perceivedQ, Optional.of(f));
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
				.filter(toBeExp -> toBeExp.getPriceToBeExpelled().compareTo(minPrice) >= 0)
				.sorted(new CompareByPriceToExpel()).collect(Collectors.toList()).iterator();

		Optional<BigDecimal> prevPriceToBeExpelled;
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

	private static OptimalPriceResult getSegmentOptimalResult(Firm firm, BigDecimal realQ, Neighbors currNeighbors) {
		
		Consumers consumers = firm.market.consumers;

		assert currNeighbors.getLoPriceLimit().compareTo(currNeighbors.getHiPriceLimit()) < 0;

		BigDecimal perceivedQ = firm.getPerceivedQuality(realQ);
		double cost = firm.getUnitCost(realQ);

		OptimalPriceResult result = new OptimalPriceResult();

		result.price = getSegmentOptimalPrice(firm, realQ, currNeighbors);

		Optional<Offer> loOffer = currNeighbors.getLoF().map(f -> firm.getCompetitorPerceivedOffer(f));
		Optional<Offer> hiOffer = currNeighbors.getHiF().map(f -> firm.getCompetitorPerceivedOffer(f));

		// Collect expected data
		double fullDemand = consumers.getExpectedQuantity(new Offer(result.price, perceivedQ), loOffer, hiOffer);
		result.expInf.demand = firm.getAdjustedDemand(fullDemand);

		result.expInf.profit = Firm.calcProfit(result.price.doubleValue(), cost, result.expInf.demand,
				firm.getFixedCost());

		// Collect
		Optional<Offer> currOf = Optional.of(new Offer(result.price, perceivedQ));
		result.expInf.loLimit = consumers.limitingWelfareParamPerceivedByFirms(loOffer, currOf);
		result.expInf.hiLimit = consumers.limitingWelfareParamPerceivedByFirms(currOf, hiOffer);

		return result;

	}

	private static BigDecimal getSegmentOptimalPrice(Firm firm, BigDecimal realQ, Neighbors currNeighbors) {

		BigDecimal loPriceLimit = currNeighbors.getLoPriceLimit();
		BigDecimal hiPriceLimit = currNeighbors.getHiPriceLimit();

		assert loPriceLimit.compareTo(hiPriceLimit) < 0;

		BigDecimal retval;

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
					new SearchInterval(loPriceLimit.doubleValue(), hiPriceLimit.doubleValue()), GoalType.MAXIMIZE,
					new UnivariateObjectiveFunction(expectedGrossProfitForMaxim), maxIter, maxEval);

			double optPrice = optimRetval.getPoint();
			assert (loPriceLimit.doubleValue() <= optPrice) && (optPrice <= hiPriceLimit.doubleValue());

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