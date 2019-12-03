package firmTypes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class IncreaseQFirm extends Firm {

	public IncreaseQFirm(Market market) {
		super(market);
	}

	public Stream<BigDecimal> getRealQualityOptions() {

		Stream.Builder<Optional<BigDecimal>> realQOpts = Stream.builder();

		BigDecimal currRealQ = getQuality();

		realQOpts.add(getClosestAvailableQuality(currRealQ.add(Offer.getQualityStep())));

		return realQOpts.build().filter(op -> op.isPresent()).map(Optional::get);

	}

	public FirmTypes getFirmType() {
		return FirmTypes.INCREASE_Q_FIRM;
	}

}
