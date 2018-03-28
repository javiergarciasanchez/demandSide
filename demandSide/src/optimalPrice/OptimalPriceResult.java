package optimalPrice;

import java.math.BigDecimal;

public class OptimalPriceResult {
	BigDecimal price = BigDecimal.ZERO;
	private double expectedGrossProfit = 0.;
	private double expectedDemand = 0.;
	
	public void setExpectedDemand(double expectedDemand) {
		this.expectedDemand = expectedDemand;
	}

	public OptimalPriceResult(BigDecimal price, double grossProfit){
		this.price = price;
		this.expectedGrossProfit = grossProfit;
		
	}
	
	public String toString(){
		return "Price: " + price + " - Gross Profit: " + expectedGrossProfit; 
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public double getExpectedGrossProfit() {
		return expectedGrossProfit;
	}
	
	public void setExpectedGrossProfit(double expectedGrossProfit) {
		this.expectedGrossProfit = expectedGrossProfit;
	}

	public double getExpectedDemand() {
		return expectedDemand;
	}
	
}
