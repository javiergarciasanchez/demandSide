package offer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import demandSide.Market;
import firms.Firm;

public class Offer {

	private double quality;
	private double price;

	public Offer() {
	}

	public Offer(double p, double q) {
		if (p < 0 || q < 0)
			throw new Error("Price and quality should be higher than zero");

		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public static Offer checkedAdd(Firm f, Offer realOffer, DeltaOffer deltaOffer) {

		// Quality should be higher than zero and different from other firms'
		// quality
		double q = realOffer.getQuality() + deltaOffer.getDeltaQuality();
		q = FastMath.max(q, getMinQuality());
		
		while (Market.firms.firmsByQ.containsKey(q))
			q = q + Double.MIN_VALUE;

		// Price should be higher than minPrice
		double p = realOffer.getPrice() + deltaOffer.getDeltaPrice();
		p = FastMath.max(p, getMinPrice(f, q));

		return new Offer(p, q);

	}

	public static double getMinQuality() {
		return 0. + Double.MIN_VALUE;
	}

	public static double getMinPrice(Firm f, double quality) {
		return getMinPrice( f.getUnitCost(quality), quality);
	}
	
	public static double getMinPrice(double cost, double quality) {

		// and should have the possibility of having a consumer (margUtil > p/q)
		// Thus p/q > minMargUtilTheta
		double minMargUtil = Consumers.getMinMargUtilOfQuality();

		return FastMath.max(cost, minMargUtil * quality) + Double.MIN_VALUE;

	}

	public static boolean equal(Offer loOffer, Offer hiOffer) {

		if ((loOffer == null) && (hiOffer == null))
			return true;
		else if ((loOffer == null) || (hiOffer == null))
			return false;
		else
			// of1 and of2 both not null
			return ((loOffer.price == hiOffer.price) && (loOffer.quality == hiOffer.quality));
	}

	/*
	 * Calculates the marginal utility of quality that divides consumer
	 * preferences. Consumers with a marginal utility of quality (muq) below
	 * "limit" will choose loOffer, while the ones with higher (muq) would
	 * choose hiOffer
	 */
	public static double limit(Offer loOf, Offer hiOf) {

		if (equal(loOf, hiOf))
			throw new Error("Offers should be different");

		if (loOf == null)
			return FastMath.max(hiOf.price / hiOf.quality, Consumers.getMinMargUtilOfQuality());

		if (hiOf == null)
			return Double.POSITIVE_INFINITY;

		double loQ, hiQ;
		loQ = loOf.getQuality();
		hiQ = hiOf.getQuality();

		double loP, hiP;
		loP = loOf.getPrice();
		hiP = hiOf.getPrice();

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
