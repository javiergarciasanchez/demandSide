package optimalPrice;

import java.math.BigDecimal;

public class OptimalPriceResult {
	BigDecimal price = BigDecimal.ZERO;
	double margin = 0.;
	
	public OptimalPriceResult(BigDecimal price, double margin){
		this.price = price;
		this.margin = margin;
		
	}
	
	public String toString(){
		return "Price: " + price + " - Margin: " + margin; 
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public double getMargin() {
		return margin;
	}
	
}
