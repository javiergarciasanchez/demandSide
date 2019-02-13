package optimalPrice;

import java.math.BigDecimal;

import firms.ExpectedInfo;

public class OptimalPriceResult {
	
	public BigDecimal price = BigDecimal.ZERO;
	public ExpectedInfo expInf;
	
	public OptimalPriceResult(){
		
		price = BigDecimal.ZERO;
		expInf = new ExpectedInfo();
		
	}
	
	public String toString(){
		return "Price: " + price + " " + expInf.toString(); 
	}
	
}
