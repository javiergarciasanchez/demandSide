package offer;

import org.apache.commons.math3.util.FastMath;

public class DeltaOffer {
	private double deltaPrice;
	private double deltaQuality;

	public DeltaOffer(double p, double q) {
		setPrice(p);
		setQuality(q);
	}

	public double getPrice() {
		return deltaPrice;
	}

	public double getQuality() {
		return deltaQuality;
	}

	public void setPrice(double price) {
		this.deltaPrice = price;
	}

	public void setQuality(double quality) {
		this.deltaQuality = quality;
	}

	public Offer addTo(Offer of) {
		return new Offer(of.getPrice() + deltaPrice, of.getQuality() + deltaQuality);
	}

	public static DeltaOfferCompare deltaOfferCompare(DeltaOffer dOffer1, DeltaOffer dOffer2) {
		
		double priceSign = FastMath.signum(dOffer1.getPrice() * dOffer2.getPrice());
		double qualitySign = FastMath.signum(dOffer1.getQuality() * dOffer2.getQuality());

		if ((priceSign == -1) && (qualitySign == -1))
			return DeltaOfferCompare.BOTH_UNEQUAL;

		else if ((priceSign == 1) && (qualitySign == -1))
			return DeltaOfferCompare.UNEQUAL_QUALITY;

		else if ((priceSign == -1) && (qualitySign == 1))
			return DeltaOfferCompare.UNEQUAL_PRICE;

		else
			return DeltaOfferCompare.BOTH_EQUAL;

	}

}
