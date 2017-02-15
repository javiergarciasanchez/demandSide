package offer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumers;

public class Offer {

	private double quality;
	private double price;

	// Read boundaries of parameters
	private static double absoluteMinPrice;
	private static double absoluteMaxPrice;
	private static double absoluteMinQuality;
	private static double absoluteMaxQuality;

	public static void resetStaticVars() {
		// resets static variables
		absoluteMinPrice = (double) GetParameter("minPrice");

		/*
		 * Max price is expanded so price is enough to cover the cost of the
		 * most expensive firm
		 */
		absoluteMaxPrice = (double) GetParameter("initialMaxPrice");

		absoluteMinQuality = (double) GetParameter("minQuality");
		absoluteMaxQuality = (double) GetParameter("maxQuality");
	}

	public Offer() {
	}

	public Offer(double p, double q) {
		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public boolean equal(Offer offer) {
		return ((price == offer.price) && (quality == offer.quality));
	}
	
	public boolean higher(Offer offer) {
		return ((price > offer.price) && (quality > offer.quality));
	}

	public void add(DeltaOffer deltaOffer) {
		price += deltaOffer.getPrice();
		quality += deltaOffer.getQuality();
	}

	public void setQuality(double q) {

		quality = q;
	}

	public double getQuality() {
		return quality;
	}

	public void setPrice(double price) {

		this.price = price;

	}

	public double getPrice() {
		return price;
	}


	/*
	 * Calculates the price that the poorest consumer may afford It is the
	 * minimum price a firm may offer meaningfully
	 */
	public static double getMinPrice(double quality) {
		return Consumers.getMinMargUtilOfQuality() * quality;
	}

	public static double getMaxInitialQuality() {
		// The purpose of this function is to avoid reaching the maxQ
		// Ideally a firm that increases its quality every step, shouldn't reach
		// maxQ
		return absoluteMaxQuality / 4.0;
	}

	public static double getAbsoluteMinPrice() {
		return absoluteMinPrice;
	}

	public static double getAbsoluteMaxPrice() {
		return absoluteMaxPrice;
	}

	public static void setAbsoluteMaxPrice(double p) {
		absoluteMaxPrice = p;
	}

	public static double getAbsoluteMinQuality() {
		return absoluteMinQuality;
	}

	public static double getAbsoluteMaxQuality() {
		return absoluteMaxQuality;
	}

	public String toString() {
		return "P: " + price + " Q: " + quality;
	}
}
