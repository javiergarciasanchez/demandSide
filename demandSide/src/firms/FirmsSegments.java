package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

import pHX_2.Market;
import consumers.Consumers;

public abstract class FirmsSegments extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;

	// It orders the list from the highest price to the lowest
	private class CompareByPriceToExpel implements Comparator<Firm> {
		private double q;

		public CompareByPriceToExpel(double q) {
			this.q = q;
		}

		@Override
		public int compare(Firm f1, Firm f2) {

			if (f1.equals(f2))
				return 0;
			else
				return (getPriceToExpel(q, f1) > getPriceToExpel(q, f2) ? -1
						: 1);
		}

	}

	public FirmsSegments(Comparator<Firm> compBy) {
		super(compBy);
	}

	@Override
	public boolean add(Firm f) {
		if (checkEntry(f)) {
			// Take out from the market expelled firms
			takeOutExpelledFirms(f);
			return super.add(f);
		} else
			return false;
	}

	@Override
	public boolean remove(Object f) {

		boolean retval = remove(f);

		// after remove check if any of the firms that are out of the market may
		// enter
		Market.firms.stream().filter(e -> (!contains(e) && checkEntry(e)))
				.forEach(e -> super.add(e));

		return retval;
	}

	private Offer getOffer(Firm f) {
		Offer retval = new Offer(f.getOffer());
		retval.setQuality(getQuality(f));

		return retval;

	}

	// Quality according to segments ordering
	protected abstract double getQuality(Firm f);

	protected double getOptimalPrice(double q, double cost,
			ToDoubleBiFunction<Double, Double> selectPriceFromRange) {
		Firm loF, hiF;

		loF = lowerFirmGivenQ(q);

		if (loF != null)
			hiF = higher(loF);
		else
			hiF = first();

		if (loF == null && hiF == null)
			return getSegmentOptimalPriceWithoutLimits(q, cost);
		else if (loF != null && hiF == null)
			return getSegmentOptimalPriceWithLowerLimit(q, cost, loF);
		else
			return getSegmentOptimalPriceWithBothLimits(q, cost, loF, hiF,
					selectPriceFromRange);

	}

	private double getSegmentOptimalPriceWithoutLimits(double q, double cost) {

		double retval = Consumers.lambda * cost / (Consumers.lambda - 1.0);

		return Math.max(retval, Offer.getMinPrice(q));
	}

	private double getSegmentOptimalPriceWithLowerLimit(double q, double cost,
			Firm loF) {

		double retval = (Consumers.lambda * cost - loF.getPrice())
				/ (Consumers.lambda - 1.0);

		retval = Math.max(retval, Offer.getMinPrice(q));

		// Price shouldn't expel loF
		retval = Math.max(retval, getPriceToExpel(q, loF));

		// Price should be higher than cost
		retval = Math.max(retval, cost);

		return retval;
	}

	// It returns 0.0 if there is no price for the firm to enter the market
	// ie if expected quantity is zero
	private double getSegmentOptimalPriceWithBothLimits(double q, double cost,
			Firm loF, Firm hiF,
			ToDoubleBiFunction<Double, Double> selectPriceFromRange) {

		// loF could be null but hiF no
		if (hiF == null)
			throw new Error("hiF shouldn't be null");

		double retVal = 0., expectedMargin = 0.;
		double tempPrice, tempMargin;
		Firm tempF = null;

		// Limits to avoid expelling competitors
		double lowerPriceLimit, higherPriceLimit;

		Iterator<Firm> itByPriceToExpel = stream()
				.filter(f -> getPriceToExpel(q, f) > cost)
				.sorted(new CompareByPriceToExpel(q))
				.collect(Collectors.toList()).iterator();

		lowerPriceLimit = Double.POSITIVE_INFINITY;
		while (itByPriceToExpel.hasNext()) {

			// Note that the first tempF is either equal to loF or to hiF
			tempF = itByPriceToExpel.next();
			if (getQuality(tempF) < q)
				loF = tempF;
			else
				hiF = tempF;

			higherPriceLimit = lowerPriceLimit;
			lowerPriceLimit = getPriceToExpel(q, tempF);

			tempPrice = getSegmentOptimalPriceBetweenLimits(q, cost, loF, hiF,
					higherPriceLimit, lowerPriceLimit, selectPriceFromRange);
			tempMargin = expectedQuantity(tempPrice, q, loF, hiF)
					* (tempPrice - cost);

			if (tempMargin > expectedMargin) {
				retVal = tempPrice;
				expectedMargin = tempMargin;
			}

		}

		if (tempF != null) {
			// Last segment, expelling the last neighbor that could be expelled
			if (getQuality(tempF) < q)
				loF = lower(loF);
			else
				hiF = higher(hiF);

			higherPriceLimit = lowerPriceLimit;
			lowerPriceLimit = cost;

			tempPrice = getSegmentOptimalPriceBetweenLimits(q, cost, loF, hiF,
					higherPriceLimit, lowerPriceLimit, selectPriceFromRange);
			tempMargin = expectedQuantity(tempPrice, q, loF, hiF)
					* (tempPrice - cost);

			if (tempMargin > expectedMargin)
				retVal = tempPrice;
		}

		return retVal;

	}

	/*
	 * As there is no functional solution to the optimization problem, we use a
	 * range where the optimum is located
	 * 
	 * Inside the range, firm chooses optimum according to its preference for
	 * quality
	 */
	private double getSegmentOptimalPriceBetweenLimits(double q, double cost,
			Firm loF, Firm hiF, double higherPriceLimit,
			double lowerPriceLimit,
			ToDoubleBiFunction<Double, Double> selectPriceFromRange) {

		// loF could be null but hiF no
		if (hiF == null)
			throw new Error("hiF shouldn't be null");

		double lo, hi;
		double loP, loQ, hiP, hiQ;

		if (loF == null) {
			loP = Offer.getMinPrice(q);
			loQ = q;
		} else {
			loP = loF.getPrice();
			loQ = getQuality(loF);
		}

		hiP = hiF.getPrice();
		hiQ = getQuality(hiF);

		double gamma = (hiQ - q) / (hiQ - loQ);
		double theta = loP * gamma + hiP * (1 - gamma);

		lo = (Consumers.lambda * cost - theta) / (Consumers.lambda - 1);
		hi = (Consumers.lambda * cost - loP) / (Consumers.lambda - 1);

		lo = Math.max(cost, lo);
		lo = Math.max(lowerPriceLimit, lo);
		lo = Math.max(Offer.getMinPrice(q), lo);

		hi = Math.min(higherPriceLimit, hi);

		return selectPriceFromRange.applyAsDouble(lo, hi);

	}

	private double expectedQuantity(double p, double q, Firm loF, Firm hiF) {

		double loLimit, hiLimit;

		if (loF == null)
			loLimit = Consumers.getMinMargUtilOfQuality();
		else
			loLimit = Offer.limitOperator(getOffer(loF), new Offer(p, q));

		if (hiF == null)
			return Consumers.getExpectedConsumersAbove(loLimit);

		else {
			hiLimit = Offer.limitOperator(new Offer(p, q), getOffer(hiF));
			return Consumers.getExpectedConsumersAbove(loLimit)
					- Consumers.getExpectedConsumersAbove(hiLimit);

		}

	}

	private boolean checkEntry(Firm f) {
		if (f == null)
			throw new Error("Cannot check entry of null firm");

		Firm lo = lower(f);
		Firm hi = higher(f);

		if (hi == null)
			// f has the highest quality
			return true;

		// Note that limitOperator could return positive infinity
		else if (lo == null) {
			return (Consumers.getMinMargUtilOfQuality() < Offer.limitOperator(
					getOffer(f), getOffer(hi)));
		}

		else
			return Offer.limitOperator(getOffer(lo), getOffer(f)) < Offer
					.limitOperator(getOffer(f), getOffer(hi));

	}

	private void takeOutExpelledFirms(Firm f) {

		Firm loF, hiF;
		double q = getQuality(f);
		double p = f.getPrice();

		loF = lower(f);
		while (loF != null) {
			if (p < getPriceToExpel(q, loF)) {
				remove(loF);
				loF = lower(loF);
			} else
				break;
		}

		hiF = higher(f);
		while (hiF != null) {
			if (p > getPriceToExpel(q, hiF)) {
				remove(hiF);
				hiF = higher(loF);
			} else
				break;
		}

	}

	// Note that it could return positive infinity
	private double getPriceToExpel(double q, Firm f) {
		if (f == null)
			throw new Error("Cannot get a price to expel null firm");

		double qF = getQuality(f);
		if (q == qF)
			return f.getPrice() - Double.MIN_NORMAL;
		else if (q < qF)
			return f.getPrice() + getLoLimit(f) * (q - qF);
		else
			return f.getPrice() + getHiLimit(f) * (q - qF);
	}

	private double getLoLimit(Firm f) {
		if (f == null)
			throw new Error("Firm value cannot be null");

		Firm loF = lower(f);

		if (loF == null)
			return Consumers.getMinMargUtilOfQuality();
		else
			return Offer.limitOperator(getOffer(loF), getOffer(f));

	}

	private double getHiLimit(Firm f) {
		if (f == null)
			throw new Error("Firm value cannot be null");

		Firm hiF = higher(f);

		if (hiF == null)
			return Double.POSITIVE_INFINITY;
		else
			return Offer.limitOperator(getOffer(f), getOffer(hiF));

	}

	private Firm lowerFirmGivenQ(double q) {

		return stream().filter(f -> f.getPerceivedQuality() >= q).findFirst()
				.orElse(null);

	}

	public double getMarginalProfitOfQuality(Firm f) {

		if (!contains(f))
			throw new Error("Firm should be in the market");

		double p = f.getPrice();
		double q = getQuality(f);
		double margCost = f.getMarginalCostOfQuality(q);
		double derivQ = demandDerivRespToQuality(f);

		return (p - f.unitCost(q)) * derivQ - margCost * getDemand(f);
	}

	public double getMarginalProfitOfPrice(Firm f) {

		if (!contains(f))
			throw new Error("Firm should be in the market");

		double p = f.getPrice();
		double q = getQuality(f);
		double derivP = demandDerivRespToPrice(f);

		return (p - f.unitCost(q)) * derivP + getDemand(f);
	}

	private double getDemand(Firm f) {

		if (!contains(f))
			return 0;
		else
			return expectedQuantity(f.getPrice(), getQuality(f), lower(f),
					higher(f));
		
	}

	private double demandDerivRespToQuality(Firm f) {

		if (!contains(f))
			throw new Error("Firm should be in the market");

		double lambda = Consumers.lambda;
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		double p = f.getPrice();
		double q = getQuality(f);

		Firm loF = lower(f);
		Firm hiF = higher(f);

		if (loF == null && hiF == null) {
			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q, lambda - 1.0) / Math.pow(p, lambda);

		} else if (loF == null && hiF != null) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			if (pH == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q, lambda - 1.0) / Math.pow(p, lambda) + Math
							.pow(qH - q, lambda - 1.0)
							/ Math.pow(pH - p, lambda));

		} else if (loF != null && hiF == null) {
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q - qL, lambda - 1.0) / Math.pow(p - pL, lambda);

		} else {
			// loF != null && hiF != null
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pH == p || pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q - qL, lambda - 1.0)
							/ Math.pow(p - pL, lambda) + Math.pow(qH - q,
							lambda - 1.0) / Math.pow(pH - p, lambda));

		}
	}

	private double demandDerivRespToPrice(Firm f) {

		if (!contains(f))
			throw new Error("Firm should be in the market");

		double lambda = Consumers.lambda;
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();

		double p = f.getPrice();
		double q = getQuality(f);

		Firm loF = lower(f);
		Firm hiF = higher(f);

		double poorest = Utils.getPoorestConsumerMargUtil(q, p);

		if (loF == null && hiF == null && minMargUtil > poorest) {
			// It has it all the market
			return 0.0;

		} else if (loF == null && hiF == null && poorest > minMargUtil) {
			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q, lambda) / Math.pow(p, lambda + 1.0);

		} else if (loF == null && hiF != null && minMargUtil > poorest) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(qH - q, lambda) / Math.pow(pH - p, lambda + 1.0);

		} else if (loF == null && hiF != null && poorest > minMargUtil) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();

			return -mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q, lambda) / Math.pow(p, lambda + 1.0) + Math
							.pow(qH - q, lambda)
							/ Math.pow(pH - p, lambda + 1.0));

		} else if (loF != null && hiF == null) {
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			if (pL == p)
				throw new Error(
						"Prices cannot be the same if both firms are in the market");

			return -mktSize * lambda * Math.pow(minMargUtil, lambda)
					* Math.pow(q - qL, lambda) / Math.pow(p - pL, lambda + 1.0);

		} else if (loF != null && hiF != null) {
			double pH = hiF.getPrice();
			double qH = hiF.getQuality();
			double pL = loF.getPrice();
			double qL = loF.getQuality();

			return -mktSize
					* lambda
					* Math.pow(minMargUtil, lambda)
					* (Math.pow(q - qL, lambda)
							/ Math.pow(p - pL, lambda + 1.0) + Math.pow(qH - q,
							lambda) / Math.pow(pH - p, lambda + 1.0));

		} else
			// It shouldn't come here
			return 0.0;
	}

}
