package firms;

import java.math.BigDecimal;

import optimalPrice.OptimalPriceResult;

public class Decision {
	private Offer offer;
	private double expectedGrossProfit;
	private double expectedDemand;

	public Decision(OptimalPriceResult optPR, BigDecimal q) {
		offer = new Offer(optPR.getPrice(), q);
		expectedGrossProfit = optPR.getExpectedGrossProfit();
		expectedDemand = optPR.getExpectedDemand();
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

	public double getExpectedGrossProfit() {
		return expectedGrossProfit;
	}

	public void setExpectedGrossProfit(double grossProfit) {
		this.expectedGrossProfit = grossProfit;
	}

	public double getExpectedDemand() {
		return expectedDemand;
	}

	public void setExpectedDemand(double expectedDemand) {
		this.expectedDemand = expectedDemand;
	}

	public String toString() {
		return "Price: " + offer.getPrice() + " - Quality: " + offer.getQuality() + " - Expected Gross Profit: "
				+ expectedGrossProfit + " - Expected Demand: " + expectedDemand;
	}

}
