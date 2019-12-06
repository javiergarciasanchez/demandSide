package graphs;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import firms.Firm;

public class Scale {

	private static double minP;
	private static double maxP;
	private static double minQ;
	private static double maxQ;

	public static void resetStaticVars() {
		// resets static variables
		minP = 0.0;
		maxP = 0.0;

		minQ = 0.0;
		maxQ = (double) GetParameter("maxInitialQuality");
	}

	public static void update(Firm f) {
		double p = f.getPrice();		
		minP = (p < minP) ? p : minP;
		maxP = (p > maxP) ? p : maxP;

		double q = f.getQuality();
		minQ = (q < minQ) ? q : minQ;
		maxQ = (q > maxQ) ? q : maxQ;

	}

	public static double getMinPrice() {
		return minP;
	}

	public static double getMaxPrice() {
		return maxP;
	}

	public static void setMaxPrice(double p) {
		maxP = p;
	}

	public static double getMinQuality() {
		return minQ;
	}

	public static double getMaxQuality() {
		return maxQ;
	}

}
