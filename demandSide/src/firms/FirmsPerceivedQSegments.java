package firms;

import java.util.Comparator;
import java.util.TreeSet;
import demandSide.Market;
import offer.Offer;
import consumers.Consumers;

public abstract class FirmsSegments extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;

	public FirmsSegments(Comparator<Firm> compBy) {
		super(compBy);
	}

	@Override
	public boolean add(Firm f) {
		if (checkEntry(f)) {
			// Take out from the market expelled firms
			takeOutExpelledFirms(f);
			return super.add(f);
		} else
			return false;
	}

	@Override
	public boolean remove(Object f) {

		boolean retval = super.remove(f);

		// after remove check if any of the firms that are out of the market may
		// enter
		Market.firms.stream().filter(e -> (!contains(e) && checkEntry(e))).forEach(e -> super.add(e));

		return retval;
	}

	public Offer getOffer(Firm f) {
		if (f == null)
			return null;

		Offer retval = new Offer(f.getOffer());
		retval.setQuality(getQuality(f));

		return retval;

	}

	// Quality according to segments ordering
	public abstract double getQuality(Firm f);

	public abstract double getQuality(Firm f, double realQ);

	private boolean checkEntry(Firm f) {
		if (f == null)
			throw new Error("Cannot check entry of null firm");

		Firm lo = lower(f);
		Firm hi = higher(f);

		// limit takes into account the possibility of offers being null
		return Offer.limit(getOffer(lo), getOffer(f)) < Offer.limit(getOffer(f), getOffer(hi));

	}

	private void takeOutExpelledFirms(Firm f) {

		Firm loF, hiF;
		double q = getQuality(f);
		double p = f.getPrice();

		loF = lower(f);
		while (loF != null) {
			if (p < getPriceToExpel(q, loF)) {
				remove(loF);
				loF = lower(loF);
			} else
				break;
		}

		hiF = higher(f);
		while (hiF != null) {
			if (p > getPriceToExpel(q, hiF)) {
				remove(hiF);
				hiF = higher(hiF);
			} else
				break;
		}

	}

	private double getLoLimit(Firm f) {
		if (f == null)
			throw new Error("Firm value cannot be null");

		return Offer.limit(getOffer(lower(f)), getOffer(f));

	}

	private double getHiLimit(Firm f) {
		if (f == null)
			throw new Error("Firm value cannot be null");

		return Offer.limit(getOffer(f), getOffer(higher(f)));

	}

	public Firm getLowerFirmGivenQ(double q) {

		return stream().filter(f -> getQuality(f) < q).max(comparator()).orElse(null);

	}

	public Firm getHigherFirmGivenQ(double q) {

		return stream().filter(f -> getQuality(f) > q).findFirst().orElse(null);

	}

	public double getExpectedDemand(Firm f) {

		if (!contains(f))
			return 0;
		else
			return Consumers.expectedQuantity(getOffer(f), getOffer(lower(f)), getOffer(higher(f)));

	}

	// Note that it could return negative infinity
	public double getPriceToExpel(double q, Firm f) {
		if (f == null)
			throw new Error("Cannot get a price to expel null firm");
		

		double qF = getQuality(f);
		
		if (q == qF)
			return f.getPrice() - Double.MIN_NORMAL;
		else if (qF < q)
			return f.getPrice() + getLoLimit(f) * (q - qF);
		else
			return f.getPrice() - getHiLimit(f) * (qF - q);
	}

}
