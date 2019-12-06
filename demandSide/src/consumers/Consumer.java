package consumers;

import java.util.HashMap;
import java.util.Map;
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

	private Market market;

	private double rawWelfareParam;
	private double qualityDiscount;
	private Optional<Firm> chosenFirm;
	private ExpectedUtilityComparator expUtilComp;

	// This map collects the known firms, and provides a quality discount
	// Quality discount is used in case consumer has not yet tried the firm
	private HashMap<Firm, Double> knownFirmsQualityDiscount;

	protected static long consumerIDCounter = 1;

	protected long consumerIntID = consumerIDCounter++;
	protected String ID = "Cons. " + consumerIntID;

	public static void resetCounter() {
		consumerIDCounter = 1;
	}

	public Consumer(Market market) {
		this.market = market;

		market.consumers.add(this);

		chosenFirm = Optional.empty();

		knownFirmsQualityDiscount = new HashMap<Firm, Double>();

		assignPreferences();

		expUtilComp = new ExpectedUtilityComparator(this);

	}

	// Assigns parameters for the choice function
	private void assignPreferences() {

		// We need to introduce randomness

		setRawWelfareParam();

		setQualityDiscount();

	}

	private void setRawWelfareParam() {

		rawWelfareParam = market.consumers.getWelfareParamDistrib().nextDouble();

	}

	private void setQualityDiscount() {
		// 1.0 could not being assigned because a value of 1.0 is considered
		// the consumer has already tried the firm
		qualityDiscount = market.consumers.getQualityDicountDistrib().nextDouble();

		if (getQualityDiscount() == 1.0)
			qualityDiscount = FastMath.nextDown(getQualityDiscount());

	}

	public void addToKnownFirms(Firm f) {
		// It is assumed that firm was not known,
		knownFirmsQualityDiscount.put(f, getQualityDiscount());
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
		// Sets quality discount to 1
		knownFirmsQualityDiscount.put(f, 1.0);
	}

	private boolean firstTimeChosen(Firm f) {
		return (knownFirmsQualityDiscount.get(f) != 1.0);
	}

	// Returns the firm from the known firms that maximizes utility
	// if no firm is chosen returns empty (all utilities are below 0)
	private Optional<Firm> chooseMaximizingFirm() {

		return knownFirmsQualityDiscount.entrySet().stream().max(expUtilComp).filter(kF -> expectedUtility(kF) > 0.0)
				.map(Map.Entry::getKey);

	}

	public double expectedUtility(Entry<Firm, Double> knownFirm) {

		assert (knownFirm != null);

		double welfareParam = RecessionsHandler.getWelfareParamForConsumers(getRawWelfareParam());

		double price = knownFirm.getKey().getPrice();
		double quality = knownFirm.getKey().getQuality();
		double qualityDiscount = knownFirm.getValue();
		
		return UtilityFunction.realUtility(welfareParam, price, quality * qualityDiscount);
	}

	private double realUtility(Firm f) {

		double welfareParam = RecessionsHandler.getWelfareParamForConsumers(getRawWelfareParam());

		return UtilityFunction.realUtility(welfareParam, f.getPrice(), f.getQuality());
	}

	public void removeTraceOfFirm(Firm firm) {
		knownFirmsQualityDiscount.remove(firm);

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

		return chosenFirm.map(f -> expectedUtility(Map.entry(f, knownFirmsQualityDiscount.get(f)))).orElse(0.0);
	}

	public double getRealUtility() {
		return chosenFirm.map(f -> realUtility(f)).orElse(0.0);
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