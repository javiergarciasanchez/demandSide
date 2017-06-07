package firms;

import java.util.TreeSet;
import demandSide.Market;
import offer.Offer;

public class FirmsPerceivedQSegments extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;

	public FirmsPerceivedQSegments() {

		super(new CompareByPerceivedQ());

		Market.firms.firmsByQ.forEach((q, f) -> add(f));

	}

	@Override
	public boolean add(Firm f) {
		if (f == null)
			throw new Error("Cannot add null firm");
		
		if (checkEntry(f)) {
			// Take out from the market expelled firms
			takeOutExpelledFirms(f);
			return super.add(f);
		} else
			return false;
	}

	/*
	 * @Override public boolean remove(Object f) {
	 * 
	 * boolean retval = super.remove(f);
	 * 
	 * // after remove check if any of the firms that are out of the market may
	 * // enter Market.firms.stream().filter(e -> (!contains(e) &&
	 * checkEntry(e))).forEach(e -> super.add(e));
	 * 
	 * return retval; }
	 */

	private boolean checkEntry(Firm f) {

		return getLoLimit(f) < getHiLimit(f);

	}

	private void takeOutExpelledFirms(Firm f) {

		Firm loF, hiF;
		double q = f.getPerceivedQuality();
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

	private Offer getLoOffer(Firm f) {

		Firm lo = lower(f);
		return (lo != null) ? lo.getPerceivedOffer() : null;

	}

	private Offer getHiOffer(Firm f) {

		Firm hi = higher(f);
		return (hi != null) ? hi.getPerceivedOffer() : null;

	}

	private double getLoLimit(Firm f) {
		
		return Offer.limit(getLoOffer(f), f.getPerceivedOffer());

	}

	private double getHiLimit(Firm f) {

		return Offer.limit(f.getPerceivedOffer(), getHiOffer(f));

	}

	public Firm getLowerFirmGivenQ(double perceivedQ) {

		// The reduce operation is to get the highest element of the ones that
		// have lower perceived q
		return stream().filter(f -> f.getPerceivedQuality() < perceivedQ).reduce((a, b) -> b).orElse(null);

	}

	public Firm getHigherFirmGivenQ(double perceivedQ) {

		return stream().filter(f -> f.getPerceivedQuality() > perceivedQ).findFirst().orElse(null);

	}

	/*
	 * public double getExpectedDemand(Firm f) {
	 * 
	 * if ((f == null) || !contains(f)) return 0; else return
	 * Consumers.expectedQuantity(f.getPerceivedOffer(), getLoOffer(f),
	 * getHiOffer(f));
	 * 
	 * }
	 */

	// Note that it could return negative infinity
	public double getPriceToExpel(double perceivedQ, Firm f) {
		if (f == null)
			throw new Error("Cannot get a price to expel null firm");

		double firmPerceivedQ = f.getPerceivedQuality();
		double retval;
		
		if (perceivedQ == firmPerceivedQ)
			retval = f.getPrice();
		else if (firmPerceivedQ < perceivedQ)
			retval = f.getPrice() + getLoLimit(f) * (perceivedQ - firmPerceivedQ);
		else
			retval = f.getPrice() - getHiLimit(f) * (firmPerceivedQ - perceivedQ);
		
		return retval - Double.MIN_VALUE;
	}

}
