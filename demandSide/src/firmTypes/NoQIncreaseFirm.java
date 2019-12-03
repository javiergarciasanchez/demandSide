package firmTypes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class NoQIncreaseFirm extends Firm {

	public NoQIncreaseFirm(Market market) {
		super(market);
	}


	public Stream<BigDecimal> getRealQualityOptions() {
		
		Stream.Builder<Optional<BigDecimal>> realQOpts = Stream.builder();
		
		BigDecimal currRealQ = getQuality();
		
		realQOpts.add(Optional.of(currRealQ));
		
		// Quality should be higher than zero
		if (currRealQ.compareTo(Offer.getQualityStep()) > 0) 
			realQOpts.add(getClosestAvailableQuality(currRealQ.subtract(Offer.getQualityStep())));
		
		return realQOpts.build().filter(op->op.isPresent()).map(Optional::get);
		
	}

	
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_INCREASE_FIRM;
	}
	
}
