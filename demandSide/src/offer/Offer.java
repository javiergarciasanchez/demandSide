package offer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;

public class Offer {

	private double quality;
	private double price;

	public Offer() {
	}

	public Offer(double p, double q) {
		if (p<0 || q <0)
			throw new Error("Price and quality shoulb be higher than zero");
		
		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public static boolean equal(Offer of1, Offer of2) {
		if ((of1 == null) && (of2 == null))
			return true;
		else if ((of1 == null) || (of2 == null))
			return false;
		else
			// of1 and of2 both not null
			return ((of1.price == of2.price) && (of1.quality == of2.quality));
	}


	/*
	 * Calculates the marginal utility of quality that divides consumer
	 * preferences. Consumers with a marginal utility of quality (muq) below
	 * "limit" will choose loOffer, while the ones with higher (muq) would
	 * choose hiOffer
	 */
	public static double limit(Offer loOffer, Offer hiOffer) {

		if (equal(loOffer, hiOffer))
			throw new Error("Offers should be different");

		if (loOffer == null)
			return Consumers.getMinMargUtilOfQuality();

		if (hiOffer == null)
			return Double.POSITIVE_INFINITY;

		double loQ, hiQ;
		loQ = loOffer.getQuality();
		hiQ = hiOffer.getQuality();

		double loP, hiP;
		loP = loOffer.getPrice();
		hiP = hiOffer.getPrice();

		if (loQ > hiQ)
			throw new Error("higher offer quality should be >= than low offer quality");

		else if (loP >= hiP)
			// no consumer would choose lower offer
			return Consumers.getMinMargUtilOfQuality();

		else if (loQ == hiQ) {
			// loP < hiP, no consumer would choose higher offer
			return Double.POSITIVE_INFINITY;

		} else {
			// loQ < hiQ and loP < hiP
			return FastMath.max((hiP - loP) / (hiQ - loQ), Consumers.getMinMargUtilOfQuality());

		}

	}

	public void add(DeltaOffer deltaOffer) {
		price += deltaOffer.getDeltaPrice();
		quality += deltaOffer.getDeltaQuality();
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

	public static double getMaxInitialQuality() {
		return (double) GetParameter("maxInitialQuality");
	}

	public String toString() {
		return "P: " + price + " Q: " + quality;
	}
}
