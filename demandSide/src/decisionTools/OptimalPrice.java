package decisionTools;

import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import demandSide.Market;
import firms.Firm;
import firms.FirmsSegments;
import offer.Offer;

public class OptimalPrice {

	public static double get(FirmsSegments seg, double q, double cost) throws NoPrice {

		if (seg.isEmpty())
			return getOptimalPriceWithoutHigherLimitingFirm(seg, q, cost, Double.POSITIVE_INFINITY);

		Firm hiF = seg.higherFirmGivenQ(q);

		if (hiF == null)
			return getOptimalPriceWithoutHigherLimitingFirm(seg, q, cost, Double.POSITIVE_INFINITY);
		else
			return getOptimalPriceWithHigherLimitingFirms(seg, q, cost, hiF, Double.POSITIVE_INFINITY);

	}

	private static double getOptimalPriceWithoutHigherLimitingFirm(FirmsSegments seg, double q, double cost,
			double hiLimit) throws NoPrice {
		double min = FastMath.max(cost, Offer.getMinPrice(q));

		if (min > hiLimit)
			throw new NoPrice();
		else if (min == hiLimit)
			return min;

		Iterator<Firm> itByPriceToExpel = seg.stream().filter(f -> seg.getPriceToExpel(q, f) > min)
				.sorted(new CompareByPriceToExpel(seg, q)).collect(Collectors.toList()).iterator();

		Firm loF = null;
		double loLimit;

		double retval = 0.0, maxMargin = 0.0;
		double tempPrice, tempMargin;

		while (itByPriceToExpel.hasNext()) {
			loF = itByPriceToExpel.next();

			tempPrice = (Consumers.getLambda() * cost - loF.getPrice()) / (Consumers.getLambda() - 1.0);

			loLimit = seg.getPriceToExpel(q, loF);
			tempPrice = FastMath.max(tempPrice, loLimit);
			tempPrice = FastMath.min(tempPrice, hiLimit);

			tempMargin = Consumers.expectedQuantity(new Offer(tempPrice, q), seg.getOffer(loF), null)
					* (tempPrice - cost);

			if (tempMargin > maxMargin) {
				retval = tempPrice;
				maxMargin = tempMargin;
			}

			hiLimit = loLimit;

		}

		// Get the optimum of the last segment
		// Get the last lower limit, the one that cannot be expelled
		if (loF != null)
			loF = seg.lower(loF);
		else
			loF = seg.lowerFirmGivenQ(q);

		if (loF == null) {
			// no firm as last lower limit
			tempPrice = (Consumers.getLambda() * cost) / (Consumers.getLambda() - 1.0);
			tempPrice = FastMath.max(tempPrice, min);
			tempPrice = FastMath.min(tempPrice, hiLimit);

			tempMargin = Consumers.getMarketSize() * (tempPrice - cost);

		} else {
			// loF is the last lower limit
			tempPrice = (Consumers.getLambda() * cost - loF.getPrice()) / (Consumers.getLambda() - 1.0);
			tempPrice = FastMath.max(tempPrice, min);
			tempPrice = FastMath.min(tempPrice, hiLimit);

			tempMargin = Consumers.expectedQuantity(new Offer(tempPrice, q), seg.getOffer(loF), null)
					* (tempPrice - cost);
		}

		if (tempMargin > maxMargin)
			retval = tempPrice;

		return retval;
	}

