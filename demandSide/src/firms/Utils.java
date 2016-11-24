package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumers;
import firmTypes.NoPrice;
import demandSide.Market;

public class Utils {


	public static double getMarginalProfitOfQuality(Firm firm) {
		// It is assumed firm is in the market
	
		double p = firm.getPrice();
		double q = firm.getQuality();
		double demand = firm.getDemand();
		double cost = firm.unitCost(q);
		double margCost = firm.getMarginalCostOfQuality(q);
		double derivQ = demandDerivRespToQuality(q, p);
	
		return (p - cost) * derivQ - margCost * demand;
	}

	private static double demandDerivRespToQuality(double q, double p) {
		// It is assumed firm is in the market
	
		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();
	
		Firm loF = Market.firms.getLowerLimitFirm(q);
		Firm hiF = Market.firms.getHigherLimitFirm(q);
	
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

	private static double demandDerivRespToPrice(double q, double p) {
		// It is assumed firm is in the market
	
		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);
		double minMargUtil = Consumers.getMinMargUtilOfQuality();
		double mktSize = Consumers.getNumberOfConsumers();
	
		double poorest = Utils.getPoorestConsumerMargUtil(q, p);
	
		Firm loF = Market.firms.getLowerLimitFirm(q);
		Firm hiF = Market.firms.getHigherLimitFirm(q);
	
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

	public static double getMarginalProfitOfPrice(Firm firm) {
		// It is assumed firm is in the market
	
		double p = firm.getPrice();
		double q = firm.getQuality();
		double demand = firm.getDemand();
		double cost = firm.unitCost(q);
		double derivP = demandDerivRespToPrice(q, p);
	
		return (p - cost) * derivP + demand;
	}



	public static void setMaximizingOffer(Firm f) {
		// It is assumed it is in the market

		DeltaOffer newDeltaOffer = DeltaOffer
				.createNormalizedMaximizingDirection(f);

		double newDeltaSize = DeltaOffer.getDefaultSize();
		if (DeltaOffer.oppositeDirections(f.getDeltaOffer(), newDeltaOffer))
			newDeltaSize = f.getDeltaOffer().getSize() / 2.0;

		newDeltaOffer.setSize(newDeltaSize);

		newDeltaOffer.addTo(f.getOffer());

	}

	/*
	 * it adds the new offer to history it removes the firm from FirmsByQ and
	 * adds it back to it
	 */
	public static void setNewRationalOffer(Firm f) {

		boolean wasInTheMarket = f.isInTheMarket();

		Market.firms.removeFromFirmsByQ(f);

		if (wasInTheMarket)
			Utils.setMaximizingOffer(f);
		else
			Utils.setReenteringOffer(f);

		Market.firms.addToFirmsLists(f);

	}
}
