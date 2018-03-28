package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;

import consumers.Consumer;
import consumers.Consumers;
import demandSide.Market;
import demandSide.RunPriority;
import graphs.Scale;
import optimalPrice.OptimalPrice;
import optimalPrice.OptimalPriceResult;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;

public abstract class Firm {

	private Decision decision;
	private int demand = 0;
	private double profit = 0.0;

	private double accumProfit = 0;
	private int accumZeroDemand = 0;
	private double autoRegressiveProfit = 0;
	private double fixedCost;
	private double born;

	private ArrayList<Consumer> notYetKnownBy;
	private int triedBy = 0;

	// Cost Scale is the same for all firms. It could be changed easily
	private static double costParameter;
	private static double currentProfitWeight;

	protected static long firmIDCounter;

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm " + firmIntID;

	public static void resetStaticVars() {
		// resets static variables
		costParameter = (Double) GetParameter("costParameter");
		currentProfitWeight = (Double) GetParameter("currentProfitWeight");
		firmIDCounter = 1;
	}

	public Firm() {

		Market.firms.add(this);

		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

		born = RepastEssentials.GetTickCount();

		if (!makeInitialOffer())
			Market.firms.remove(this);

	}

	private boolean makeInitialOffer() {

		Optional<BigDecimal> optQ;
		BigDecimal realQ;

		optQ = getRandomQuality();

		if (optQ.isPresent())
			realQ = optQ.get();
		else
			// There is no available quality
			return false;

		Optional<OptimalPriceResult> optPriceResult = getOptimalPriceResultGivenRealQ(realQ);

		optPriceResult.ifPresent(opr -> setNewDecision(opr, realQ));

		return optPriceResult.isPresent();

	}

	private Optional<BigDecimal> getRandomQuality() {

		double maxIniQ = (double) GetParameter("maxInitialQuality");
		BigDecimal q = BigDecimal.valueOf(RandomHelper.nextDoubleFromTo(0.0, maxIniQ));

		return getClosestAvailableQuality(q);
	}

	public Optional<BigDecimal> getClosestAvailableQuality(BigDecimal q) {

		// If quality is occupied it tries first upward then downward
		Optional<BigDecimal> up = Offer.getUpWardClosestAvailableQuality(q);
		return (up.isPresent() ? up : Offer.getDownWardClosestAvailableQuality(q));

	}

	private void setNewDecision(OptimalPriceResult optPR, BigDecimal q) {
		decision = new Decision(optPR, q);
		Market.firms.addToFirmLists(this);
		initializeConsumerKnowledge();
	}

	/*
	 * Gets the price that maximizes profit given quality.
	 * 
	 * Profit depends on quantity which depends on who are the neighbors. Thus
	 * depending which competitors are expelled, we have different profit functions.
	 * 
	 * For every possible set of competitors we get a temporary optimal price
	 * 
	 * The price that produces the highest profit among all set of competitors is
	 * the optimal price
	 * 
	 * The different set of competitors are determined by reducing price. The lower
	 * the price the more competitors are expelled The lowest meaningful price is
	 * marginal cost
	 */
	private Optional<OptimalPriceResult> getOptimalPriceResultGivenRealQ(BigDecimal q) {

		BigDecimal perceivedQ = getPerceivedQuality(q);
		double cost = getUnitCost(q);

		return OptimalPrice.get(perceivedQ, cost, getKnownByPerc(), new ExpectedMarket(this));

	}

	@ScheduledMethod(start = 1, priority = RunPriority.RESET_DEMAND_PRIORITY, interval = 1)
	public void resetDemand() {
		demand = 0;
	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1, shuffle = true)
	public void makeOffer() {

		ExpectedMarket expMkt = new ExpectedMarket(this);

		// if there is no decision it will keep previous one
		getRealQualityOptions().map(q -> getOptPriceDecision(q, expMkt)).max(new DecisionComparator())
				.ifPresent(this::updateDecision);

	}

	public abstract Stream<BigDecimal> getRealQualityOptions();

	private Optional<Decision> getOptPriceDecision(BigDecimal realQ, ExpectedMarket expMkt) {

		return OptimalPrice.get(getPerceivedQuality(realQ), getUnitCost(realQ), getKnownByPerc(), expMkt)
				.map(r -> new Decision(r, realQ));

	}

	private void updateDecision(Optional<Decision> optD) {

		// If decision is empty nothing is done, thus previous decision is kept
		optD.ifPresent(d -> {
			Market.firms.updateFirmLists(this, getQuality(), d.getQuality());
			decision = d;
			updateConsumerKnowledge();
		});
	}

	public BigDecimal getPerceivedQuality() {
		return getPerceivedQuality(getQuality());
	}

	public BigDecimal getPerceivedQuality(BigDecimal realQuality) {
		return realQuality.multiply(BigDecimal.valueOf(getPerceptionDiscount())).setScale(Offer.getQualityScale(),
				Offer.getQualityRounding());
	}

	private double getPerceptionDiscount() {
		// Weighted average of discounted quality factor and real quality factor
		// real q weighted by consumers already tried the firm
		// disc q weighted by consumers didn't try the firm

		double mktSize = Market.consumers.size();
		double notTriedBy = mktSize - triedBy;

		double avgDiscountFactor = (Double) GetParameter("qualityDiscountMean");
		return (triedBy + avgDiscountFactor * notTriedBy) / mktSize;

	}