	private static double getOptimalPriceWithHigherLimitingFirms(FirmsSegments seg, double q, double cost, Firm hiF,
			double hiLimit) throws NoPrice {

		if (hiF == null)
			throw new Error("hiF can not be null");

		double min = FastMath.max(cost, Offer.getMinPrice(q));

		if (min > hiLimit)
			throw new NoPrice();
		else if (min == hiLimit)
			return min;

		Iterator<Firm> itByPriceToExpel = seg.stream().filter(f -> seg.getPriceToExpel(q, f) > min)
				.sorted(new CompareByPriceToExpel(seg, q)).collect(Collectors.toList()).iterator();

		double loLimit;
		Offer loOffer = null;
		Firm loF = null;

		double retval = 0.0, maxMargin = 0.0;
		double tempPrice, tempMargin;
		Firm tempF = null;

		while (itByPriceToExpel.hasNext()) {
			tempF = itByPriceToExpel.next();

			if (seg.getQuality(tempF) < q)
				loF = tempF;
			else
				hiF = tempF;

			tempPrice = getSegmentOptimalPrice(seg, q, cost, loF, hiF, hiLimit);

			loLimit = seg.getPriceToExpel(q, tempF);
			tempPrice = FastMath.max(tempPrice, loLimit);
			tempPrice = FastMath.min(tempPrice, hiLimit);

			loOffer = (loF == null ? null : seg.getOffer(loF));
			tempMargin = Consumers.expectedQuantity(new Offer(tempPrice, q), loOffer, seg.getOffer(hiF))
					* (tempPrice - cost);

			if (tempMargin > maxMargin) {
				retval = tempPrice;
				maxMargin = tempMargin;
			}

			hiLimit = loLimit;

		}

		// get the optimum of last segment
		if (tempF == null) {
			// itByPriceToExpel was empty No firm is expelled
			loF = seg.lowerFirmGivenQ(q);

		} else {
			// Expel tempF
			if (seg.getQuality(tempF) < q)
				loF = Market.firms.firmsByQ.lower(tempF);
			else
				hiF = Market.firms.firmsByQ.higher(tempF);
		}

		tempPrice = getSegmentOptimalPrice(seg, q, cost, loF, hiF, hiLimit);
		tempPrice = FastMath.max(tempPrice, min);
		tempPrice = FastMath.min(tempPrice, hiLimit);

		loOffer = (loF == null ? null : seg.getOffer(loF));
		tempMargin = Consumers.expectedQuantity(new Offer(tempPrice, q), loOffer, seg.getOffer(hiF))
				* (tempPrice - cost);

		if (tempMargin > maxMargin)
			retval = tempPrice;

		return retval;

	}

	private static double getSegmentOptimalPrice(FirmsSegments seg, double q, double cost, Firm loF, Firm hiF,
			double hiLimit) throws NoPrice {

		// loF could be null but hiF no
		if (hiF == null)
			throw new Error("hiF shouldn't be null");

		double min = FastMath.max(cost, Offer.getMinPrice(q));

		double loLimit = seg.getPriceToExpel(q, hiF);
		if (loF != null) {
			loLimit = FastMath.max(loLimit, seg.getPriceToExpel(q, loF));
		}

		loLimit = FastMath.max(loLimit, min);

		if (loLimit > hiLimit)
			throw new NoPrice();
		else if (loLimit == hiLimit)
			return loLimit;

		double loP, hiP;
		loP = (loF != null) ? loF.getPrice() : 0.;
		hiP = hiF.getPrice();

		double positDerivPrice = (Consumers.getLambda() * cost - hiP) / (Consumers.getLambda() - 1);
		double negativDerivPrice = (Consumers.getLambda() * cost - loP) / (Consumers.getLambda() - 1);

		PriceDerivative priceDeriv = new PriceDerivative(seg, q, cost, seg.getOffer(loF), seg.getOffer(hiF));

		/*
		 * Need to check for border solutions
		 */
		if (loLimit > positDerivPrice) {
			// Possible border solution
			if (priceDeriv.value(loLimit) > 0)
				positDerivPrice = loLimit;
			else
				// Border solution
				return loLimit;
		}

		if (hiLimit < negativDerivPrice) {
			// Possible border solution
			if (priceDeriv.value(hiLimit) < 0)
				negativDerivPrice = hiLimit;
			else
				// Border solution
				return hiLimit;
		}

		NewtonRaphsonSolver solver = new  NewtonRaphsonSolver();
		int maxEval = 100;
		return solver.solve(maxEval, priceDeriv, positDerivPrice, negativDerivPrice);

	}

}