package firms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeSet;
import demandSide.Market;
import offer.Offer;

public class ExpectedMarket extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;

	public ExpectedMarket(Firm omitFirm) {

		super(new CompareByPerceivedQ());
		Market.firms.stream().filter(f -> (!f.equals(omitFirm))).forEach(f -> add(f));

	}

	@Override
	public boolean add(Firm f) {

		assert (f != null);

		if (checkEntry(f)) {
			// Take out from the expelled firms
			takeOutExpelledFirms(f);
			return super.add(f);
		} else
			return false;
	}

	private boolean checkEntry(Firm f) {

		return getLoLimit(f) < getHiLimit(f);

	}

	private void takeOutExpelledFirms(Firm f) {

		Firm loF, hiF;
		BigDecimal q = f.getPerceivedQuality();
		BigDecimal p = f.getPrice();

		loF = lower(f);
		while (loF != null) {
			if ((getPriceToExpel(q, loF) == null) || (p.compareTo(getPriceToExpel(q, loF)) <= 0)) {
				remove(loF);
				loF = lower(loF);
			} else
				break;
		}

		hiF = higher(f);
		while (hiF != null) {
			if ((getPriceToExpel(q, hiF) == null) || (p.compareTo(getPriceToExpel(q, hiF)) <= 0)) {
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

	public Firm getLowerFirmGivenQ(BigDecimal perceivedQ) {

		// The reduce operation is to get the highest element of the ones that
		// have lower perceived q
		return stream().filter(f -> f.getPerceivedQuality().compareTo(perceivedQ) <= 0).reduce((a, b) -> b)
				.orElse(null);

	}

	public Firm getHigherFirmGivenQ(BigDecimal perceivedQ) {

		return stream().filter(f -> f.getPerceivedQuality().compareTo(perceivedQ) > 0).findFirst().orElse(null);

	}

	/*
	 * It returns the price below which f is expelled.
	 * 
	 * In case any price expels f, it returns NULL
	 * 
	 * In case no price expels f it returns 0
	 */
	public BigDecimal getPriceToExpel(BigDecimal q, Firm f) {
		
		assert (f != null);

		BigDecimal firmPerceivedQ = f.getPerceivedQuality();
		BigDecimal retval;

		if (q.compareTo(firmPerceivedQ) == 0)
			retval = f.getPrice();
		else if (firmPerceivedQ.compareTo(q) < 0) {
			// price + loLimit * (q - percQ)
			if (getLoLimit(f) == Double.POSITIVE_INFINITY)
				// Any price expels f
				retval = null;
			else
				retval = f.getPrice().add(BigDecimal.valueOf(getLoLimit(f)).multiply(q.subtract(firmPerceivedQ)));
		} else {
			// price - hiLimit * (percQ - q)
			if (getHiLimit(f) == Double.POSITIVE_INFINITY)
				// No price expels f
				retval = BigDecimal.ZERO;
			else
				retval = f.getPrice().subtract(BigDecimal.valueOf(getHiLimit(f)).multiply(firmPerceivedQ.subtract(q)));
		}

		// It rounds downward to make sure the price returned would expel f
		return retval.setScale(Offer.getPriceScale(), RoundingMode.FLOOR);
	}

	public static BigDecimal getMaxPriceToEnter(BigDecimal perceivedQ, Firm loF, Firm hiF) {

		if (hiF == null)
			return Offer.getMaxPrice();

		BigDecimal loP, loQ, hiP, hiQ;

		hiP = hiF.getPrice();
		hiQ = hiF.getPerceivedQuality();

		if (loF == null) {
			loP = BigDecimal.ZERO;
			loQ = BigDecimal.ZERO;
		} else {
			loP = loF.getPrice();
			loQ = loF.getPerceivedQuality();
		}

		if (hiQ.compareTo(loQ) <= 0)
			return BigDecimal.ZERO;
		else {

			// maxPrice = (hiP * (percQ - loQ) + loP * (hiQ - percQ)) / (hiQ -
			// loQ)
			BigDecimal num1 = hiP.multiply(perceivedQ.subtract(loQ));
			BigDecimal num2 = loP.multiply(hiQ.subtract(perceivedQ));
			BigDecimal num = num1.add(num2);
			BigDecimal denom = hiQ.subtract(loQ);

			// It rounds downward to make sure firm would enter
			return num.divide(denom, Offer.getPriceScale(), RoundingMode.FLOOR);
		}

	}

}
