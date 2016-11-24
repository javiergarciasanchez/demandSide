package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.util.ArrayList;

import consumers.Consumer;
import consumers.Consumers;
import demandSide.Market;
import demandSide.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;

public class Firm {

	protected double minPoorestConsumerMargUtil = Consumers
			.getMinMargUtilOfQuality();
	protected double maxPoorestConsumerMargUtil = Consumers
			.getMaxMargUtilOfQuality();

	private StrategicPreference strategicPreference;
	private Offer offer;
	private DeltaOffer deltaOffer;
	private int demand = 0;
	private double profit = 0.0;

	private double accumProfit = 0;
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

		offer = new Offer();
		setDeltaOffer(new DeltaOffer());

		strategicPreference = new StrategicPreference(this);

		notYetKnownBy = new ArrayList<Consumer>();

		fixedCost = (Double) Market.firms.getFixedCostDistrib().nextDouble();

		born = RepastEssentials.GetTickCount();

		// Expand max price if cost at highest quality is higher than max price
		double maxUnitCost = unitCost(Offer.getMaxQuality());
		if (maxUnitCost > Offer.getMaxPrice()) {
			Offer.setMaxPrice(maxUnitCost + DeltaOffer.getDefaultSize());
		}

		makeInitialOffer();

	}

	private void makeInitialOffer() {

		double q = getRandomInitialQuality();
		double p = getOptimalPriceGivenQ(q);

		offer.setQuality(q);
		offer.setPrice(p);

		Market.firms.addToFirmLists(this);

		initializeConsumerKnowledge();

	}

	/*
	 * Gets the price that maximizes profit given quality.
	 * 
	 * Profit depends on quantity which depends on who are the neighbors. Thus
	 * depending which competitors are expelled, we have different profit
	 * functions.
	 * 
	 * For every possible set of competitors we get a temporary optimal price
	 * 
	 * The price that produces the highest profit among all set of competitors
	 * is the optimal price
	 * 
	 * The different set of competitors are determined by reducing price. The
	 * lower the price the more competitors are expelled The lowest meaningful
	 * price is marginal cost
	 */
	private double getOptimalPriceGivenQ(double realQ) {
		double perceivedQ = getPerceptionDiscount() * realQ;
		double realCost = unitCost(realQ);

		return Market.firms.perceivedQSegments.getOptimalPrice(perceivedQ,
				realCost, this::selectPriceFromRange);

	}

	private double getRandomInitialQuality() {
		return RandomHelper.nextDoubleFromTo(Offer.getMinQuality(),
				Offer.getMaxInitialQuality());
	}

	@ScheduledMethod(start = 1, priority = RunPriority.MAKE_OFFER_PRIORITY, interval = 1)
	public void makeOffer() {

		setNextOffer();

		updateConsumerKnowledge();

	}

	/*
	 * it should add the new offer to history it should remove the firm from and
	 * add it back to FirmsByQ
	 */
	protected void setNextOffer() {
		Utils.setNewRationalOffer(this);
	}

	public double selectPriceFromRange(double lo, double hi) {
		return strategicPreference.selectPriceFromRange(lo, hi);
	}

	private double getPerceptionDiscount() {
		// Weighted average of discounted quality factor and real quality factor
		// real q weighted by consumers already tried the firm
		// disc q weighted by consumers didn't try the firm

		double notTriedBy = Market.consumers.size() - triedBy;

		double avgDiscountFactor = (Double) GetParameter("qualityDiscountMean");
		return (triedBy + avgDiscountFactor * notTriedBy)
				/ (triedBy + notTriedBy);
	}

	protected double getPerceivedQuality() {

		return getQuality() * getPerceptionDiscount();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.NEXT_STEP_FIRM_PRIORITY, interval = 1)
	public void nextStep() {
		// This is run after all offers are made and consumers have chosen
		// Calculates profit, accumProfit and kills the firm if necessary
		// History was kept when Current State was established

		// Calculate profits of period
		profit = profit();

		accumProfit += getProfit();

		if (born == RepastEssentials.GetTickCount())
			// Entry moment
			autoRegressiveProfit = getProfit();
		else
			autoRegressiveProfit = currentProfitWeight * getProfit()
					+ (1 - currentProfitWeight) * autoRegressiveProfit;

		if (isToBeKilled())
			Market.toBeKilled.add(this);
		else
			// Updates Projections of results
			updateProjections();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.firms2DProjection.update(this);
		Market.firmsDemandProjection.update(this);
		Market.firmsProfitProjection.update(this);
		Market.margUtilProjection.update(this);
	}

	private void initializeConsumerKnowledge() {

		notYetKnownBy.addAll(Market.consumers);

		// Take out of the list the initial "knower's"
		getFromIgnorance((int) Math
				.round((Double) GetParameter("initiallyKnownByPerc")
						* Market.consumers.size()));

	}

	private void getFromIgnorance(int amount) {
		Consumer c;

		for (int k = 0; (k < amount) && !notYetKnownBy.isEmpty(); k++) {

			int i = RandomHelper.getUniform().nextIntFromTo(0,
					notYetKnownBy.size() - 1);

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

		int knownByIncrement = (int) Math
				.round(Market.firms.diffusionSpeedParam * alreadyK
						* (1.0 - alreadyK / mktSize));

		knownByIncrement = Math.min(mktSize, knownByIncrement);
		knownByIncrement = Math.max(1, knownByIncrement);

		getFromIgnorance(knownByIncrement);

	}

	private double profit() {
		return (getPrice() - unitCost(getQuality())) * getDemand() - fixedCost;
	}

	public double unitCost(double quality) {
		// Cost grows quadratically with quality
		return Math.pow(quality / costParameter, 2.0);
	}

	protected double getMarginalCostOfQuality(double q) {
		return 2.0 / Math.pow(costParameter, 2.0) * q;
	}

	private boolean isToBeKilled() {
		// Returns true if firm should exit the market
		return (autoRegressiveProfit < Market.firms.minimumProfit);
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

	//
	// Getters to probe
	//

	public DeltaOffer getDeltaOffer() {
		return deltaOffer;
	}

	public void setDeltaOffer(DeltaOffer deltaOffer) {
		this.deltaOffer = deltaOffer;
	}

	public void setDemand(int i) {
		demand = i;
	}

	/*
	 * Setters to probe DO NOT USE FOR CODE
	 */
	public void setPrice(double p) {
		Market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setPrice(p);

		Market.firms.addToFirmLists(this);
	}

	public void setQuality(double q) {
		Market.firms.removeFromFirmLists(this);

		Offer o = getOffer();
		o.setQuality(q);

		Market.firms.addToFirmLists(this);
	}

	//
	// Getters to probe
	//

	public int getDemand() {
		return demand;
	}

	public Offer getOffer() {
		return offer;
	}

	public double getProfit() {
		return profit;
	}

	public double getPrice() {
		return offer.getPrice();
	}

	public double getQuality() {
		return offer.getQuality();
	}

	public double getAge() {
		return RepastEssentials.GetTickCount() - born;
	}

	public String getFirmType() {
		return getClass().toString().substring(16, 17);
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

	public double getAutoRegressiveProfit() {
		return autoRegressiveProfit;
	}

	public double getFixedCost() {
		return fixedCost;
	}

	public double getGrossMargin() {
		return (getPrice() - unitCost(getQuality())) / getPrice();
	}

	public double getMargin() {
		return getProfit() / (getDemand() * getPrice());
	}

	public String toString() {
		return ID;
	}

}
