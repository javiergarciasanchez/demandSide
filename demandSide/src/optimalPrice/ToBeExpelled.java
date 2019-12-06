package optimalPrice;

import java.util.Optional;

import firms.Firm;

public class ToBeExpelled {
	Firm f;
	// empty means any price expels the firm
	Optional<Double> optPriceToBeExpelled;
	
	public double getPriceToBeExpelled(){
		assert optPriceToBeExpelled.isPresent();
		return optPriceToBeExpelled.get();
	}
}