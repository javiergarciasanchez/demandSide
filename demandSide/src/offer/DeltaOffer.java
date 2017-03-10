package offer;

import org.apache.commons.math3.util.FastMath;

public class DeltaOffer {
	private double deltaPrice;
	private double deltaQuality;

	public DeltaOffer(double p, double q) {
		setDeltaPrice(p);
		setDeltaQuality(q);
	}

	public double getDeltaPrice() {
		return deltaPrice;
	}

	public double getDeltaQuality() {
		return deltaQuality;
	}

	public void setDeltaPrice(double price) {
		this.deltaPrice = price;
	}

	public void setDeltaQuality(double quality) {
		this.deltaQuality = quality;
	}

	public Offer addTo(Offer of) {
		return new Offer(of.getPrice() + deltaPrice, of.getQuality() + deltaQuality);
	}

	public static DeltaOfferSignCompare deltaOfferCompare(DeltaOffer dOffer1, DeltaOffer dOffer2) {

		double priceSign = FastMath.signum(dOffer1.getDeltaPrice() * dOffer2.getDeltaPrice());
		double qualitySign = FastMath.signum(dOffer1.getDeltaQuality() * dOffer2.getDeltaQuality());

		if ((priceSign == -1) && (qualitySign == -1))
			return DeltaOfferSignCompare.BOTH_UNEQUAL;

		else if ((priceSign == 1) && (qualitySign == -1))
			return DeltaOfferSignCompare.UNEQUAL_QUALITY;

		else if ((priceSign == -1) && (qualitySign == 1))
			return DeltaOfferSignCompare.UNEQUAL_PRICE;

		else
			return DeltaOfferSignCompare.BOTH_EQUAL;

	}

}
