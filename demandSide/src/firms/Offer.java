package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import consumers.Consumers;
import demandSide.Market;

public class Offer {

	private static int priceScale, qualityScale;
	private static BigDecimal qualityStep;
	private static BigDecimal minDeltaPrice, minDeltaQuality;

	private BigDecimal quality = BigDecimal.ZERO;
	private BigDecimal price = BigDecimal.ZERO;

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
		
		qualityStep = BigDecimal.valueOf((Double) GetParameter("qualityStep"));

		minDeltaPrice = BigDecimal.ONE.movePointLeft(priceScale).setScale(priceScale);
		minDeltaQuality = BigDecimal.ONE.movePointLeft(qualityScale).setScale(qualityScale);

	}

	public static BigDecimal getMinQuality() {
		return getMinDeltaQuality();
	}

	public static BigDecimal getMinPrice(double cost, BigDecimal perceivedQ) {
		// Should be higher than cost
		BigDecimal costPlus = (BigDecimal.valueOf(cost)).add(minDeltaPrice);

		// Shouldn't be lower than the price needed to catch poorest consumer
		BigDecimal pricePoorest = Consumers.getMaxPriceForPoorestConsumer(perceivedQ);

		return costPlus.max(pricePoorest).setScale(getPriceScale(), RoundingMode.CEILING);
	}

	public static BigDecimal getMinPrice(Firm f, BigDecimal realQuality) {
		return getMinPrice(Firm.getUnitCost(realQuality), f.getPerceivedQuality(realQuality));
	}

	public static boolean equivalentOffers(Optional<Offer> loOf, Optional<Offer> hiOf) {

		if ((!loOf.isPresent()) && (!hiOf.isPresent()))
			return true;
		else if ((!loOf.isPresent()) || (!hiOf.isPresent()))
			return false;
		else
			// of1 and of2 both present
			return ((loOf.get().getPrice().compareTo(hiOf.get().getPrice()) == 0)
					&& (loOf.get().getQuality().compareTo(hiOf.get().getQuality()) == 0));
	}

	public static Optional<BigDecimal> getDownWardClosestAvailableQuality(BigDecimal q) {
		BigDecimal minQ = getMinQuality();

		// Search an available quality moving down
		while (Market.firms.firmsByQ.containsKey(q) && q.compareTo(minQ) > 0) {
			q = q.subtract(minDeltaQuality);
		}

		if (Market.firms.firmsByQ.containsKey(q))
			return Optional.empty();
		else
			return Optional.of(q.setScale(Offer.getQualityScale(), Offer.getQualityRounding()));

	}

	public static Optional<BigDecimal> getUpWardClosestAvailableQuality(BigDecimal q) {

		// Search an available quality moving upward
		while (Market.firms.firmsByQ.containsKey(q)) {
			q = q.add(minDeltaQuality);
		}

		return Optional.of(q.setScale(Offer.getQualityScale(), Offer.getQualityRounding()));

	}

	public static BigDecimal getMinDeltaPrice() {
		return minDeltaPrice;
	}

	public static BigDecimal getMinDeltaQuality() {
		return minDeltaQuality;
	}

	public static BigDecimal getQualityStep() {
		return qualityStep;
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
		return RoundingMode.HALF_UP;
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
