package optimalPrice;

import firms.ExpectedInfo;

public class OptimalPriceResult {
	
	public double price = 0.0;
	public ExpectedInfo expInf;
	
	public OptimalPriceResult(){
		
		price = 0.0;
		expInf = new ExpectedInfo();
		
	}
	
	public String toString(){
		return "Price: " + price + " " + expInf.toString(); 
	}
	
}
