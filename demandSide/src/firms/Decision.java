package firms;

import optimalPrice.OptimalPriceResult;

public class Decision {

	private Offer offer;

	public ExpectedInfo expInf = new ExpectedInfo();

	public Decision(OptimalPriceResult optPR, double q) {

		offer = new Offer(optPR.price, q);
		expInf = optPR.expInf;

	}

	public Offer getOffer() {
		return offer;
	}

	public double getPrice() {
		return offer.getPrice();
	}

	public void setPrice(double price) {
		this.offer.setPrice(price);
	}

	public double getQuality() {
		return offer.getQuality();
	}

	public void setQuality(double quality) {
		this.offer.setQuality(quality);
	}

	public String toString() {
		return "Price: " + offer.getPrice() + " - Quality: " + offer.getQuality() + " " + expInf.toString();
	}

}
