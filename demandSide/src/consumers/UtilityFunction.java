package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import demandSide.RecessionsHandler;
import firms.Firm;
import firms.Offer;

public class UtilityFunction {

	public static double realUtility(double welfareParam, double price, double quality) {
		return welfareParam * powQuality(quality) - price;
	}
	
	private static double powQuality(double quality) {

		double utilityQualityExponent = (double) GetParameter("utilityQualityExponent");
		
		return Math.pow(quality, utilityQualityExponent);
	}

	/*
	 * Functions used by Firms
	 */
	public static double getMaxPriceToEnter(Firm firm, double perceivedQ, Optional<Firm> loF, Optional<Firm> hiF) {
		
		Consumers consumers = firm.market.consumers;
		
		double loP, loQ, hiP, hiQ;
		Optional<Offer> hiOf, loOf;

		if (hiF.isEmpty())
			return consumers.getMaxPriceForRichestConsumer(firm, perceivedQ);

		// note that null will never be assigned because hiF is present
		hiOf = hiF.map(f -> firm.getCompetitorPerceivedOffer(f));
		hiP = hiOf.map(Offer::getPrice).orElse(null);
		hiQ = hiOf.map(Offer::getQuality).orElse(null);

		loOf = loF.map(f->firm.getCompetitorPerceivedOffer(f));
		loP = loOf.map(Offer::getPrice).orElse(0.);
		loQ = loOf.map(Offer::getQuality).orElse(0.);

		if (hiQ <= loQ)
			return 0.0;
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(perceivedQ);
			double powLoQ = powQuality(loQ);
			double powHiQ = powQuality(hiQ);
			double hiPD = hiP;
			double loPD = loP;

			// maxPrice = (hiP * (powPercQ - powLoQ) + loP * (powHiQ - powPercQ)) / (powHiQ
			// - powLoQ)
			return (hiPD * (powPercQ - powLoQ) + loPD * (powHiQ - powPercQ)) / (powHiQ - powLoQ);

		}

	}

	/*
	 * As Welfare param > p/powQ then p < welfare param * powQ
	 */
	static double getMaxPriceForWelfareParam(double perceivedQ, double welfareParam) {

		return welfareParam * powQuality(perceivedQ);

	}

	/*
	 * (hiP - loP) / (powHiQ - powLoQ)
	 */
	public static double calculateRawLimit(double loP, double loQ, double hiP, double hiQ) {

		assert ((loQ <hiQ) && (loP < hiP));

		double powLoQ = powQuality(loQ);
		double powHiQ = powQuality(hiQ);
		double hiPD = hiP;
		double loPD = loP;

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
	public static Optional<Double> priceToExpelFromAbove(double perceivedQ, Firm owner, Firm f, Optional<Firm> fLowNeighbor) {

		assert f != null;
		
		Consumers consumers = f.market.consumers;

		Optional<Offer> fLowNeighborOffer = fLowNeighbor.map(firm -> owner.getCompetitorPerceivedOffer(firm));
		Offer fOffer = owner.getCompetitorPerceivedOffer(f);		

		double fLowLimit = consumers.limitingWelfareParamPerceivedByFirms(fLowNeighborOffer, Optional.of(fOffer));

		if (fLowLimit == Double.POSITIVE_INFINITY)
			// Any price expels f
			return Optional.empty();
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(fOffer.getQuality());
			double powQ = powQuality(perceivedQ);
			double price = fOffer.getPrice();

			// price + loLimit * (powQ - powPercQ)
			double retval = price + fLowLimit * (powQ - powPercQ);
			return Optional.of(retval);

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
	public static double priceToExpelFromBelow(double perceivedQ, Firm owner, Firm f, Optional<Firm> fHighNeighbor) {

		Consumers consumers = owner.market.consumers;
		
		Optional<Offer> fHighNeighborOffer = fHighNeighbor.map(firm -> owner.getCompetitorPerceivedOffer(firm));
		Offer fOffer = owner.getCompetitorPerceivedOffer(f);
				
		double fHighLimit = consumers.limitingWelfareParamPerceivedByFirms(Optional.of(fOffer), fHighNeighborOffer);

		if (fHighLimit == Double.POSITIVE_INFINITY)
			// No price expels f
			return 0.0;
		else {
			// This depends on Consumers utility functional form

			// Calculation is done on double because bigdecimal does not support pow to
			// double
			double powPercQ = powQuality(fOffer.getQuality());
			double powQ = powQuality(perceivedQ);
			double price = fOffer.getPrice();

			// price - hiLimit * (powPercQ - powQ)
			return price - fHighLimit * (powPercQ - powQ);

		}

	}

	/*
	 * This depends on consumers utility functional form
	 */
	public static double getMinWelfareParamAceptingOfferPerceivedByFirms(Offer of) {

		assert of != null;

		double rawWP = of.getPrice() / powQuality(of.getQuality());

		rawWP = FastMath.max(rawWP, Consumers.getMinRawWelfareParam());
		
		return RecessionsHandler.getWelfareParamPerceivedByFirms(rawWP);

	}

}