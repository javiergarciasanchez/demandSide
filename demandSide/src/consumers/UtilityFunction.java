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
	 * Utility function used by consumers to choose firm
	 * the quality factor is discount factor applied to quality for choosing an untried firm 
	 */
	public static double expectedUtility(double welfareParam, Offer o, double qualityFactor) {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality

		return welfareParam * o.getQuality().doubleValue() * qualityFactor - o.getPrice().doubleValue();
	}
	
	/*
	 * This function is for probe purposes only
	 */
	public static double realUtility(double welfareParam, Offer o) {
		// equal to expectedUtility except for the qualityFactor
		return welfareParam * o.getQuality().doubleValue()  - o.getPrice().doubleValue();
	}

	
	
	/*
	 * Functions used by Firms
	 */
	public static BigDecimal getMaxPriceToEnter(BigDecimal perceivedQ, Optional<Firm> loF, Optional<Firm> hiF) {

		BigDecimal loP, loQ, hiP, hiQ;

		if (!hiF.isPresent())
			return Offer.getMaxPrice();

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

	/*
	 * This depends on consumers utility functional form
	 */
	public static BigDecimal getMaxPriceForPoorestConsumer(BigDecimal quality) {
		/*
		 * It is assumed poorest consumer has margUtil = minMargUtil As margUtil > p/q p
		 * < minMargUtil * q
		 */
		BigDecimal minMargUtil = BigDecimal.valueOf(RecessionsHandler.getMinWelfareParamPerceivedByFirms());
	
		return quality.multiply(minMargUtil).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);
	
	}

	/*
	 * This depends on consumers utility functional form
	 */
	public static BigDecimal getMaxPriceToHaveMinimumExpectedDemand(BigDecimal perceivedQ, double knownByPerc) {
	
		// The price is calculated for the firm with highest quality, i.e. with no
		// competing firm from above.
		// Other firms will have a lower max price, but as it cannot be calculated
		// analytically the restriction is introduced in the numerical maximization
		double minWefParam = RecessionsHandler.getMinWelfareParamPerceivedByFirms();
		double mktSize = Consumers.getMarketSize();
		double minExpectedDemand = Consumers.getMinExpectedDemand();
		double lambda = Consumers.getLambda();
		
		double maxP = minWefParam * perceivedQ.doubleValue()
				* FastMath.pow(mktSize * knownByPerc / minExpectedDemand, 1.0 / lambda);
	
		return BigDecimal.valueOf(maxP).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);
	}

}