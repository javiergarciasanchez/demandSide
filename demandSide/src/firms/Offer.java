package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class Offer {
	
	private static int priceScale;
	private static int qualityScale;
	private static BigDecimal qualityStep;
	private static BigDecimal minDeltaPrice;
	private static BigDecimal minDeltaQuality;

	private BigDecimal quality = BigDecimal.ZERO;
	private BigDecimal price = BigDecimal.ZERO;
	
	public static void readOffersParams() {
		priceScale = (Integer) GetParameter("priceScale");
		qualityScale = (Integer) GetParameter("qualityScale");
		
		qualityStep = BigDecimal.valueOf((Double) GetParameter("qualityStep"));

		minDeltaPrice = BigDecimal.ONE.movePointLeft(priceScale).setScale(priceScale);
		minDeltaQuality = BigDecimal.ONE.movePointLeft(qualityScale).setScale(qualityScale);

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
