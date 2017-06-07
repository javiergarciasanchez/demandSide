package improvingOffer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import firms.FirmsPerceivedQSegments;
import offer.DeltaOffer;
import offer.DeltaOfferSignCompare;
import offer.Offer;
import optimalPrice.NoPrice;
import optimalPrice.OptimalPrice;

public class ImprovingOffer {

	public static Offer get(Firm f) {

		FirmsPerceivedQSegments seg = new FirmsPerceivedQSegments();

		if (seg.contains(f))
			return getImprovingOffer(f, seg);
		else {
			return getReEntryOffer(f, seg);
		}

	}

	private static Offer getImprovingOffer(Firm f, FirmsPerceivedQSegments seg) {

		DeltaOffer deltaOffer = new DeltaOffer(0., 0.);

		Offer currRealOffer = f.getOffer();
		deltaOffer = getImprovingDeltaOffer(f, seg, currRealOffer);

		/*
		 * Check if after tentative steps any derivative changes sign If sign
		 * changes reduces step to one half
		 */
		DeltaOffer nextDeltaOffer = getImprovingDeltaOffer(f, seg, Offer.checkedAdd(f, currRealOffer, deltaOffer));
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

			nextDeltaOffer = getImprovingDeltaOffer(f, seg, Offer.checkedAdd(f, currRealOffer, deltaOffer));
			comp = DeltaOffer.deltaOfferCompare(deltaOffer, nextDeltaOffer);

		}

		return Offer.checkedAdd(f, currRealOffer, deltaOffer);

	}

	private static Offer getReEntryOffer(Firm f, FirmsPerceivedQSegments seg) {

		double perceivedQ = f.getPerceivedQuality();
		double realQ = f.getQuality();
		double cost = f.getUnitCost(realQ);

		// Choose a price to reenter
		double p;
		try {
			p = OptimalPrice.get(perceivedQ, cost, seg);

		} catch (NoPrice e) {

			// No price to reenter, keeps previous price
			p = f.getPrice();
		}

		return new Offer(p, perceivedQ);

	}

	private static DeltaOffer getImprovingDeltaOffer(Firm f, FirmsPerceivedQSegments seg, Offer realOffer) {

		MarginalProfit mgProf = getMarginalProfit(f, realOffer, seg);

		double signQ = FastMath.signum(mgProf.respectToQuality);

		double qStep = signQ * (Double) GetParameter("defaultQualityStep");

		/*
		 * Price step is chosen so that expected profit improvement with respect
		 * to price is equal to the one respect to quality
		 */
		double pStep = qStep * mgProf.respectToQuality / mgProf.respectToPrice;

		return new DeltaOffer(pStep, qStep);

	}

	private static MarginalProfit getMarginalProfit(Firm f, Offer realOffer, FirmsPerceivedQSegments seg) {

		// Cost and marginal cost depend always on real quality
		double realQ = realOffer.getQuality();
		double cost = f.getUnitCost(realQ);
		double marginalCost = f.getMarginalCostOfQuality(realQ);

		// Marginal profit and segment neighbors depend on segment quality
		double perceivedQ = f.getPerceivedQuality(realQ);
		Offer segOffer = new Offer(realOffer.getPrice(), perceivedQ);
		Offer loLimitOffer = Firm.getPerceivedOffer(seg.getLowerFirmGivenQ(perceivedQ));
		Offer hiLimitOffer = Firm.getPerceivedOffer(seg.getHigherFirmGivenQ(perceivedQ));

		MarginalProfit mgProf = new MarginalProfit(segOffer, cost, marginalCost, loLimitOffer, hiLimitOffer);

		return mgProf;
	}

}
