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

	private static double minExpectedDemand;

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
		mktSize = (int) GetParameter("numberOfConsumers");
		minExpectedDemand = (double) GetParameter("minimumExpectedDemand");
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

	public static long getExpectedConsumersAbove(double margUtilQuality) {

		if (margUtilQuality <= minMargUtilOfQuality)
			return mktSize;
		else if (margUtilQuality == Double.POSITIVE_INFINITY)
			return 0;
		else
			return Math.round(mktSize * FastMath.pow(minMargUtilOfQuality / margUtilQuality, lambda));

	}

	public long getConsumersAbove(double margUtilQuality) {
		if (margUtilQuality == Double.POSITIVE_INFINITY)
			return 0;
		else
			return this.stream().filter(c -> c.getMargUtilOfQuality() > margUtilQuality).count();
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

	public long getExpectedQuantityWExpecDistrib(Offer segOffer, Optional<Offer> loOffer, Optional<Offer> hiOffer) {

		long demandAboveLoLimit, demandAboveHiLimit;

		demandAboveLoLimit = getExpectedConsumersAbove(Offer.limit(loOffer, Optional.of(segOffer)));
		demandAboveHiLimit = getExpectedConsumersAbove(Offer.limit(Optional.of(segOffer), hiOffer));

		if (demandAboveLoLimit > demandAboveHiLimit)
			return demandAboveLoLimit - demandAboveHiLimit;
		else
			return 0;

	}

	public long getExpectedQuantityWRealDistrib(Offer segOffer, Optional<Offer> loOffer, Optional<Offer> hiOffer) {

		long demandAboveLoLimit, demandAboveHiLimit;

		demandAboveLoLimit = getConsumersAbove(Offer.limit(loOffer, Optional.of(segOffer)));
		demandAboveHiLimit = getConsumersAbove(Offer.limit(Optional.of(segOffer), hiOffer));

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
		 * It is assumed poorest consumer has margUtil = minMargUtil As margUtil > p/q p
		 * < minMargUtil * q
		 */
		BigDecimal minMargUtil = BigDecimal.valueOf(getMinMargUtilOfQuality());

		return quality.multiply(minMargUtil).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);

	}

	public static BigDecimal getMaxPriceToHaveMinimumExpectedDemand(BigDecimal perceivedQ, double knownByPerc) {

		// The price is calculated for the firm with highest quality, i.e. with no
		// competing firm from above.
		// Other firms will have a lower max price, but as it cannot be calculated
		// analytically the restriction is introduced in the numerical maximization
		double maxP = minMargUtilOfQuality * perceivedQ.doubleValue()
				* FastMath.pow(mktSize * knownByPerc / minExpectedDemand, 1.0 / lambda);

		return BigDecimal.valueOf(maxP).setScale(Offer.getPriceScale(), RoundingMode.FLOOR);
	}

	public static double getLambda() {
		return lambda;
	}

	public static void setLambda(double lambda) {
		Consumers.lambda = lambda;
	}

	public static double getMinExpectedDemand() {
		return minExpectedDemand;
	}

	public static void setMinExpectedDemand(double minExpectedDemand) {
		Consumers.minExpectedDemand = minExpectedDemand;
	}

}