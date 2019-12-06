package firms;

import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.math3.util.FastMath;

import demandSide.Market;
import demandSide.RunPriority;
import firmTypes.FirmTypes;

import cern.jet.random.Gamma;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;

public class Firms extends DefaultContext<Firm> {

	private Market market;

	// Random distributions
	public Uniform firmTypes;
	private Gamma fixedCostDistrib;
	private Uniform initialQualityDistrib;
	private Uniform getFromIgnoranceDistrib;

	// Parameters for Firms
	double initiallyKnownByPerc, diffusionSpeedParam,
		minimumProfit, currentProfitWeight,
		costScale, costExponent;
	int maxZeroDemand;

	// Data Summarization
	private class MarketStats {
		private int totalDemand;
		private double totalSales;

		public void accumulate(Firm f) {
			totalDemand += f.getDemand();
			totalSales += f.getSales();
		}

		public void combine(MarketStats mktStats) {
			totalDemand += mktStats.totalDemand;
			totalSales += mktStats.totalSales;
		}
	}

	private MarketStats marketStats;

	// Theoretical market based on perceived quality and price
	public TreeSet<Firm> firmsByQ;

	public Firms(Market market) {
		super("Firms_Context");

		this.market = market;

		readParams();
		
		createProbabilityDistrib();

		firmsByQ = new TreeSet<Firm>(new FirmComparatorByQ());

	}

	public void readParams() {

		// Read parameters for all firms
		initiallyKnownByPerc = (Double) GetParameter("initiallyKnownByPerc");
		minimumProfit = (Double) GetParameter("minimumProfit");
		diffusionSpeedParam = (Double) GetParameter("diffusionSpeedParam");
		maxZeroDemand = (int) GetParameter("maxZeroDemand");
		costScale = (Double) GetParameter("costScale");
		costExponent = (Double) GetParameter("costExponent");
		currentProfitWeight = (Double) GetParameter("currentProfitWeight");
		
	}

	private void createProbabilityDistrib() {
		double mean, stdDevPercent, alfa, lamda;

		// Fixed Cost
		// We use Gamma distribution because the domain is > 0
		mean = (Double) GetParameter("fixedCostMean");
		stdDevPercent = (Double) GetParameter("fixedCostStdDevPerc");
		alfa = (1 / FastMath.pow(stdDevPercent, 2));
		lamda = alfa / mean;
//		fixedCostDistrib = RandomHelper.createGamma(alfa, lamda);

		RandomEngine engine = new MersenneTwister(Market.seed);
		fixedCostDistrib = new Gamma(alfa, lamda, engine);

//		firmTypes = RandomHelper.createUniform(1, FirmTypes.values().length);
		engine = new MersenneTwister(Market.seed);
		firmTypes = new Uniform(1, FirmTypes.values().length, engine);

		double maxIniQ = (double) GetParameter("maxInitialQuality");
		engine = new MersenneTwister(Market.seed);
		initialQualityDistrib = new Uniform(0.0, maxIniQ, engine);

		engine = new MersenneTwister(Market.seed);
		getFromIgnoranceDistrib = new Uniform(engine);
	}

	public void addToFirmLists(Firm f) {
		firmsByQ.add(f);
	}

	public void updateFirmLists(Firm f, double prevQ, double newQ) {
		firmsByQ.remove(f);		
		firmsByQ.add(f);
	}

	public void removeFromFirmLists(Firm f) {
		firmsByQ.remove(f);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.ADD_FIRMS_PRIORITY, interval = 1)
	public void addFirms() {

		if ((boolean) GetParameter("firmsEntryOnlyAtStart") && (RepastEssentials.GetTickCount() != 1))
			return;

		for (int i = 1; i <= (Integer) GetParameter("potencialFirmsPerPeriod"); i++) {
			createRandomTypeFirm();
		}

	}

	private void createRandomTypeFirm() {

		// Create a firm of random type
//		FirmTypes.createRandomTypeFirm();

		// Temporarily fixed the type
		FirmTypes.STANDARD_FIRM.createFirm(market);

	}

	@ScheduledMethod(start = 1, priority = RunPriority.KILL_FIRMS_PRIORITY, interval = 1)
	public void wipeDeadFirms() {

		Market.toBeKilled.forEach(Firm::killFirm);

		Market.toBeKilled.clear();

	}

	@ScheduledMethod(start = 1, priority = RunPriority.UPDATE_STATISTICS_PRIORITY, interval = 1)
	public void updateStatistics() {
		marketStats = stream().collect(MarketStats::new, MarketStats::accumulate, MarketStats::combine);
	}

	public Gamma getFixedCostDistrib() {
		return fixedCostDistrib;
	}

	public Uniform getGetFromIgnoranceDistrib() {
		return getFromIgnoranceDistrib;
	}

	public Uniform getInitialQualityDistrib() {
		return initialQualityDistrib;
	}

	@SuppressWarnings("unchecked")
	public static void get() {

		get((Collection<Firm>) RepastEssentials.FindContext("Firms_Context"));

	}

	public static void get(Collection<Firm> firmsColl) {
		System.out.println("firm price quality perceivedQ");

		firmsColl.stream().forEach(f -> System.out.println("f" + f.getFirmNumID() + " " + f.getPrice() + " "
				+ f.getQuality() + " " + f.getPerceivedQuality(f.getQuality())));
	}

	public static void get(Collection<Firm> firmsColl, Firm omitFirm) {
		System.out.println("firm price quality perceivedQ");

		firmsColl.stream().filter(f -> (!f.equals(omitFirm))).forEach(f -> System.out.println("f" + f.getFirmNumID()
				+ " " + f.getPrice() + " " + f.getQuality() + " " + f.getPerceivedQuality(f.getQuality())));
	}

	public int getTotalDemand() {
		return marketStats.totalDemand;
	}

	public double getTotalSales() {
		return marketStats.totalSales;
	}
	
}
