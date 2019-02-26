package firmTypes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import firms.Firm;
import firms.Offer;

public class StandardFirm extends Firm {

	public Stream<BigDecimal> getRealQualityOptions() {
				
		Stream.Builder<Optional<BigDecimal>> realQOpts = Stream.builder();
		
		BigDecimal currRealQ = getQuality();
		
		realQOpts.add(Optional.of(currRealQ));
		
		// Quality should be higher than zero
		if (currRealQ.compareTo(Offer.getQualityStep()) > 0) 
			realQOpts.add(getClosestAvailableQuality(currRealQ.subtract(Offer.getQualityStep())));
		
		realQOpts.add(getClosestAvailableQuality(currRealQ.add(Offer.getQualityStep())));
		
		return realQOpts.build().filter(op->op.isPresent()).map(Optional::get);
		
	}
	
	public FirmTypes getFirmType() {
		return FirmTypes.STANDARD_FIRM;
	}
	
}
