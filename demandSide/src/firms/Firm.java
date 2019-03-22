package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;

import consumers.Consumer;
import consumers.Consumers;
import consumers.UtilityFunction;
import demandSide.Market;
import demandSide.RunPriority;
import firmTypes.FirmTypes;
import optimalPrice.OptimalPrice;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;

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
	private static double costScale;
	private static double costExponent;

	private static double currentProfitWeight;

	protected static long firmIDCounter;

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm_" + firmIntID;

	public static void resetStaticVars() {
		// resets static variables
		costScale = (Double) GetParameter("costScale");
		costExponent = (Double) GetParameter("costExponent");
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

		Optional<Decision> optPriceDecision = getOptPriceDecision(realQ);
		optPriceDecision.ifPresent(opd -> setNewDecision(opd));

		return optPriceDecision.isPresent();

	}

	private Optional<BigDecimal> getRandomQuality() {

		double doubleQ = Market.firms.getInitialQualityDistrib().nextDouble();
		return getClosestAvailableQuality(BigDecimal.valueOf(doubleQ));
	}

	public Optional<BigDecimal> getClosestAvailableQuality(BigDecimal q) {

		// If quality is occupied it tries first upward then downward
		Optional<BigDecimal> up = Offer.getUpWardClosestAvailableQuality(q);

		return (up.isPresent() ? up : Offer.getDownWardClosestAvailableQuality(q));

	}

	private void setNewDecision(Decision d) {
		assert d != null;

		decision = d;
		Market.firms.addToFirmLists(this);
		initializeConsumerKnowledge();
	}

	@ScheduledMethod(start = 1, priority = RunPriority.RESET_DEMAND_PRIORITY, interval = 1)
	public void resetDemand() {
		demand = 0;
	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1, shuffle = true)
	public void makeOffer() {

		// Gets possible quality options
		// Then gets the optimal price for each quality option
		// Gets the best decision according to expected profit
		// If there is a valid new decision updates it decision otherwise keeps previous
		// one
		getRealQualityOptions().map(q -> getOptPriceDecision(q)).max(new DecisionComparator())
				.ifPresent(this::updateDecision);

	}

	public abstract Stream<BigDecimal> getRealQualityOptions();

	public abstract FirmTypes getFirmType();

	/*
	 * Gets the optimal decision given real q
	 * 
	 * it calculates price that maximizes profit given quality. Then it packs all in
	 * a decision
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
	 * 
	 * Decision includes expected demand and expected Gross Profit
	 */

	private Optional<Decision> getOptPriceDecision(BigDecimal realQ) {

		ExpectedMarket expMkt = new ExpectedMarket(this);

		Optional<Decision> opd = OptimalPrice.get(this, realQ, expMkt).map(opr -> new Decision(opr, realQ));

		return opd;

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
		profit = calcProfit(getPrice().doubleValue(), getUnitCost(getQuality()), getDemand(), getFixedCost());

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

	}

	private void initializeConsumerKnowledge() {

		notYetKnownBy.addAll(Market.consumers);

		// Take out of the list the initial "knower's"
		getFromIgnorance(FastMath.round((Double) GetParameter("initiallyKnownByPerc") * Consumers.getMarketSize()));

	}

	private void getFromIgnorance(long amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = Market.firms.getGetFromIgnoranceDistrib().nextIntFromTo(0, notYetKnownBy.size() - 1);

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

	public static double calcProfit(double price, double unitCost, double demand, double fixedCost) {
		return (price - unitCost) * demand - fixedCost;
	}

	public static double getUnitCost(BigDecimal quality) {
		// Cost grows quadratically with quality
		return costScale * FastMath.pow(quality.doubleValue(), costExponent);

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

	/*
	 * Returns demand adjusted by knowledge by consumers
	 */
	public double getAdjustedDemand(double demand) {
		return demand * getKnownByPerc();
	}

	private double getKnownByPerc() {
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
		return decision.expInf.demand;
	}

	public double getExpectedProfit() {
		return decision.expInf.profit;
	}

	public double getExpectedLowLimit() {
		return decision.expInf.loLimit;
	}

	public double getExpectedHighLimit() {
		return decision.expInf.hiLimit;
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

	public String getFirmID() {
		return ID;
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
		return getProfit() + getFixedCost();
	}

	public double getGrossMargin() {
		return (getPrice().doubleValue() - getUnitCost(getQuality())) / getPrice().doubleValue();
	}

	public double getPoorestConsumer() {
		return UtilityFunction.getMinWelfareParamAceptingOfferPerceivedByFirms(getPerceivedOffer());
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
		return getFirmType().toString() + "_" + getFirmIntID();
	}

}