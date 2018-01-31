package offer;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.math3.util.FastMath;

import consumers.Consumers;
import demandSide.Market;
import firms.Firm;
import firms.StrategicPreference;
import repast.simphony.random.RandomHelper;

public class Offer {

	private static int priceScale, qualityScale;
	private static BigDecimal maxPrice, maxQuality;
	private static BigDecimal minDeltaPrice, minDeltaQuality;

	private BigDecimal quality;
	private BigDecimal price;

	public Offer() {
	}

	public Offer(BigDecimal p, BigDecimal q) {

		assert ((p.compareTo(BigDecimal.ZERO) > 0) && (q.compareTo(BigDecimal.ZERO) > 0));

		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public Offer(double priceD, double qualityD) {
		setQuality(BigDecimal.valueOf(qualityD));
		setPrice(BigDecimal.valueOf(priceD));
	}

	public static void resetStaticVars() {

		priceScale = (Integer) GetParameter("priceScale");
		qualityScale = (Integer) GetParameter("qualityScale");

		maxPrice = BigDecimal.valueOf((Integer) GetParameter("maxPrice"));
		maxQuality = BigDecimal.valueOf((Integer) GetParameter("maxQuality"));

		minDeltaPrice = BigDecimal.ONE.movePointLeft(priceScale).setScale(priceScale);
		minDeltaQuality = BigDecimal.ONE.movePointLeft(qualityScale).setScale(qualityScale);

	}

	public static BigDecimal getMinQuality() {
		return getMinDeltaQuality();
	}

	public static BigDecimal getMaxQuality() {
		return maxQuality;
	}

	public static BigDecimal getMinPrice(double cost, BigDecimal perceivedQ) {
		// Should be higher than cost
		BigDecimal costPlus = (BigDecimal.valueOf(cost)).add(minDeltaPrice);

		// Shouldn't be lower than the price needed to catch poorest consumer
		BigDecimal pricePoorest = Consumers.getMaxPriceForPoorestConsumer(perceivedQ);

		return costPlus.max(pricePoorest).setScale(getPriceScale(), RoundingMode.CEILING);
	}

	public static BigDecimal getMinPrice(Firm f, BigDecimal realQuality) {
		return getMinPrice(f.getUnitCost(realQuality), f.getPerceivedQuality(realQuality));
	}

	public static BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public static boolean equivalentOffers(Offer loOffer, Offer hiOffer) {

		if ((loOffer == null) && (hiOffer == null))
			return true;
		else if ((loOffer == null) || (hiOffer == null))
			return false;
		else
			// of1 and of2 both not null
			return ((loOffer.price.compareTo(hiOffer.price) == 0) && (loOffer.quality.compareTo(hiOffer.quality) == 0));
	}

	/*
	 * Calculates the marginal utility of quality that divides consumer
	 * preferences. Consumers with a marginal utility of quality (muq) below
	 * "limit" will choose loOffer, while the ones with higher (muq) would
	 * choose hiOffer When there is no limit, ie hiOf demand is zero, function
	 * returns null
	 */
	public static double limit(Offer loOf, Offer hiOf) {

		if (equivalentOffers(loOf, hiOf))
			throw new Error("Offers should be different");

		if (loOf == null)
			// Note that hiOf is not null, otherwise they would be equal
			return Consumers.getMinMargUtilOfQualityAceptingOffer(hiOf);

		if (hiOf == null)
			return Double.POSITIVE_INFINITY;

		BigDecimal loQ, hiQ;
		loQ = loOf.getQuality();
		hiQ = hiOf.getQuality();

		BigDecimal loP, hiP;
		loP = loOf.getPrice();
		hiP = hiOf.getPrice();

		if (loQ.compareTo(hiQ) > 0)
			throw new Error("higher offer quality should be >= than low offer quality");

		else if (loP.compareTo(hiP) >= 0)
			// no consumer would choose lower offer
			return Consumers.getMinMargUtilOfQuality();

		else if (loQ.compareTo(hiQ) == 0) {
			// loP < hiP, no consumer would choose higher offer
			return Double.POSITIVE_INFINITY;

		} else {
			// loQ < hiQ and loP < hiP
			BigDecimal deltaP = hiP.subtract(loP);
			BigDecimal deltaQ = hiQ.subtract(loQ);

			double limit = deltaP.doubleValue() / deltaQ.doubleValue();

			return FastMath.max(limit, Consumers.getMinMargUtilOfQuality());

		}

	}

	public static double limit(Double loP, Double loQ, Double hiP, Double hiQ) {

		if ((loP == hiP) && (loQ == hiQ))
			throw new Error("Offers should be different");

		if (((loP == null) && (loQ != null)) || ((loP != null) && (loQ == null)))
			throw new Error("low offer has inconsistent values");

		if (((hiP == null) && (hiQ != null)) || ((hiP != null) && (hiQ == null)))
			throw new Error("high offer has inconsistent values");

		if (loP == null)
			// Note that hiP and hiQ are not null, otherwise they would be equal
			// Also note that loQ is null
			return Consumers.getMinMargUtilOfQualityAceptingOffer(new Offer(hiP, hiQ));

		if (hiP == null)
			return Double.POSITIVE_INFINITY;

		if (loQ > hiQ)
			throw new Error("higher offer quality should be >= than low offer quality");

		else if (loP >= hiP)
			// no consumer would choose lower offer
			return Consumers.getMinMargUtilOfQuality();

		else if (loQ == hiQ) {
			// loP < hiP, no consumer would choose higher offer
			return Double.POSITIVE_INFINITY;

		} else
			
			return FastMath.max((hiP - loP) / (hiQ - loQ), Consumers.getMinMargUtilOfQuality());

	}

	public static DeltaOffer minus(Offer a, Offer b) {
		// Calculates a - b
		BigDecimal dP = a.getPrice().subtract(b.getPrice());
		BigDecimal dQ = a.getQuality().subtract(b.getQuality());

		return new DeltaOffer(dP, dQ);
	}

	public static BigDecimal getRandomQuality(StrategicPreference stratPref) {

		double maxIniQ = (double) GetParameter("maxInitialQuality");
		BigDecimal q = BigDecimal.valueOf(RandomHelper.nextDoubleFromTo(0.0, maxIniQ));

		if (stratPref.forQuality()) {
			q = getUpWardClosestAvailableQuality(q);

			if (q == null)
				q = getDownWardClosestAvailableQuality(q);

		} else {
			q = getDownWardClosestAvailableQuality(q);

			if (q == null)
				q = getUpWardClosestAvailableQuality(q);
		}

		return q.setScale(Offer.getQualityScale(), Offer.getQualityRounding());

	}

	public static BigDecimal getDownWardClosestAvailableQuality(BigDecimal q) {
		BigDecimal minQ = getMinQuality();

		// Search an available quality moving down
		while (Market.firms.firmsByQ.containsKey(q) && q.compareTo(minQ) > 0) {
			q = q.subtract(minDeltaQuality);
		}

		if (Market.firms.firmsByQ.containsKey(q))
			return null;
		else
			return q;

	}

	public static BigDecimal getUpWardClosestAvailableQuality(BigDecimal q) {

		// Search an available quality moving down
		while (Market.firms.firmsByQ.containsKey(q) && q.compareTo(maxQuality) < 0) {
			q = q.add(minDeltaQuality);
		}

		if (Market.firms.firmsByQ.containsKey(q))
			return null;
		else
			return q;

	}

	public static BigDecimal getMinDeltaPrice() {
		return minDeltaPrice;
	}

	public static BigDecimal getMinDeltaQuality() {
		return minDeltaQuality;
	}

	public static int getPriceScale() {
		return priceScale;
	}

	public static int getQualityScale() {
		return qualityScale;
	}

	public static RoundingMode getPriceRounding() {
		return RoundingMode.HALF_DOWN;
	}

	public static RoundingMode getQualityRounding() {
		return RoundingMode.HALF_DOWN;
	}

	public void setQuality(BigDecimal q) {
		this.quality = q.setScale(getQualityScale(), getQualityRounding());
	}

	public void setQuality(double q) {
		this.quality = BigDecimal.valueOf(q).setScale(getQualityScale(), getQualityRounding());
	}

	public BigDecimal getQuality() {
		return quality;
	}

	public void setPrice(BigDecimal p) {

		this.price = p.setScale(getPriceScale(), getPriceRounding());

	}

	public void setPrice(double p) {
		this.price = BigDecimal.valueOf(p).setScale(getPriceScale(), getPriceRounding());
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String toString() {
		return "P: " + price.toPlainString() + " Q: " + quality.toPlainString();
	}

}
