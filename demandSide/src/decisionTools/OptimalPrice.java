package decisionTools;

import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import firms.Firm;
import firms.FirmsSegments;
import offer.Offer;

public class OptimalPrice {

	public static double get(FirmsSegments seg, double q, double cost) throws NoPrice {

		if (seg.isEmpty())
			return getOptimalPriceWithoutHigherLimitingFirm(seg, q, cost);

		Firm hiF = seg.getHigherFirmGivenQ(q);

		if (hiF == null)
			return getOptimalPriceWithoutHigherLimitingFirm(seg, q, cost);
		else
			return getOptimalPriceWithHigherLimitingFirms(seg, q, cost, hiF);

	}

	private static double getOptimalPriceWithoutHigherLimitingFirm(FirmsSegments seg, double q, double cost)
			throws NoPrice {
		double lambda = Consumers.getLambda();

		Iterator<Firm> itByPriceToExpel = seg.stream().filter(f -> seg.getPriceToExpel(q, f) > cost)
				.sorted(new CompareByPriceToExpel(seg, q)).collect(Collectors.toList()).iterator();

		Firm loF = null;		
		double hiLimit = Double.POSITIVE_INFINITY;

		double retval = 0.0, maxMargin = 0.0;
		double tempPrice, tempMargin;

		while (itByPriceToExpel.hasNext()) {
			loF = itByPriceToExpel.next();

			tempPrice = (lambda * cost - loF.getPrice()) / (lambda - 1.0);

			double loLimit = seg.getPriceToExpel(q, loF);
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
			// itByPriceToExpel was empty
			loF = seg.getLowerFirmGivenQ(q);

		double loPrice = 0.0;
		Offer loOffer = null;

		if (loF != null) {
			loPrice = loF.getPrice();
			loOffer = seg.getOffer(loF);
		}

		tempPrice = (lambda * cost - loPrice) / (lambda - 1.0);
		tempPrice = FastMath.max(tempPrice, cost);
		tempPrice = FastMath.min(tempPrice, hiLimit);

		tempMargin = Consumers.expectedQuantity(new Offer(tempPrice, q), loOffer, null) * (tempPrice - cost);

		if (tempMargin > maxMargin)
			retval = tempPrice;

		return retval;
	}

	private static double getOptimalPriceWithHigherLimitingFirms(FirmsSegments seg, double q, double cost, Firm hiF)
			throws NoPrice {

		if (hiF == null)
			throw new Error("hiF can not be null");

		Iterator<Firm> itByPriceToExpel = seg.stream().filter(f -> seg.getPriceToExpel(q, f) > cost)
				.sorted(new CompareByPriceToExpel(seg, q)).collect(Collectors.toList()).iterator();

		Firm loF = seg.getLowerFirmGivenQ(q);
		Offer loOffer;

		// The price should be lower than the price of higher quality competitor
		double hiLimit = hiF.getPrice() - Double.MIN_NORMAL;

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

			double loLimit = seg.getPriceToExpel(q, tempF);
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
			loF = seg.getLowerFirmGivenQ(q);

		} else {
			// Expel tempF
			if (seg.getQuality(tempF) < q)
				loF = seg.lower(tempF);
			else
				hiF = seg.higher(tempF);
		}

		tempPrice = getSegmentOptimalPrice(seg, q, cost, loF, hiF, hiLimit);
		tempPrice = FastMath.max(tempPrice, cost);
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

		double loLimit = seg.getPriceToExpel(q, hiF);
		if (loF != null) {
			loLimit = FastMath.max(loLimit, seg.getPriceToExpel(q, loF));
		}

		loLimit = FastMath.max(loLimit, cost);

		if (loLimit > hiLimit)
			throw new NoPrice();
		else if (loLimit == hiLimit)
			return loLimit;

		Offer loOffer = seg.getOffer(loF);
		Offer hiOffer = seg.getOffer(hiF);
		PriceDerivative priceDeriv = new PriceDerivative(seg, q, cost, loOffer, hiOffer, loLimit, hiLimit);

		final double absAccuracy = 1e-3;
		int maxEval = 100;
		NewtonRaphsonSolver solver = new NewtonRaphsonSolver(absAccuracy);

		try {

			double retval = solver.solve(maxEval, priceDeriv, loLimit, hiLimit);

			if (checkMax(seg, new Offer(retval, q), cost, loOffer, hiOffer))
				return retval;
			else
				return getBestBorderSolution(loLimit, hiLimit, q, cost, loOffer, hiOffer);

		} catch (TooManyEvaluationsException e) {

			// It is assumed there is no maximum in the interval then the
			// highest border is chosen
			return getBestBorderSolution(loLimit, hiLimit, q, cost, loOffer, hiOffer);

		}

	}

	private static boolean checkMax(FirmsSegments seg, Offer of, double cost, Offer loOffer, Offer hiOffer) {
		// Checks that second derivative is negative

		double lambda = Consumers.getLambda();
		double alfa = (of.getPrice() - cost) * (lambda + 1);

		double deltaLowPrice = Consumers.deltaPrice(loOffer, of);
		double deltaLowQuality = Consumers.deltaQuality(loOffer, of);
		double deltaHighPrice = Consumers.deltaPrice(of, hiOffer);
		double deltaHighQuality = Consumers.deltaQuality(of, hiOffer);

		return (((alfa - 2 * deltaLowPrice)
				/ (alfa + 2 * deltaLowPrice)) < (FastMath.pow(deltaHighQuality / deltaLowQuality, lambda)
						* FastMath.pow(deltaLowPrice / deltaHighPrice, lambda + 2)));

	}

	private static double getBestBorderSolution(double loPrice, double hiPrice, double q, double cost, Offer loOffer,
			Offer hiOffer) {
		double loPriceMargin = expectedMargin(loPrice, q, cost, loOffer, hiOffer);
		double hiPriceMargin = expectedMargin(hiPrice, q, cost, loOffer, hiOffer);

		if (loPriceMargin < hiPriceMargin)
			return loPrice;
		else
			return hiPrice;
	}

	private static double expectedMargin(double p, double q, double cost, Offer loOffer, Offer hiOffer) {
		return (p - cost) * Consumers.expectedQuantity(new Offer(p, q), loOffer, hiOffer);
	}

}