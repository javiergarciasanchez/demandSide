package consumers;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import cern.jet.random.Beta;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import demandSide.Market;
import demandSide.RecessionsHandler;
import firms.Firm;
import firms.Offer;
import repast.simphony.context.DefaultContext;

public class Consumers extends DefaultContext<Consumer> {

	// Distribution of consumer's utility parameters
	// This variables are static to access them easily
	// Are initialized in constructor without problem because
	// only one instance is always created
	private static double rawMinWelfareParam;
	private static double lambda;
	private static double probabilityForRichestConsumer;

	private static Pareto welfareParamDistrib;
	private static Beta qualityDiscountDistrib;
	private static int mktSize;

	public static void resetStaticVars() {

		rawMinWelfareParam = (double) GetParameter("minWelfareParam");
		welfareParamDistrib = null;
		qualityDiscountDistrib = null;
		double gini = (double) GetParameter("gini");
		lambda = (1.0 + gini) / (2.0 * gini);
		mktSize = (int) GetParameter("numberOfConsumers");
		probabilityForRichestConsumer = (double) GetParameter("richestProbability");

	}

	public Consumers() {
		super("Consumers_Context");

		createProbabilityDistrib();

	}

	private static void createProbabilityDistrib() {

		// Welfare Param
		welfareParamDistrib = Pareto.getPareto(lambda, rawMinWelfareParam);

		// Quality Discount
		double mean = (Double) GetParameter("qualityDiscountMean");
		double mode = (Double) GetParameter("qualityDiscountMostLikely");
		double alpha = mean * (1 - 2 * mode) / (mean - mode);
		double beta = alpha * (1 - mean) / mean;
//		qualityDiscountDistrib = RandomHelper.createBeta(alpha, beta);
		
		RandomEngine engine = new MersenneTwister(Market.seed);
		qualityDiscountDistrib = new Beta(alpha, beta, engine);
	}

	public static Pareto getWelfareParamDistrib() {
		return welfareParamDistrib;
	}

	public static Beta getQualityDicountDistrib() {
		return qualityDiscountDistrib;
	}

	public static int getMarketSize() {
		return mktSize;
	}

	public static double getLambda() {
		return lambda;
	}

	public static double getMinRawWelfareParam() {
		return rawMinWelfareParam;
	}

	public static void createConsumers() {

		for (int i = 1; i <= (Integer) GetParameter("numberOfConsumers"); i++) {
			new Consumer();
		}

	}

	public static double getExpectedQuantity(Offer segOffer, Optional<Offer> loOffer, Optional<Offer> hiOffer) {

		double demandAboveLoLimit, demandAboveHiLimit;

		demandAboveLoLimit = getExpectedConsumersAbove(
				Consumers.limitingWelfareParamPerceivedByFirms(loOffer, Optional.of(segOffer)));
		demandAboveHiLimit = getExpectedConsumersAbove(
				Consumers.limitingWelfareParamPerceivedByFirms(Optional.of(segOffer), hiOffer));

		if (demandAboveLoLimit > demandAboveHiLimit)
			return demandAboveLoLimit - demandAboveHiLimit;
		else
			return 0;

	}

	/*
	 * This function depends on the assumption of Pareto distribution of welfare
	 * parameter
	 */
	private static double getExpectedConsumersAbove(double welfareParameter) {

		if (welfareParameter <= rawMinWelfareParam)
			return mktSize;
		else if (welfareParameter == Double.POSITIVE_INFINITY)
			return 0.;
		else
			return mktSize * FastMath.pow(rawMinWelfareParam / welfareParameter, lambda);

	}

	/*
	 * Calculates the welfare parameter (originally marginal utility of quality)
	 * that divides consumer preferences, as perceived by firms (independenty how
	 * recessions are taken into account on firms decisions)
	 * 
	 * Consumers with a welfare parameter below "limit" will choose loOffer, while
	 * the ones with higher welfare parameter would choose hiOffer
	 * 
	 * When there is no limit, function returns POSITIVE_INFINITY
	 */
	public static double limitingWelfareParamPerceivedByFirms(Optional<Offer> loOf, Optional<Offer> hiOf) {

		assert !Offer.equivalentOffers(loOf, hiOf) : "Offers should be different";
		
		if (!hiOf.isPresent())
			// If there is no higher Offer there is no limit
			return Double.POSITIVE_INFINITY;

		else if (!loOf.isPresent())
			// If there is no lower Offer the limit is determined by the minimum welfare
			// param
			return UtilityFunction.getMinWelfareParamAceptingOfferPerceivedByFirms(hiOf.get());

		else
			// Both are present
			return limitingWelfareParamPerceivedByFirms(loOf.get(), hiOf.get());

	}

	/*
	 * It returns the welfare parameter that segments market between low and high
	 * offers
	 * 
	 * Returns minimum welfare if no consumer would choose lower offer
	 * 
	 * Returns positive infinity if no consumer would choose higher offer
	 * 
	 */
	private static double limitingWelfareParamPerceivedByFirms(Offer loOf, Offer hiOf) {

		assert (loOf != null) && (hiOf != null);

		BigDecimal loQ, hiQ;

		loQ = loOf.getQuality();
		hiQ = hiOf.getQuality();

		BigDecimal loP, hiP;
		loP = loOf.getPrice();
		hiP = hiOf.getPrice();

		assert (loQ.compareTo(hiQ) <= 0);
		
		double minRawWP = getMinRawWelfareParam();

		if (loP.compareTo(hiP) >= 0)
			// no consumer would choose lower offer
			return RecessionsHandler.getWelfareParamPerceivedByFirms(minRawWP);

		else if (loQ.compareTo(hiQ) == 0) {
			// as loP < hiP, no consumer would choose higher offer
			return Double.POSITIVE_INFINITY;

		} else {
			double rawWP = FastMath.max(minRawWP, UtilityFunction.calculateRawLimit(loP, loQ, hiP, hiQ)); 
			return RecessionsHandler.getWelfareParamPerceivedByFirms(rawWP);
		}

	}

	public static BigDecimal getMaxPriceForPoorestConsumer(BigDecimal quality) {

		double rawPoorestWP = getMinRawWelfareParam();
		double poorestWelfareParam = RecessionsHandler.getWelfareParamPerceivedByFirms(rawPoorestWP);

		return UtilityFunction.getMaxPriceForWelfareParam(quality, poorestWelfareParam);

	}

	public static BigDecimal getMaxPriceForRichestConsumer(Firm f, BigDecimal perceivedQ) {

		double rawRichestWP = getRawMaxWelfareParamForRichestConsumer(f);
		double richestWelfareParam = RecessionsHandler.getWelfareParamPerceivedByFirms(rawRichestWP);

		return UtilityFunction.getMaxPriceForWelfareParam(perceivedQ, richestWelfareParam);
	}

	/*
	 * Calculates the maximum raw welfare parameter that guarantees at least one
	 * consumer with probability given as parameter
	 */
	private static double getRawMaxWelfareParamForRichestConsumer(Firm f) {

		double adjMktSize = f.getAdjustedDemand(mktSize);

		double tmp = Math.pow(1 - probabilityForRichestConsumer, 1.0 / adjMktSize);
		tmp = Math.pow(1 - tmp, 1 / lambda);

		return rawMinWelfareParam / tmp;

	}

}