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

		if (margUtil <= minMargUtilOfQuality)
			return mktSize;
		else if (margUtil == Double.POSITIVE_INFINITY)
			return 0;
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

		assert segOffer != null;

		double demandAboveLoLimit, demandAboveHiLimit;

		demandAboveLoLimit = getExpectedConsumersAbove(Offer.limit(segLowOffer, segOffer));
		demandAboveHiLimit = getExpectedConsumersAbove(Offer.limit(segOffer, segHighOffer));

		if (demandAboveLoLimit > demandAboveHiLimit)
			return demandAboveLoLimit - demandAboveHiLimit;
		else
			return 0;

	}

	public static double getLambda() {
		return lambda;
	}

	public static void setLambda(double lambda) {
		Consumers.lambda = lambda;
	}

	public static double deltaPrice(Offer loOf, Offer hiOf) {
		// low Offer could be null
		return hiOf.getPrice() - ((loOf == null) ? 0. : loOf.getPrice());
	}

	public static double deltaQuality(Offer loOf, Offer hiOf) {
		// low Offer could be null
		return hiOf.getQuality() - ((loOf == null) ? 0. : loOf.getQuality());
	}

}