package decisionTools;

import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import offer.Offer;

public class MarginalProfit {
	double respectToPrice, respectToQuality;

	public MarginalProfit(Offer of, double cost, double margCost, Offer loLimitOffer, Offer hiLimitOffer) {

		double demand = Consumers.expectedQuantity(of, loLimitOffer, hiLimitOffer);

		double p = of.getPrice();

		respectToPrice = (p - cost) * marginalDemandRespectToPrice(of, loLimitOffer, hiLimitOffer) + demand;
		respectToQuality = (p - cost) * marginalDemandRespectToQuality(of, loLimitOffer, hiLimitOffer)
				- margCost * demand;

	}

	public static double respectToPrice(Offer of, double cost, Offer loLimitOffer, Offer hiLimitOffer) {

		double demand = Consumers.expectedQuantity(of, loLimitOffer, hiLimitOffer);
		double mgDemand = marginalDemandRespectToPrice(of, loLimitOffer, hiLimitOffer);

		return (of.getPrice() - cost) * mgDemand + demand;

	}

	public static double respectToPriceTwice(Offer of, double cost, Offer loLimitOffer, Offer hiLimitOffer) {

		double mgDemand = marginalDemandRespectToPrice(of, loLimitOffer, hiLimitOffer);
		double mgDemandRespectoToPriceTwice = marginalDemandRespectToPriceTwice(of, loLimitOffer, hiLimitOffer);

		return 2 * mgDemand + (of.getPrice() - cost) * mgDemandRespectoToPriceTwice;

	}

	public static double respectToQuality(Offer of, double cost, double margCost, Offer loLimitOffer,
			Offer hiLimitOffer) {

		double demand = Consumers.expectedQuantity(of, loLimitOffer, hiLimitOffer);
		double mgDemand = marginalDemandRespectToQuality(of, loLimitOffer, hiLimitOffer);

		return (of.getPrice() - cost) * mgDemand - margCost * demand;

	}

	private static double marginalDemandRespectToPrice(Offer of, Offer loLimitOffer, Offer hiLimitOffer) {

		double deltaLowPrice = deltaLowPrice(of, loLimitOffer);
		double deltaLowQuality = deltaLowQuality(of, loLimitOffer);

		double demandLow = Consumers.getExpectedConsumersAbove(deltaLowPrice / deltaLowQuality);

		if (hiLimitOffer == null) {

			return -Consumers.getLambda() * demandLow / deltaLowPrice;

		} else {

			double deltaHighPrice = hiLimitOffer.getPrice() - of.getPrice();
			double deltaHighQuality = hiLimitOffer.getQuality() - of.getQuality();

			double demandHigh = Consumers.getExpectedConsumersAbove(deltaHighPrice / deltaHighQuality);

			return -Consumers.getLambda() * (demandLow / deltaLowPrice + demandHigh / deltaHighPrice);

		}

	}

	private static double marginalDemandRespectToPriceTwice(Offer of, Offer loLimitOffer, Offer hiLimitOffer) {

		double deltaLowPrice = deltaLowPrice(of, loLimitOffer);
		double deltaLowPriceSqr = FastMath.pow(deltaLowPrice, 2);
		double deltaLowQuality = deltaLowQuality(of, loLimitOffer);
				
		double lambda = Consumers.getLambda();

		double demandLow = Consumers.getExpectedConsumersAbove(deltaLowPrice / deltaLowQuality);

		if (hiLimitOffer == null) {

			return lambda * (lambda + 1) * demandLow / deltaLowPriceSqr;

		} else {

			double deltaHighPrice = hiLimitOffer.getPrice() - of.getPrice();
			double deltaHighPriceSqr = FastMath.pow(deltaHighPrice, 2);
			double deltaHighQuality = hiLimitOffer.getQuality() - of.getQuality();
			
			double demandHigh = Consumers.getExpectedConsumersAbove(deltaHighPrice / deltaHighQuality);

			return lambda * (lambda + 1) * (demandLow / deltaLowPriceSqr - demandHigh / deltaHighPriceSqr);

		}

	}


	private static double marginalDemandRespectToQuality(Offer of, Offer loLimitOffer, Offer hiLimitOffer) {

		double deltaLowPrice = deltaLowPrice(of, loLimitOffer);
		double deltaLowQuality = deltaLowQuality(of, loLimitOffer);

		double demandLow = Consumers.getExpectedConsumersAbove(deltaLowPrice / deltaLowQuality);

		if (hiLimitOffer == null) {

			return -Consumers.getLambda() * demandLow / deltaLowQuality;

		} else {

			double deltaHighPrice = hiLimitOffer.getPrice() - of.getPrice();
			double deltaHighQuality = hiLimitOffer.getQuality() - of.getQuality();

			double hiLimit = deltaHighPrice / deltaHighQuality;
			double demandHigh = Consumers.getExpectedConsumersAbove(hiLimit);

			return Consumers.getLambda() * (demandLow / deltaLowQuality + demandHigh / deltaHighQuality);

		}

	}

	private static double deltaLowPrice(Offer of, Offer loLimitOffer) {
		return of.getPrice() - ((loLimitOffer == null) ? 0. : loLimitOffer.getPrice());
	}

	private static double deltaLowQuality(Offer of, Offer loLimitOffer) {
		return of.getQuality() - ((loLimitOffer == null) ? 0. : loLimitOffer.getQuality());
	}

}
