package firmTypes;

import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;

public class NoQChangeFirm extends Firm {

	public NoQChangeFirm(Market market) {
		super(market);
	}

	public Stream<Double> getRealQualityOptions() {
		return Stream.of(getQuality());	
	}
		
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_CHANGE_FIRM;
	}

}
