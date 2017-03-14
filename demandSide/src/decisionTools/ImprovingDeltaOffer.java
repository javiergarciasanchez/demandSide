package decisionTools;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import firms.FirmsSegments;
import offer.DeltaOffer;
import offer.DeltaOfferSignCompare;
import offer.Offer;

public class ImprovingDeltaOffer {

	/*
	 * Creates a vector that improves profit, using MarginalProfit
	 */
	public static DeltaOffer get(Firm f, FirmsSegments seg) {
		DeltaOffer deltaOffer = new DeltaOffer(0., 0.);

		Offer currOffer = f.getOffer();
		deltaOffer = getImprovingDeltaOffer(f, seg, currOffer);

		/*
		 * Check if after tentative steps any derivative changes sign If sign
		 * changes reduces step to one half
		 */
		DeltaOffer nextDeltaOffer = getImprovingDeltaOffer(f, seg,  Offer.checkedAdd(f, currOffer, deltaOffer));
		DeltaOfferSignCompare comp = DeltaOffer.deltaOfferCompare(deltaOffer, nextDeltaOffer);

		while (comp != DeltaOfferSignCompare.BOTH_EQUAL) {

			switch (comp) {
			case BOTH_UNEQUAL:
				deltaOffer.setDeltaPrice(deltaOffer.getDeltaPrice() / 2.);
				deltaOffer.setDeltaQuality(deltaOffer.getDeltaQuality() / 2.);
				break;
			case UNEQUAL_PRICE:
				deltaOffer.setDeltaPrice(deltaOffer.getDeltaPrice() / 2.);
				break;
			case UNEQUAL_QUALITY:
				deltaOffer.setDeltaQuality(deltaOffer.getDeltaQuality() / 2.);
				break;
			case BOTH_EQUAL:
				break;
			}

			nextDeltaOffer = getImprovingDeltaOffer(f, seg,  Offer.checkedAdd(f, currOffer, deltaOffer));
			comp = DeltaOffer.deltaOfferCompare(deltaOffer, nextDeltaOffer);

		}

		return deltaOffer;

	}

	private static DeltaOffer getImprovingDeltaOffer(Firm f, FirmsSegments seg, Offer offer) {

		MarginalProfit mgProf = getMarginalProfit(f, offer, seg);

		double signQ = FastMath.signum(mgProf.respectToQuality);

		double qStep = signQ * (Double) GetParameter("defaultQualityStep");

		/*
		 * Price step is chosen so that expected profit improvement with respect
		 * to price is equal to the one respect to quality
		 */
		double pStep = qStep * mgProf.respectToQuality / mgProf.respectToPrice;

		return new DeltaOffer(pStep, qStep);

	}

	private static MarginalProfit getMarginalProfit(Firm f, Offer realOffer, FirmsSegments seg) {

		// Cost and marginal cost depend always on real quality
		double realQ = realOffer.getQuality();
		double cost = f.getUnitCost(realQ);
		double marginalCost = f.getMarginalCostOfQuality(realQ);

		// Marginal profit and segment neighbors depend on segment quality
		double segQ = seg.getQuality(f, realQ);
		Offer segOffer = new Offer(realOffer.getPrice(), segQ);
		Offer loLimitOffer = seg.getOffer(seg.getLowerFirmGivenQ(segQ));
		Offer hiLimitOffer = seg.getOffer(seg.getHigherFirmGivenQ(segQ));

		MarginalProfit mgProf = new MarginalProfit(segOffer, cost, marginalCost, loLimitOffer, hiLimitOffer);

		return mgProf;
	}
	
}
