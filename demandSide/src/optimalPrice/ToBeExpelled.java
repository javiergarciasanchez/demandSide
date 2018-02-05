package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;

import firms.Firm;

public class ToBeExpelled {
	Firm f;
	// empty means any price expels the firm
	Optional<BigDecimal> optPriceToBeExpelled;
	
	public BigDecimal getPriceToBeExpelled(){
		assert optPriceToBeExpelled.isPresent();
		return optPriceToBeExpelled.get();
	}
}