package consumers;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.lang.Double;
import java.lang.Long;
import java.lang.String;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import firms.Offer;
import demandSide.Market;
import demandSide.RecessionsHandler;
import demandSide.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private double margUtilOfQuality;
	private double qualityDiscount;
	private Optional<Firm> chosenFirm;

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

		chosenFirm = Optional.empty();

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

		// Upper limit is unbounded. The max marg utility is updated
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

	@ScheduledMethod(start = 1, priority = RunPriority.CHOOSE_FIRM_PRIORITY, interval = 1)
	public void chooseFirm() {

		chosenFirm = chooseMaximizingFirm();

		chosenFirm.ifPresent(f -> {
			// Increase Demand
			f.setDemand(f.getDemand() + 1);

			// Check if first time chosen
			if (firstTimeChosen(f)) {
				f.addNewConsumer();
				addToTriedFirms(f);

			}
		});

	}

	private void addToTriedFirms(Firm f) {
		// Sets quality factor to 1
		knownFirmsQualityFactor.put(f, 1.0);
	}

	private boolean firstTimeChosen(Firm f) {
		return (knownFirmsQualityFactor.get(f) != 1.0);
	}

	// @ScheduledMethod(start = 1, priority =
	// RunPriority.UPDATE_PROJECTIONS_PRIORITY, interval = 1)
	public void updateProjections() {
		Market.consumersProjection.update(this);
		Market.margUtilProjection.update(this);
		Market.consumptionProjection.update(this);
	}

	// Returns the firm from the known firms that maximizes utility
	// if no firm is chosen returns null (all utilities are below 0)
	private Optional<Firm> chooseMaximizingFirm() {

		double maxUtility = 0;
		Optional<Firm> maxUtilFirm = Optional.empty();

		for (Entry<Firm, Double> knownFirm : knownFirmsQualityFactor.entrySet()) {

			double tmpUtil = expectedUtility(knownFirm.getKey(), knownFirm.getValue());

			if (tmpUtil > maxUtility) {
				maxUtilFirm = Optional.of(knownFirm.getKey());
				maxUtility = tmpUtil;
			}
		}

		// if no firm was chosen maxUtilFirm would be null
		return maxUtilFirm;

	}

	private double expectedUtility(Firm f, double qualityFactor) {
		Offer o = f.getOffer();
		return getMargUtilOfQuality() * o.getQuality().doubleValue() * qualityFactor - o.getPrice().doubleValue();
	}

	private double realUtility(Firm f, double qualityFactor) {
		Offer o = f.getOffer();
		return getMargUtilOfQuality() * o.getQuality().doubleValue() - o.getPrice().doubleValue();
	}

	public void removeTraceOfFirm(Firm firm) {
		knownFirmsQualityFactor.remove(firm);

		chosenFirm.ifPresent(f -> {
			if (f.equals(firm))
				chosenFirm = Optional.empty();
		});

	}

	public double getMargUtilOfQuality() {
		double recessionImpact = 1 - RecessionsHandler.getRecesMagnitude();
		return margUtilOfQuality * recessionImpact;
	}

	//
	// Procedures for inspecting values
	//
	public Optional<Firm> getChosenFirm() {
		return chosenFirm;
	}

	public String getChosenFirmID() {
		return chosenFirm.map(Firm::toString).orElse("Substitute");
	}

	public double getChosenFirmIntID() {
		return chosenFirm.map(Firm::getFirmIntID).orElse((long) 0);
	}

	public double getRealUtility() {
		return chosenFirm.map(f -> realUtility(f, knownFirmsQualityFactor.get(f))).orElse(0.0);
	}

	public double getExpectedUtility() {
		return chosenFirm.map(f -> expectedUtility(f, knownFirmsQualityFactor.get(f))).orElse(0.0);
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
