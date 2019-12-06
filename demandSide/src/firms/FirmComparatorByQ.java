package firms;

import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

public class FirmComparatorByQ implements Comparator<Firm> {

	@Override
	public int compare(Firm f1, Firm f2) {

		// If offers are equivalent return arbitrary order
		if (Offer.equivalentOffers(Optional.of(f1.getOffer()), Optional.of(f2.getOffer())))
			return -1;

		// Compare by quality
		double q1 = f1.getQuality();
		double q2 = f2.getQuality();

		if (q1 != q2)
			return (int) FastMath.signum(q1 - q2);

		// if quality is equal compare for price.
		// Note that as offers are equivalent prices should be different
		double p1 = f1.getPrice();
		double p2 = f2.getPrice();
		return (int) FastMath.signum(p1 - p2);

	}

}
