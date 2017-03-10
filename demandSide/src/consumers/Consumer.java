package consumers;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import offer.Offer;
import demandSide.Market;
import demandSide.RecessionsHandler;
import demandSide.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private double margUtilOfQuality;
	private double qualityDiscount;
	private Firm chosenFirm;

	// This map collects the known firms, and provides a quality factor
	// Quality factor is a discount in case consumer has not yet tried the firm
	private HashMap<Firm, Double> knownFirmsQualityFactor;

	protected static long consumerIDCounter = 1;

	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public static void resetStaticVars() {
		// resets static variables
		consumerIDCounter = 1;
	}

	public Consumer() {

		Market.consumers.add(this);

		knownFirmsQualityFactor = new HashMap<Firm, Double>();

		assignPreferences();

	}

	// Assigns parameters for the choice function
	private void assignPreferences() {

		// We need to introduce randomness

		setMargUtilOfQuality();

		setQualityDiscount();

	}

	private void setMargUtilOfQuality() {

		// There is no need to introduce a marginal utility of money
		// because it is implicit in the marginal utility of quality
		margUtilOfQuality = Consumers.getMargUtilOfQualityDistrib().nextDouble();

		// Upper limit is unbounded. The max marg utility is updated to scale
		// graphs
		Consumers.setMaxMargUtilOfQuality(FastMath.max(Consumers.getMaxMargUtilOfQuality(), margUtilOfQuality));

	}

	private void setQualityDiscount() {
		// 1.0 could not being assigned because a value of 1.0 is considered
		// the consumer has already tried the firm
		qualityDiscount = Consumers.getQualityDicountDistrib().nextDouble();

		if (getQualityDiscount() == 1.0)
			qualityDiscount = FastMath.nextDown(getQualityDiscount());

	}

	public void addToKnownFirms(Firm f) {
		// It is assumed that firm was not known,
		// thus quality factor is quality discount
		knownFirmsQualityFactor.put(f, getQualityDiscount());
	}

	public void addToTriedFirms(Firm f) {
		// Sets quality factor to 1
		knownFirmsQualityFactor.put(f, 1.0);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public void chooseFirm() {

		chosenFirm = chooseMaximizingFirm();

		if (chosenFirm != null) {
			// Increase Demand
			chosenFirm.setDemand(chosenFirm.getDemand() + 1);

			// Check if first time chosen
			if (knownFirmsQualityFactor.get(chosenFirm) != 1.0) {
				chosenFirm.addNewConsumer();
				knownFirmsQualityFactor.put(chosenFirm, 1.0);
			}
		}

	}

//	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.consumersProjection.update(this);
		Market.margUtilProjection.update(this);
		Market.consumptionProjection.update(this);
	}

	// Returns the firm from the known firms that maximizes utility
	// if no firm is chosen returns null (all utilities are below 0)
	private Firm chooseMaximizingFirm() {

		double utility = 0;
		Firm maxUtilFirm = null;

		for (Entry<Firm, Double> knownFirm : knownFirmsQualityFactor.entrySet()) {

			if (utility(knownFirm) > utility) {
				maxUtilFirm = knownFirm.getKey();
				utility = utility(knownFirm);
			}
		}

		// if no firm was chosen maxUtilFirm would be null
		return maxUtilFirm;

	}

	private double utility(Entry<Firm, Double> knownFirm) {
		Offer o = knownFirm.getKey().getOffer();
		double qualityFactor = knownFirm.getValue();

		return margUtilOfQuality * o.getQuality() * qualityFactor - o.getPrice();
	}

	public void removeTraceOfFirm(Firm firm) {
		knownFirmsQualityFactor.remove(firm);

		if ((chosenFirm != null) && (chosenFirm.getFirmIntID() == firm.getFirmIntID()))
			chosenFirm = null;
	}

	// Procedures for inspecting values

	public Firm getChosenFirm() {
		return chosenFirm;
	}

	public double getMargUtilOfQuality() {
		double recessionImpact = 1 - RecessionsHandler.getRecesMagnitude();
		return margUtilOfQuality * recessionImpact;
	}

	public String getChosenFirmID() {
		if (chosenFirm == null)
			return "Substitute";
		else
			return chosenFirm.toString();
	}

	public double getChosenFirmIntID() {
		if (chosenFirm == null)
			return 0.0;
		else
			return chosenFirm.getFirmIntID();
	}

	public double getUtility() {
		if (chosenFirm == null)
			return 0.0;
		else
			return utility(new HashMap.SimpleEntry<Firm, Double>(chosenFirm, knownFirmsQualityFactor.get(chosenFirm)));

	}

	public double getConsumerIntID() {
		return consumerIntID;
	}

	public String getConsumerNumID() {
		return Long.toString(consumerIntID);
	}

	public String getConsumerID() {
		return ID;
	}

	public String toString() {
		return ID;
	}

	public double getQualityDiscount() {
		return qualityDiscount;
	}

}
