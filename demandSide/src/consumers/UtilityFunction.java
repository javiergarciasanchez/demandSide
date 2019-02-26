package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import demandSide.RecessionsHandler;
import firms.Firm;
import firms.Offer;

public class UtilityFunction {

	private static double utilityQualityExponent;

	/*
	 * Functions used by consumers
	 */

	public static void resetStaticVars() {
		utilityQualityExponent = (double) GetParameter("utilityQualityExponent");
	}

	/*
	 * Utility function used by consumers to choose firm the quality factor is
	 * discount factor applied to quality for choosing an untried firm
	 * 
	 * There is no need to introduce a marginal utility of money because it is
	 * implicit in the marginal utility of quality which depends on welfare param
	 * 
	 */
	public static double expectedUtility(double welfareParam, Offer o, double qualityFactor) {
		return welfareParam * powQuality(o.getQuality()) * qualityFactor - o.getPrice().doubleValue();
	}

	public static double realUtility(double welfareParam, Offer o) {
		// equal to expectedUtility except for the qualityFactor
		return welfareParam * powQuality(o.getQuality()) - o.getPrice().doubleValue();
	}

	private static double powQuality(BigDecimal quality) {
		return Math.pow(quality.doubleValue(), utilityQualityExponent);
	}

	/*
	 * Functions used by Firms
	 */
	public static BigDecimal getMaxPriceToEnter(Firm f, BigDecimal perceivedQ, Optional<Firm> loF, Optional<Firm> hiF) {

		BigDecimal loP, loQ, hiP, hiQ;

		if (!hiF.isPresent())
			return Consumers.getMaxPriceForRichestConsumer(f, perceivedQ);

		// note that null will never be assigned because hiF is present
		hiP = hiF.map(Firm::getPrice).orElse(null);
		hiQ = hiF.map(Firm::getQuality).orElse(null);

		loP = loF.map(Firm::getPrice).orElse(BigDecimal.ZERO);
		loQ = loF.map(Firm::getPerceivedQuality).orElse(BigDecimal.ZERO);

		if (hiQ.compareTo(loQ) <= 0)
			return BigDecimal.ZERO;
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(perceivedQ);
			double powLoQ = powQuality(loQ);
			double powHiQ = powQuality(hiQ);
			double hiPD = hiP.doubleValue();
			double loPD = loP.doubleValue();

			// maxPrice = (hiP * (powPercQ - powLoQ) + loP * (powHiQ - powPercQ)) / (powHiQ
			// - powLoQ)
			double retval = (hiPD * (powPercQ - powLoQ) + loPD * (powHiQ - powPercQ)) / (powHiQ - powLoQ);

			// It rounds downward to make sure firm would enter
			return BigDecimal.valueOf(retval).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);

		}

	}

	/*
	 * As Welfare param > p/powQ then p < welfare param * powQ
	 */
	static BigDecimal getMaxPriceForWelfareParam(BigDecimal quality, double welfareParam) {

		double retval = welfareParam * powQuality(quality);

		return BigDecimal.valueOf(retval).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);

	}

	/*
	 * (hiP - loP) / (powHiQ - powLoQ)
	 */
	public static double calculateRawLimit(BigDecimal loP, BigDecimal loQ, BigDecimal hiP, BigDecimal hiQ) {

		assert (loQ.compareTo(hiQ) < 0) && (loP.compareTo(hiP) < 0);

		double powLoQ = powQuality(loQ);
		double powHiQ = powQuality(hiQ);
		double hiPD = hiP.doubleValue();
		double loPD = loP.doubleValue();

		double rawWP = (hiPD - loPD) / (powHiQ - powLoQ);

		return FastMath.max(rawWP, Consumers.getMinRawWelfareParam()); 

	}

	/*
	 * Returns the price that combined with q, would expel firm f from above
	 * 
	 * Firm f would be expelled if the limit between the new offer and f's lower
	 * neighbor is less than current limit between f and f's lower neighbor
	 * 
	 * Returns empty if any price expels f
	 * 
	 */
	public static Optional<BigDecimal> priceToExpelFromAbove(BigDecimal q, Firm f, Optional<Firm> fLowNeighbor) {

		assert f != null;

		Optional<Offer> fLowNeighborOffer = fLowNeighbor.map(firm -> firm.getPerceivedOffer());
		Optional<Offer> fOffer = Optional.of(f.getPerceivedOffer());

		double fLowLimit = Consumers.limitingWelfareParamPerceivedByFirms(fLowNeighborOffer, fOffer);

		if (fLowLimit == Double.POSITIVE_INFINITY)
			// Any price expels f
			return Optional.empty();
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(f.getPerceivedQuality());
			double powQ = powQuality(q);
			double price = f.getPrice().doubleValue();

			// price + loLimit * (powQ - powPercQ)
			double retval = price + fLowLimit * (powQ - powPercQ);

			// It rounds downward to make sure firm would enter
			return Optional.of(BigDecimal.valueOf(retval).setScale(Offer.getPriceScale(), RoundingMode.FLOOR));

		}
	}

	/*
	 * Returns the price that combined with q, would expel firm f from below
	 * 
	 * Firm f would be expelled if the limit between the new offer and f's higher
	 * neighbor is greater than current limit between f and its higher neighbor
	 * 
	 * Returns Zero is all prices expel f
	 * 
	 */
	public static BigDecimal priceToExpelFromBelow(BigDecimal q, Firm f, Optional<Firm> fHighNeighbor) {

		Optional<Offer> fHighNeighborOffer = fHighNeighbor.map(firm -> firm.getPerceivedOffer());
		Optional<Offer> fOffer = Optional.of(f.getPerceivedOffer());

		double fHighLimit = Consumers.limitingWelfareParamPerceivedByFirms(fOffer, fHighNeighborOffer);

		if (fHighLimit == Double.POSITIVE_INFINITY)
			// No price expels f
			return BigDecimal.ZERO;
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(f.getPerceivedQuality());
			double powQ = powQuality(q);
			double price = f.getPrice().doubleValue();

			// price - hiLimit * (powPercQ - powQ)
			double retval = price - fHighLimit * (powPercQ - powQ);

			// It rounds downward to make sure firm would enter
			return BigDecimal.valueOf(retval).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);

		}

	}

	/*
	 * This depends on consumers utility functional form
	 */
	public static double getMinWelfareParamAceptingOfferPerceivedByFirms(Offer of) {

		assert of != null;

		double rawWP = of.getPrice().doubleValue() / powQuality(of.getQuality());

		rawWP = FastMath.max(rawWP, Consumers.getMinRawWelfareParam());
		
		return RecessionsHandler.getWelfareParamPerceivedByFirms(rawWP);

	}

}