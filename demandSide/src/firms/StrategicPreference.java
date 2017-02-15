package firms;

import decisionTools.ImprovingDeltaOffer;

public class StrategicPreference {

	public StrategicPreference(Firm firm) {
		// TODO Auto-generated constructor stub
	}

	public double selectPriceFromRange(double lo, double hi) {
		// TODO Auto-generated method stub
		return (lo+hi)/2.0;
	}
	
	public ImprovingDeltaOffer adjustImprovingDeltaOffer(ImprovingDeltaOffer dOffer){
		return dOffer;
	}

}
