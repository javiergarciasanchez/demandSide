package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import org.apache.commons.math3.util.FastMath;

import cern.jet.random.Beta;
import offer.Offer;
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

	public static double getExpectedConsumersAbove(double margUtil) {

		if (margUtil < minMargUtilOfQuality)
			return mktSize;
		else
			return mktSize * FastMath.pow(minMargUtilOfQuality / margUtil, lambda);

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

	public static double expectedQuantity(Offer segOffer, Offer segLowOffer, Offer segHighOffer) {

		double loLimit, hiLimit;

		if (segLowOffer == null)
			loLimit = getMinMargUtilOfQuality();
		else
			loLimit = Consumers.limit(segLowOffer, segOffer);

		if (segHighOffer == null)
			return getExpectedConsumersAbove(loLimit);

		else {
			hiLimit = Consumers.limit(segOffer, segHighOffer);
			
			if (loLimit < hiLimit)
				return getExpectedConsumersAbove(loLimit) - getExpectedConsumersAbove(hiLimit);
			else
				return 0.0;

		}

	}

	/*
	 * Calculates the marginal utility of quality that divides consumer
	 * preferences. Consumers with a marginal utility of quality (muq) below
	 * "limit" will choose loOffer, while the ones with higher (muq) would
	 * choose hiOffer
	 */
	public static double limit(Offer loOffer, Offer hiOffer) {

		if (loOffer == null)
			return Consumers.getMinMargUtilOfQuality();

		if (hiOffer == null)
			return Double.POSITIVE_INFINITY;

		double loQ, hiQ;
		loQ = loOffer.getQuality();
		hiQ = hiOffer.getQuality();

		if (loQ >= hiQ)
			throw new Error("loOffer should have lower quality than hiOffer");

		double loP, hiP;
		loP = loOffer.getPrice();
		hiP = hiOffer.getPrice();

		if (hiP < loP)
			return Consumers.getMinMargUtilOfQuality();
		else
			return FastMath.max((hiP - loP) / (hiQ - loQ), Consumers.getMinMargUtilOfQuality());

	}

	public static double getLambda() {
		return lambda;
	}

	public static void setLambda(double lambda) {
		Consumers.lambda = lambda;
	}

}