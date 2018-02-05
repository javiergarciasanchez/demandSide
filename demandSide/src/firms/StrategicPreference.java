package firms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class StrategicPreference {

	public StrategicPreference(Firm firm) {
		// TODO Auto-generated constructor stub
	}

	public double selectPriceFromRange(double lo, double hi) {
		// TODO Auto-generated method stub
		return (lo + hi) / 2.0;
	}

	private boolean forQuality() {
		return true;
	}

	public Optional<BigDecimal> getClosestAvailableQuality(BigDecimal q) {
		Optional<BigDecimal> up, down;

		up = Offer.getUpWardClosestAvailableQuality(q);
		down = Offer.getDownWardClosestAvailableQuality(q);

		if (forQuality()) {

			if (up.isPresent())
				return up;
			else
				return down;

		} else {

			if (down.isPresent())
				return down;
			else
				return up;
		}

	}

	public Stream<BigDecimal> getRealQualityOptions(BigDecimal currRealQ) {

		ArrayList<Optional<BigDecimal>> realQOpts = new ArrayList<Optional<BigDecimal>>(3);

		realQOpts.add(Optional.of(currRealQ));
		realQOpts.add(getClosestAvailableQuality(currRealQ.subtract(Offer.getQualityStep())));
		realQOpts.add(getClosestAvailableQuality(currRealQ.add(Offer.getQualityStep())));

		return realQOpts.stream().filter(op->op.isPresent()).map(Optional::get);
	}

}
