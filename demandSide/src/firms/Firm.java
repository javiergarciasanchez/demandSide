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

	public Market market;

	private Decision decision;
	private History history;
	private int demand = 0;
	private double profit = 0.0;

	private double accumProfit = 0;
	private int accumZeroDemand = 0;
	private double autoRegressiveProfit = 0;
	private double fixedCost;
	private double born;

	private ArrayList<Consumer> notYetKnownBy;
	private int triedBy = 0;

	protected static long firmIDCounter;

	private long firmIntID = firmIDCounter++;
	private String ID = "Firm_" + firmIntID;

	public static void resetCounter() {
		firmIDCounter = 1;
	}

	public Firm(Market market) {
		this.market = market;

		market.firms.add(this);

		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) market.firms.getFixedCostDistrib().nextDouble();

		born = RepastEssentials.GetTickCount();

		history = new History(market);

		if (!makeInitialOffer())
			market.firms.remove(this);

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

		double doubleQ = market.firms.getInitialQualityDistrib().nextDouble();
		return getClosestAvailableQuality(BigDecimal.valueOf(doubleQ));
	}

	public Optional<BigDecimal> getClosestAvailableQuality(BigDecimal q) {

		// If quality is occupied it tries first upward then downward
		Optional<BigDecimal> up = market.consumers.getUpWardClosestAvailableQuality(q);

		return (up.isPresent() ? up : market.consumers.getDownWardClosestAvailableQuality(q));

	}

	private void setNewDecision(Decision d) {
		assert d != null;

		decision = d;
		market.firms.addToFirmLists(this);
		initializeConsumerKnowledge();
	}

	@ScheduledMethod(start = 1, priority = RunPriority.RESET_DEMAND_PRIORITY, interval = 1)
	public void resetDemand() {
		demand = 0;
	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1, shuffle = true)
	public void makeOffer() {

		// Update competitors expected offers
		history.updateCompetitorsPerceivedOffers(getQuality());

		// Gets possible quality options
		// Then gets the optimal price for each quality option
		// Gets the best decision according to expected profit
		// If there is a valid new decision, updates its decision otherwise keeps previous
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
			market.firms.updateFirmLists(this, getQuality(), d.getQuality());
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

		double mktSize = market.consumers.size();
		double notTriedBy = mktSize - triedBy;

		double avgDiscountFactor = (Double) GetParameter("qualityDiscountMean");
		return (triedBy + avgDiscountFactor * notTriedBy) / mktSize;

	}

	public Offer getPerceivedOffer() {

		Offer retval = new Offer(decision.getOffer());
		retval.setQuality(getPerceivedQuality());

		return retval;

	}

	public Offer getCompetitorPerceivedOffer(Firm f) {
		return history.getCompetitorPerceivedOffer(f);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {

		double currentProfitWeight = market.firms.currentProfitWeight;
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

		notYetKnownBy.addAll(market.consumers);

		// Take out of the list the initial "knower's"
		getFromIgnorance(
				FastMath.round((Double) GetParameter("initiallyKnownByPerc") * market.consumers.getMarketSize()));

	}

	private void getFromIgnorance(long amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = market.firms.getGetFromIgnoranceDistrib().nextIntFromTo(0, notYetKnownBy.size() - 1);

			c = notYetKnownBy.get(i);
			notYetKnownBy.remove(i);
			c.addToKnownFirms(this);

		}
	}

	private void updateConsumerKnowledge() {
		Consumers consumers = market.consumers;
		int mktSize = consumers.size();

		// if all Consumers know the firm then return
		if (notYetKnownBy.isEmpty())
			return;

		// Take some consumers out of "ignorance" and let them know this
		// firm
		// Knowledge is spread using the logistic growth equation (Bass
		// model with 100% imitators)

		double alreadyK = mktSize - notYetKnownBy.size();

		double diffusionSpeed = market.firms.diffusionSpeedParam;
		int knownByIncrement = (int) FastMath.round(diffusionSpeed * alreadyK * (1.0 - alreadyK / mktSize));

		knownByIncrement = FastMath.min(mktSize, knownByIncrement);
		knownByIncrement = FastMath.max(1, knownByIncrement);

		getFromIgnorance(knownByIncrement);

	}

	public static double calcProfit(double price, double unitCost, double demand, double fixedCost) {
		return (price - unitCost) * demand - fixedCost;
	}

	public double getUnitCost(BigDecimal quality) {
		// Cost grows quadratically with quality
		double costScale = market.firms.costScale;	
		double costExponent = market.firms.costExponent;
		return costScale * FastMath.pow(quality.doubleValue(), costExponent);

	}

	private boolean isToBeKilled() {

		// Returns true if firm should exit the market
		boolean minProf = autoRegressiveProfit < market.firms.minimumProfit;
		boolean maxZeroDemand = accumZeroDemand > market.firms.maxZeroDemand;

		return (minProf || maxZeroDemand);

	}

	public void killFirm() {

		market.firms.removeFromFirmLists(this);

		// Remove firm from consumers lists
		market.consumers.forEach((c) -> c.removeTraceOfFirm(this));

		market.firms.remove(this);

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
		return 1.0 - (double) notYetKnownBy.size() / (double) market.consumers.getMarketSize();
	}

	/*
	 * Setters to probe DO NOT USE FOR CODE
	 */
	public void setPrice(double p) {
		// market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setPrice(p);

		market.firms.addToFirmLists(this);
	}

	public void setQuality(double q) {
		// market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setQuality(q);

		market.firms.addToFirmLists(this);
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
		return (double) getDemand() / market.firms.getTotalDemand();
	}

	public double getMktShare() {
		return getSales() / market.firms.getTotalSales();
	}

	public String toString() {
		return getFirmType().toString() + "_" + getFirmIntID();
	}

}