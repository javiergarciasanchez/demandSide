package firms;

import java.math.BigDecimal;

import optimalPrice.OptimalPriceResult;

public class Decision {

	private Offer offer;

	public ExpectedInfo expInf = new ExpectedInfo();

	public Decision(OptimalPriceResult optPR, BigDecimal q) {

		offer = new Offer(optPR.price, q);
		expInf = optPR.expInf;

	}

	public Offer getOffer() {
		return offer;
	}

	public BigDecimal getPrice() {
		return offer.getPrice();
	}

	public void setPrice(BigDecimal price) {
		this.offer.setPrice(price);
	}

	public BigDecimal getQuality() {
		return offer.getQuality();
	}

	public void setQuality(BigDecimal quality) {
		this.offer.setQuality(quality);
	}

	public String toString() {
		return "Price: " + offer.getPrice() + " - Quality: " + offer.getQuality() + " " + expInf.toString();
	}

}
