package firmTypes;

import java.util.stream.Stream;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class IncreaseQFirm extends Firm {

	public IncreaseQFirm(Market market) {
		super(market);
	}

	public Stream<Double> getRealQualityOptions() {

		return Stream.of(getQuality() + Offer.getQualityStep());

	}

	public FirmTypes getFirmType() {
		return FirmTypes.INCREASE_Q_FIRM;
	}

}
