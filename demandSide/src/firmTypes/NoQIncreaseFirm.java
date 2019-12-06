package firmTypes;

import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class NoQIncreaseFirm extends Firm {

	public NoQIncreaseFirm(Market market) {
		super(market);
	}


	public Stream<Double> getRealQualityOptions() {
		
		double currRealQ = getQuality();
		double step = Offer.getQualityStep();
		
		// Quality should be higher than zero
		if (currRealQ > step)
			return Stream.of(currRealQ, currRealQ - step);
		else
			return Stream.of(currRealQ);	
		
	}

	
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_INCREASE_FIRM;
	}
	
}