	public Offer getPerceivedOffer() {

		Offer retval = new Offer(decision.getOffer());
		retval.setQuality(getPerceivedQuality());

		return retval;

	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary

		// Calculate profits of period
		profit = calcProfit();

		accumProfit += getProfit();

		// Accumulates continuos zero demand periods
		if (getDemand() == 0)
			accumZeroDemand += 1;
		else
			accumZeroDemand = 0;

		if (born == RepastEssentials.GetTickCount())
			// Entry moment
			autoRegressiveProfit = getProfit();
		else
			autoRegressiveProfit = currentProfitWeight * getProfit() + (1 - currentProfitWeight) * autoRegressiveProfit;

		if (isToBeKilled())
			Market.toBeKilled.add(this);

		else
			// Updates Projections of results
			updateProjections();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Scale.update(this);
		Market.firms2DProjection.update(this);
		Market.firmsDemandProjection.update(this);
		Market.firmsProfitProjection.update(this);
		Market.firmsSalesProjection.update(this);
		Market.margUtilProjection.update(this);
	}

	private void initializeConsumerKnowledge() {

		notYetKnownBy.addAll(Market.consumers);

		// Take out of the list the initial "knower's"
		getFromIgnorance(FastMath.round((Double) GetParameter("initiallyKnownByPerc") * Market.consumers.size()));

	}

	private void getFromIgnorance(long amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0, notYetKnownBy.size() - 1);

			c = notYetKnownBy.get(i);
			notYetKnownBy.remove(i);
			c.addToKnownFirms(this);

		}
	}

	private void updateConsumerKnowledge() {
		Consumers consumers = Market.consumers;
		int mktSize = consumers.size();

		// if all Consumers know the firm then return
		if (notYetKnownBy.isEmpty())
			return;

		// Take some consumers out of "ignorance" and let them know this
		// firm
		// Knowledge is spread using the logistic growth equation (Bass
		// model with 100% imitators)

		double alreadyK = mktSize - notYetKnownBy.size();

		int knownByIncrement = (int) FastMath.round(Firms.diffusionSpeedParam * alreadyK * (1.0 - alreadyK / mktSize));

		knownByIncrement = FastMath.min(mktSize, knownByIncrement);
		knownByIncrement = FastMath.max(1, knownByIncrement);

		getFromIgnorance(knownByIncrement);

	}

	private double calcProfit() {
		return (getPrice().doubleValue() - getUnitCost(getQuality())) * getDemand() - fixedCost;
	}

	public double getUnitCost(BigDecimal quality) {
		// Cost grows quadratically with quality
		return FastMath.pow(quality.doubleValue() / costParameter, 2);

	}

	public double getMarginalCostOfQuality(BigDecimal realQ) {
		return 2.0 / FastMath.pow(costParameter, 2.0) * realQ.doubleValue();
	}

	private boolean isToBeKilled() {

		// Returns true if firm should exit the market
		boolean minProf = autoRegressiveProfit < Firms.minimumProfit;
		boolean maxZeroDemand = accumZeroDemand > Firms.maxZeroDemand;

		return (minProf || maxZeroDemand);

	}

	public void killFirm() {

		Market.firms.removeFromFirmLists(this);

		// Remove firm from consumers lists
		Market.consumers.forEach((c) -> c.removeTraceOfFirm(this));

		Market.firms.remove(this);

	}

	public void addNewConsumer() {
		triedBy++;
	}

	public void setDemand(int i) {
		demand = i;
	}

	public double getKnownByPerc() {
		return 1.0 - (double) notYetKnownBy.size() / (double) Consumers.getMarketSize();
	}

	/*
	 * Setters to probe DO NOT USE FOR CODE
	 */
	public void setPrice(double p) {
		// Market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setPrice(p);

		Market.firms.addToFirmLists(this);
	}

	public void setQuality(double q) {
		// Market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setQuality(q);

		Market.firms.addToFirmLists(this);
	}

	/*
	 * Getters to probe
	 */

	public int getDemand() {
		return demand;
	}

	public double getExpectedDemand() {
		return decision.getExpectedDemand();
	}

	public double getExpectedGrossProfit() {
		return decision.getExpectedGrossProfit();
	}

	public Offer getOffer() {
		return decision.getOffer();
	}

	public double getProfit() {
		return profit;
	}

	public double getSales() {
		return getDemand() * getPrice().doubleValue();
	}

	public BigDecimal getPrice() {
		return decision.getPrice();
	}

	public BigDecimal getQuality() {
		return decision.getQuality();
	}

	public double getAge() {
		return RepastEssentials.GetTickCount() - born;
	}

	public String getFirmType() {
		return "Firm";
		// return getClass().toString().substring(16, 17);
	}

	public String getFirmID() {
		return getFirmType() + " " + getFirmNumID();
	}

	public long getFirmIntID() {
		return firmIntID;
	}

	public String getFirmNumID() {
		return Long.toString(firmIntID);
	}

	public double getAccumProfit() {
		return accumProfit;
	}

	public double getAccumZeroDemand() {
		return accumZeroDemand;
	}

	public double getAutoRegressiveProfit() {
		return autoRegressiveProfit;
	}

	public double getUnitCost() {
		return getUnitCost(getQuality());
	}

	public double getFixedCost() {
		return fixedCost;
	}

	public double getGrossProfit() {
		return (getPrice().doubleValue() - getUnitCost(getQuality())) * getDemand();
	}

	public double getGrossMargin() {
		return (getPrice().doubleValue() - getUnitCost(getQuality())) / getPrice().doubleValue();
	}

	public double getPoorestConsumer() {
		return Consumers.getMinMargUtilOfQualityAceptingOffer(getPerceivedOffer());
	}

	public double getMargin() {
		return getProfit() / getSales();
	}
	
	public double getDemandShare() {
		return (double) getDemand() / Market.firms.getTotalDemand();
	}

	public double getMktShare() {
		return getSales() / Market.firms.getTotalSales();
	}

	public String toString() {
		return ID;
	}

}
