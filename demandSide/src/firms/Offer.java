package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import consumers.Consumers;

public class Offer {

	private double quality;
	private double price;

	// Read boundaries of parameters
	private static double minPrice;
	private static double maxPrice;
	private static double minQuality;
	private static double maxQuality;

	public static void resetStaticVars() {
		// resets static variables
		minPrice = (double) GetParameter("minPrice");

		/*
		 * Max price is expanded so price is enough to cover the cost of the
		 * most expensive firm
		 */
		maxPrice = (double) GetParameter("initialMaxPrice");

		minQuality = (double) GetParameter("minQuality");
		maxQuality = (double) GetParameter("maxQuality");
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

	public void modify(DeltaOffer deltaOffer) {

		deltaOffer.addTo(this);

	}

	public void setQuality(double q) {

		// Check limits
		q = Math.max(q, minQuality);
		q = Math.min(q, maxQuality);

		quality = q;
	}

	public double getQuality() {
		return quality;
	}

	public void setPrice(double price) {

		// Price should be between limits
		price = Math.max(price, minPrice);
		price = Math.min(price, maxPrice);

		this.price = price;

	}

	public double getPrice() {
		return price;
	}

	public static double limitOperator(Offer loOffer, Offer hiOffer) {
		double loP, hiP, loQ, hiQ;

		loP = loOffer.getPrice();
		hiP = hiOffer.getPrice();

		loQ = loOffer.getQuality();
		hiQ = hiOffer.getQuality();

		// Note that it might return negative or positive infinity if loQ = hiQ
		if (loQ != hiQ)
			return (hiP - loP) / (hiQ - loQ);
		else if (hiP > loP)
			return Double.POSITIVE_INFINITY;
		else if (hiP < loP)
			return Consumers.getMinMargUtilOfQuality();
		else
			return 0.0;

	}

	/*
	 * Calculates the price that the poorest consumer may afford It is the
	 * minimum price a firm may offer meaningfully
	 */
	public static double getMinPrice(double quality) {
		return Consumers.getMinMargUtilOfQuality() * quality;
	}

	public static double getMinPrice() {
		return minPrice;
	}

	public static double getMaxPrice() {
		return maxPrice;
	}

	public static void setMaxPrice(double p) {
		maxPrice = p;
	}

	public static double getMinQuality() {
		return minQuality;
	}

	public static double getMaxQuality() {
		return maxQuality;
	}

	public static double getMaxInitialQuality() {
		// The purpose of this function is to avoid reaching the maxQ
		// Ideally a firm that increases its quality every step, shouldn't reach
		// maxQ
		return maxQuality / 4.0;
	}

	public String toString() {
		return "Q: " + quality + " P: " + price;
	}
}
