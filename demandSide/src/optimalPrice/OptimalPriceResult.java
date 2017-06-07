package optimalPrice;

class OptimalPriceResult {
	double price = 0.;
	double margin = 0.;
	
	public OptimalPriceResult(double price, double margin){
		this.price = price;
		this.margin = margin;
		
	}
	
	public String toString(){
		return "Price: " + price + " - Margin: " + margin; 
	}
}
