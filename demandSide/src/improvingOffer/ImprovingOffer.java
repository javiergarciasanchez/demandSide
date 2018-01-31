package improvingOffer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import org.apache.commons.math3.util.FastMath;

import firms.ExpectedMarket;
import firms.Firm;
import offer.DeltaOffer;
import offer.Offer;
import optimalPrice.NoPrice;
import optimalPrice.OptimalPrice;

public class ImprovingOffer {

	public static Offer get(Firm f, DeltaOffer deltaOffer) {
		ExpectedMarket expMkt = new ExpectedMarket(f);
		MarginalProfit mgProf = getMarginalProfit(f, expMkt);

		BigDecimal pStep = getPriceStep(mgProf, deltaOffer);
		BigDecimal qStep = getQualityStep(mgProf, deltaOffer);

		BigDecimal currP = f.getPrice();
		BigDecimal currQ = f.getQuality();
		BigDecimal nextP, nextQ;

		if (productNeedsModification(mgProf, pStep, qStep)) {

			if (qStep.compareTo(BigDecimal.ZERO) > 0)
				nextQ = Offer.getUpWardClosestAvailableQuality(currQ.add(qStep));
			else
				nextQ = Offer.getDownWardClosestAvailableQuality(currQ.add(qStep));

			// Keep previous quality
			if (nextQ == null)
				nextQ = currQ;

			try {
				nextP = OptimalPrice.get(f.getPerceivedQuality(nextQ), f.getUnitCost(nextQ), expMkt);
			} catch (NoPrice e) {
				// Keep previous price
				nextP = currP;
			}

		} else {
			nextP = currP.add(pStep).max(Offer.getMinPrice(f, currQ));
			nextQ = currQ;
		}

		return new Offer(nextP, nextQ);

	}

	private static BigDecimal getQualityStep(MarginalProfit mgProf, DeltaOffer deltaOffer) {
		BigDecimal retval;

		BigDecimal signMgProf = BigDecimal.valueOf(FastMath.signum(mgProf.respectToQuality));

		if (deltaOffer.getDeltaQuality().multiply(signMgProf).compareTo(BigDecimal.ZERO) < 0)
			retval = deltaOffer.getDeltaQuality().divide(BigDecimal.valueOf(-2), Offer.getQualityScale(),
					Offer.getQualityRounding());
		else {
			retval = signMgProf.multiply(BigDecimal.valueOf((Double) GetParameter("defaultQualityStep")));
			retval = retval.setScale(Offer.getQualityScale(), Offer.getQualityRounding());
		}

		return retval;
	}

	private static BigDecimal getPriceStep(MarginalProfit mgProf, DeltaOffer deltaOffer) {
		BigDecimal retval;

		BigDecimal signMgProf = BigDecimal.valueOf(FastMath.signum(mgProf.respectToPrice));

		if (deltaOffer.getDeltaPrice().multiply(signMgProf).compareTo(BigDecimal.ZERO) < 0)
			retval = deltaOffer.getDeltaPrice().divide(BigDecimal.valueOf(-2), Offer.getPriceScale(),
					Offer.getPriceRounding());
		else {
			retval = signMgProf.multiply(BigDecimal.valueOf((Double) GetParameter("defaultPriceStep")));
			retval = retval.setScale(Offer.getPriceScale(), Offer.getPriceRounding());
		}

		return retval;
	}

	private static boolean productNeedsModification(MarginalProfit mgProf, BigDecimal pStep, BigDecimal qStep) {

		double qThreshold = (Double) GetParameter("qualityThreshold");
		return qThreshold > (mgProf.respectToQuality * qStep.doubleValue())
				/ (mgProf.respectToPrice * pStep.doubleValue());

	}

	private static MarginalProfit getMarginalProfit(Firm f, ExpectedMarket expMkt) {

		Offer realOffer = f.getOffer();

		// Cost and marginal cost depend always on real quality
		BigDecimal realQ = realOffer.getQuality();
		double cost = f.getUnitCost(realQ);
		double marginalCost = f.getMarginalCostOfQuality(realQ);

		// Marginal profit and segment neighbors depend on segment quality
		BigDecimal perceivedQ = f.getPerceivedQuality(realQ);
		Offer perceivedOffer = new Offer(realOffer.getPrice(), perceivedQ);
		Offer loLimitOffer = Firm.getPerceivedOffer(expMkt.getLowerFirmGivenQ(perceivedQ));
		Offer hiLimitOffer = Firm.getPerceivedOffer(expMkt.getHigherFirmGivenQ(perceivedQ));

		MarginalProfit mgProf = new MarginalProfit(perceivedOffer, cost, marginalCost, loLimitOffer, hiLimitOffer);

		return mgProf;
	}

}
