package firms;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.TreeMap;
import org.apache.commons.math3.util.FastMath;

import demandSide.Market;
import demandSide.RunPriority;
import cern.jet.random.Gamma;
import static repast.simphony.essentials.RepastEssentials.GetParameter;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.essentials.RepastEssentials;

public class Firms extends DefaultContext<Firm> {

	// Random distributions
	private Gamma fixedCostDistrib;

	// Parameters for Firms
	double initiallyKnownByPerc, minimumProfit, diffusionSpeedParam;

	// Theoretical market based on perceived quality and price
	public TreeMap<BigDecimal, Firm> firmsByQ;

	public Firms() {
		super("Firms_Context");

		// Read parameters for all firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");

		createProbabilityDistrib();

		createFirmLists();

	}

	private void createProbabilityDistrib() {
		double mean, stdDevPercent, alfa, lamda;

		// Fixed Cost
		// We use Gamma distribution because the domain is > 0
		mean = (Double) GetParameter("fixedCostMean");
		stdDevPercent = (Double) GetParameter("fixedCostStdDevPerc");
		alfa = (1 / FastMath.pow(stdDevPercent, 2));
		lamda = alfa / mean;
		fixedCostDistrib = RandomHelper.createGamma(alfa, lamda);

	}

	public void createFirmLists() {

		firmsByQ = new TreeMap<BigDecimal, Firm>();		

	}

	public void addToFirmLists(Firm f) {

		firmsByQ.put(f.getQuality(), f);

	}
	
	public void updateFirmLists(Firm f, BigDecimal prevQ, BigDecimal newQ){
		firmsByQ.remove(prevQ);
		firmsByQ.put(newQ, f);
	}

	public void removeFromFirmLists(Firm f) {

		firmsByQ.remove(f);

	}	

	public Gamma getFixedCostDistrib() {
		return fixedCostDistrib;
	}

	@ScheduledMethod(start = 1, priority = RunPriority.ADD_FIRMS_PRIORITY, interval = 1)
	public void addFirms() {

		if ((boolean) GetParameter("firmsEntryOnlyAtStart") && (RepastEssentials.GetTickCount() != 1))
			return;

		for (int i = 1; i <= (Integer) GetParameter("potencialFirmsPerPeriod"); i++) {

			new Firm();
		}

	}

	@ScheduledMethod(start = 1, priority = RunPriority.KILL_FIRMS_PRIORITY, interval = 1)
	public void wipeDeadFirms() {
		for (Firm f : Market.toBeKilled)
			f.killFirm();

		Market.toBeKilled.clear();

	}

	@SuppressWarnings("unchecked")
	public static void get() {
		
		get((Collection<Firm>)RepastEssentials.FindContext("Firms_Context"));

	}

	public static void get(Collection<Firm> firmsColl) {
		System.out.println("firm price quality perceivedQ");

		firmsColl.stream().forEach(f -> System.out.println("f" + f.getFirmNumID() + " " + f.getPrice() + " "
				+ f.getQuality() + " " + f.getPerceivedQuality(f.getQuality())));
	}
	
	public static void get(Collection<Firm> firmsColl, Firm omitFirm) {
		System.out.println("firm price quality perceivedQ");

		firmsColl.stream().filter(f->(!f.equals(omitFirm))).forEach(f -> System.out.println("f" + f.getFirmNumID() + " " + f.getPrice() + " "
				+ f.getQuality() + " " + f.getPerceivedQuality(f.getQuality())));
	}

}
