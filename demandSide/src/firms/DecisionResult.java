package firms;

import java.math.BigDecimal;

public class DecisionResult {
	private BigDecimal price, quality;
	private double margin;

	public DecisionResult(BigDecimal price, BigDecimal realQ, double margin) {
		this.price = price;
		this.quality = realQ;
		this.margin = margin;

	}

	public String toString() {
		return "Price: " + price + " - Quality: " + quality + " - Margin: " + margin;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getQuality() {
		return quality;
	}

	public void setQuality(BigDecimal quality) {
		this.quality = quality;
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

}
