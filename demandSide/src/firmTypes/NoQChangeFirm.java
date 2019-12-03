package firmTypes;

import java.math.BigDecimal;
import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;

public class NoQChangeFirm extends Firm {

	public NoQChangeFirm(Market market) {
		super(market);
	}

	public Stream<BigDecimal> getRealQualityOptions() {
		
		Stream.Builder<BigDecimal> realQOpts = Stream.builder();;		
		
		realQOpts.add(getQuality());
		
		return realQOpts.build();		
		
	}
		
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_CHANGE_FIRM;
	}

}
