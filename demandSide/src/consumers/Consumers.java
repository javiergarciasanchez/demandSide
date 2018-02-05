package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import cern.jet.random.Beta;
import firms.Offer;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;

public class Consumers extends DefaultContext<Consumer> {

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private static double minMargUtilOfQuality;
	private static double maxMargUtilOfQuality;
	private static double lambda;

	private static Pareto margUtilOfQualityDistrib;
	private static Beta qualityDiscountDistrib;
	private static int mktSize;

	public static void resetStaticVars() {
		minMargUtilOfQuality = 0.0;
		maxMargUtilOfQuality = 0.0;
		margUtilOfQualityDistrib = null;
		qualityDiscountDistrib = null;
		double gini = (double) GetParameter("gini");
		lambda = (1.0 + gini) / (2.0 * gini);
		mktSize = (Integer) GetParameter("numberOfConsumers");
	}

	public Consumers() {
		super("Consumers_Context");

		minMargUtilOfQuality = (double) GetParameter("minMargUtilOfQuality");

		// Max marginal utility is updated every time a consumer is created
		maxMargUtilOfQuality = 0.;

		createProbabilityDistrib();

	}

	private static void createProbabilityDistrib() {

		// Marginal Utility of Quality
		margUtilOfQualityDistrib = Pareto.getPareto(lambda, getMinMargUtilOfQuality());

		// Quality Discount
		double mean = (Double) GetParameter("qualityDiscountMean");
		double mode = (Double) GetParameter("qualityDiscountMostLikely");
		double alpha = mean * (1 - 2 * mode) / (mean - mode);
		double beta = alpha * (1 - mean) / mean;
		qualityDiscountDistrib = RandomHelper.createBeta(alpha, beta);

	}

	public static Pareto getMargUtilOfQualityDistrib() {
		return margUtilOfQualityDistrib;
	}

	public static Beta getQualityDicountDistrib() {
		return qualityDiscountDistrib;
	}

	public static double getExpectedConsumersAbove(Double margUtilQuality) {

		if (margUtilQuality <= minMargUtilOfQuality)
			return mktSize;
		else if (margUtilQuality == Double.POSITIVE_INFINITY)
			return 0;
		else
			return mktSize * FastMath.pow(minMargUtilOfQuality / margUtilQuality, lambda);

	}

	public static double getMinMargUtilOfQuality() {
		return minMargUtilOfQuality;
	}

	public static double getMaxMargUtilOfQuality() {
		return maxMargUtilOfQuality;
	}

	public static int getMarketSize() {
		return mktSize;
	}

	public static void setMaxMargUtilOfQuality(double maxMargUtilOfQuality) {
		Consumers.maxMargUtilOfQuality = maxMargUtilOfQuality;
	}

	public static void createConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer();
		}

	}

	public static double expectedQuantity(Offer segOffer, Optional<Offer> loOffer, Optional<Offer> hiOffer) {

		double demandAboveLoLimit, demandAboveHiLimit;

		demandAboveLoLimit = getExpectedConsumersAbove(Offer.limit(loOffer, Optional.of(segOffer)));
		demandAboveHiLimit = getExpectedConsumersAbove(Offer.limit(Optional.of(segOffer), hiOffer));

		if (demandAboveLoLimit > demandAboveHiLimit)
			return demandAboveLoLimit - demandAboveHiLimit;
		else
			return 0;

	}

	public static double expectedQuantity(Double p, Double q, Optional<Offer> loOf, Optional<Offer> hiOf) {

		assert ((p != null) && (q != null));

		Double loP, loQ, hiP, hiQ;
		double loLimit, hiLimit;
		double demandAboveLoLimit, demandAboveHiLimit;

		if (loOf.isPresent()) {
			loP = loOf.get().getPrice().doubleValue();
			loQ = loOf.get().getQuality().doubleValue();
		} else {
			loP = null;
			loQ = null;
		}
		
		loLimit = Offer.limit(loP, loQ, p, q);
		demandAboveLoLimit = getExpectedConsumersAbove(loLimit);
		
		if (hiOf.isPresent()) {
			hiP = hiOf.get().getPrice().doubleValue();
			hiQ = hiOf.get().getQuality().doubleValue();
		} else {
			hiP = null;
			hiQ = null;
		}
		hiLimit = Offer.limit(p, q, hiP, hiQ);
		demandAboveHiLimit = getExpectedConsumersAbove(hiLimit);

		if (demandAboveLoLimit > demandAboveHiLimit)
			return demandAboveLoLimit - demandAboveHiLimit;
		else
			return 0;

	}

	public static double getMinMargUtilOfQualityAceptingOffer(Offer of) {
		if (of == null)
			throw new Error("Offer should not be null");
		else {
			double pDivQ = of.getPrice().doubleValue() / of.getQuality().doubleValue();
			return FastMath.max(pDivQ, getMinMargUtilOfQuality());
		}
	}

	public static BigDecimal getMaxPriceForPoorestConsumer(BigDecimal quality) {
		/*
		 * It is assumed poorest consumer has margUtil = minMargUtil As margUtil
		 * > p/q p < minMargUtil * q
		 */
		BigDecimal minMargUtil = BigDecimal.valueOf(getMinMargUtilOfQuality());

		return quality.multiply(minMargUtil).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);

	}

	public static double getLambda() {
		return lambda;
	}

	public static void setLambda(double lambda) {
		Consumers.lambda = lambda;
	}

}