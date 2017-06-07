package firms;

import improvingOffer.ImprovingOffer;

public class StrategicPreference {

	public StrategicPreference(Firm firm) {
		// TODO Auto-generated constructor stub
	}

	public double selectPriceFromRange(double lo, double hi) {
		// TODO Auto-generated method stub
		return (lo+hi)/2.0;
	}
	
	public ImprovingOffer adjustImprovingDeltaOffer(ImprovingOffer dOffer){
		return dOffer;
	}

}
