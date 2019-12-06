package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.RoundingMode;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

public class Offer {

	private static int priceScale;
	private static int qualityScale;
	private static double qualityStep;
	private static double minDeltaPrice;
	private static double minDeltaQuality;

	private double quality = 0.;
	private double price = 0.;

	public static void readOffersParams() {
		priceScale = (Integer) GetParameter("priceScale");
		qualityScale = (Integer) GetParameter("qualityScale");

		qualityStep = (Double) GetParameter("qualityStep");

		minDeltaPrice = FastMath.pow(10., -priceScale);
		minDeltaQuality = FastMath.pow(10., -qualityScale);

	}

	public Offer(double p, double q) {

		assert ((p > 0) && (q > 0));

		setQuality(q);
		setPrice(p);
	}

	public Offer(Offer offer) {
		setQuality(offer.getQuality());
		setPrice(offer.getPrice());
	}

	public static boolean equivalentOffers(Optional<Offer> loOf, Optional<Offer> hiOf) {

		if ((!loOf.isPresent()) && (!hiOf.isPresent()))
			return true;
		else if ((!loOf.isPresent()) || (!hiOf.isPresent()))
			return false;
		else
			// of1 and of2 both present
			return ((loOf.get().getPrice() == hiOf.get().getPrice())
					&& (loOf.get().getQuality() == hiOf.get().getQuality()));
	}

	public static RoundingMode getPriceRounding() {
		return RoundingMode.HALF_DOWN;
	}

	public static RoundingMode getQualityRounding() {
		return RoundingMode.HALF_UP;
	}

	public void setQuality(double q) {
		this.quality = q;
	}

	public double getQuality() {
		return quality;
	}

	public void setPrice(double p) {

		this.price = p;

	}

	public static double getMinDeltaPrice() {
		return minDeltaPrice;
	}

	public static double getMinDeltaQuality() {
		return minDeltaQuality;
	}

	public static double getQualityStep() {
		return qualityStep;
	}

	public static int getPriceScale() {
		return priceScale;
	}

	public static int getQualityScale() {
		return qualityScale;
	}

	public double getPrice() {
		return price;
	}

	public String toString() {
		String pStr = Double.toString((double) FastMath.round(price * priceScale) / (double) priceScale);
		String qStr = Double.toString((double) FastMath.round(quality * qualityScale) / (double) qualityScale);
		return "P: " + pStr + " Q: " + qStr;
	}

}
