package firmTypes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import firms.Firm;
import firms.Offer;

public class NoQReductionFirm extends Firm {

	public Stream<BigDecimal> getRealQualityOptions() {
		
		Stream.Builder<Optional<BigDecimal>> realQOpts = Stream.builder();
		
		BigDecimal currRealQ = getQuality();
		
		realQOpts.add(Optional.of(currRealQ));
		realQOpts.add(getClosestAvailableQuality(currRealQ.add(Offer.getQualityStep())));
		
		return realQOpts.build().filter(op->op.isPresent()).map(Optional::get);
		
	}
		
	public FirmTypes getFirmType() {
		return FirmTypes.NO_Q_REDUCTION_FIRM;
	}
	
}