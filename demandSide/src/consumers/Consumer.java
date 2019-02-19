package consumers;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.lang.Double;
import java.lang.Long;
import java.lang.String;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import demandSide.Market;
import demandSide.RecessionsHandler;
import demandSide.RunPriority;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Consumer {

	private double rawWelfareParam;
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

		setRawWelfareParam();

		setQualityDiscount();

	}

	private void setRawWelfareParam() {

		rawWelfareParam = Consumers.getWelfareParamDistrib().nextDouble();

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

	// Returns the firm from the known firms that maximizes utility
	// if no firm is chosen returns empty (all utilities are below 0)
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

		// if no firm was chosen maxUtilFirm would be empty
		return maxUtilFirm;

	}

	private double expectedUtility(Firm f, double qualityFactor) {

		double welfareParam = RecessionsHandler.getWelfareParamForConsumers(getRawWelfareParam());

		return UtilityFunction.expectedUtility(welfareParam, f.getOffer(), qualityFactor);
	}

	private double realUtility(Firm f, double qualityFactor) {

		double welfareParam = RecessionsHandler.getWelfareParamForConsumers(getRawWelfareParam());

		return UtilityFunction.realUtility(welfareParam, f.getOffer());
	}
	
	public void removeTraceOfFirm(Firm firm) {
		knownFirmsQualityFactor.remove(firm);

		chosenFirm.ifPresent(f -> {
			if (f.equals(firm))
				chosenFirm = Optional.empty();
		});

	}

	public double getRawWelfareParam() {
		return rawWelfareParam;
	}

	
	//
	// Procedures for inspecting values
	//
	public double getWelfareParam() {
		return RecessionsHandler.getWelfareParamForConsumers(getRawWelfareParam());
	}

	public Optional<Firm> getChosenFirm() {
		return chosenFirm;
	}

	public String getChosenFirmID() {
		return chosenFirm.map(Firm::toString).orElse("Substitute");
	}

	public double getChosenFirmIntID() {
		return chosenFirm.map(Firm::getFirmIntID).orElse((long) 0);
	}

	public double getExpectedUtility() {
		return chosenFirm.map(f -> expectedUtility(f, knownFirmsQualityFactor.get(f))).orElse(0.0);
	}

	public double getRealUtility() {
		return chosenFirm.map(f -> realUtility(f, knownFirmsQualityFactor.get(f))).orElse(0.0);
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