package firmTypes;

import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class NoQReductionFirm extends Firm {

	public NoQReductionFirm(Market market) {
		super(market);
	}

	public Stream<Double> getRealQualityOptions() {
		
		double currRealQ = getQuality();		
		return Stream.of(currRealQ, currRealQ + Offer.getQualityStep());

	}
		
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_REDUCTION_FIRM;
	}
	
}
