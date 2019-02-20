package consumers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import demandSide.RecessionsHandler;
import firms.Firm;
import firms.Offer;

public class UtilityFunction {

	/*
	 * Functions used by consumers
	 */

	/*
	 * Utility function used by consumers to choose firm the quality factor is
	 * discount factor applied to quality for choosing an untried firm
	 */
	public static double expectedUtility(double welfareParam, Offer o, double qualityFactor) {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality

		return welfareParam * o.getQuality().doubleValue() * qualityFactor - o.getPrice().doubleValue();
	}

	public static double realUtility(double welfareParam, Offer o) {
		// equal to expectedUtility except for the qualityFactor
		return welfareParam * o.getQuality().doubleValue() - o.getPrice().doubleValue();
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
			// maxPrice = (hiP * (percQ - loQ) + loP * (hiQ - percQ)) / (hiQ - loQ)
			BigDecimal num1 = hiP.multiply(perceivedQ.subtract(loQ));
			BigDecimal num2 = loP.multiply(hiQ.subtract(perceivedQ));
			BigDecimal num = num1.add(num2);
			BigDecimal denom = hiQ.subtract(loQ);

			// It rounds downward to make sure firm would enter
			return num.divide(denom, Offer.getPriceScale(), RoundingMode.FLOOR);
		}

	}

	/*
	 * As Welfare param > p/q
	 * 
	 * then p < minMargUtil * q
	 */
	static BigDecimal getMaxPriceForWelfareParam(BigDecimal quality, double welfareParam) {
	
		BigDecimal wP = BigDecimal.valueOf(welfareParam);
	
		return quality.multiply(wP).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);
	
	}

	public static double calculateLimit(BigDecimal loP, BigDecimal loQ, BigDecimal hiP, BigDecimal hiQ) {

		assert (loQ.compareTo(hiQ) < 0) && (loP.compareTo(hiP) < 0);

		BigDecimal deltaP = hiP.subtract(loP);
		BigDecimal deltaQ = hiQ.subtract(loQ);

		double limit = deltaP.doubleValue() / deltaQ.doubleValue();

		return FastMath.max(limit, RecessionsHandler.getMinWelfareParamPerceivedByFirms());

	}

	/*
	 * Returns the price that combined with q, would expel firm f from above (ie q
	 * higher f' perceived q)
	 * 
	 * Firm f would be expelled if the limit between the new offer and f's lower
	 * neighbor is less than current limit between f and its lower neighbor
	 * 
	 */
	public static BigDecimal priceToExpelFromAbove(BigDecimal q, Firm f, Optional<Firm> fLowNeighbor) {

		assert f != null;

		Optional<Offer> fLowNeighborOffer = fLowNeighbor.map(firm -> firm.getPerceivedOffer());
		Optional<Offer> fOffer = Optional.of(f.getPerceivedOffer());
		double fLowLimit = Consumers.limitingWelfareParamPerceivedByFirms(fLowNeighborOffer, fOffer);

		if (fLowLimit == Double.POSITIVE_INFINITY)
			// Any price expels f
			return null;
		else
			// This depends on Consumers utility functional form
			// price + loLimit * (q - percQ)
			return f.getPrice().add(BigDecimal.valueOf(fLowLimit).multiply(q.subtract(f.getPerceivedQuality())));
	}

	/*
	 * Returns the price that combined with q, would expel firm f from below (ie q
	 * is less than f' perceived q)
	 * 
	 * Firm f would be expelled if the limit between the new offer and f's higher
	 * neighbor is greater than current limit between f and its higher neighbor
	 * 
	 */
	public static BigDecimal priceToExpelFromBelow(BigDecimal q, Firm f, Optional<Firm> fHighNeighbor) {

		Optional<Offer> fHighNeighborOffer = fHighNeighbor.map(firm -> firm.getPerceivedOffer());
		Optional<Offer> fOffer = Optional.of(f.getPerceivedOffer());

		double fHighLimit = Consumers.limitingWelfareParamPerceivedByFirms(fOffer, fHighNeighborOffer);

		if (fHighLimit == Double.POSITIVE_INFINITY)
			// No price expels f
			return BigDecimal.ZERO;
		else
			// This depends on Consumers utility functional form
			// price - hiLimit * (percQ - q)
			return f.getPrice().subtract(BigDecimal.valueOf(fHighLimit).multiply(f.getPerceivedQuality().subtract(q)));
	}

	/*
	 * This depends on consumers utility functional form
	 */
	public static double getMinWelfareParamAceptingOfferPerceivedByFirms(Offer of) {

		assert of != null;

		double pDivQ = of.getPrice().doubleValue() / of.getQuality().doubleValue();
		return FastMath.max(pDivQ, RecessionsHandler.getMinWelfareParamPerceivedByFirms());

	}

}