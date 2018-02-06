package firms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import demandSide.Market;

public class ExpectedMarket extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;

	public ExpectedMarket(Firm omitFirm) {

		super(new CompareByPerceivedQ());
		Market.firms.stream().filter(f -> (!f.equals(omitFirm))).forEach(this::add);

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

	private void takeOutExpelledFirms(Firm firm) {

		BigDecimal q = firm.getPerceivedQuality();
		BigDecimal p = firm.getPrice();

		Stream.concat(headSet(firm).stream(), tailSet(firm, false).stream())
				.filter(f -> getPriceToExpel(q, Optional.of(f)).isPresent())
				.filter(f -> p.compareTo(getPriceToExpel(q, Optional.of(f)).get()) <= 0).collect(Collectors.toList())
				.forEach(this::remove);
		
	}

	private Optional<Offer> getLoOffer(Firm f) {
		return Optional.ofNullable(lower(f)).map(lo -> lo.getPerceivedOffer());
	}

	private Optional<Offer> getHiOffer(Firm f) {
		return Optional.ofNullable(higher(f)).map(hi -> hi.getPerceivedOffer());
	}

	private double getLoLimit(Firm f) {
		return Offer.limit(getLoOffer(f), Optional.of(f.getPerceivedOffer()));
	}

	private double getHiLimit(Firm f) {
		return Offer.limit(Optional.of(f.getPerceivedOffer()), getHiOffer(f));
	}

	public Optional<Firm> getLowerFirmGivenQ(BigDecimal perceivedQ) {

		// The reduce operation is to get the highest element of the ones that
		// have lower perceived q
		return stream().filter(f -> f.getPerceivedQuality().compareTo(perceivedQ) <= 0).reduce((a, b) -> b);

	}

	public Optional<Firm> getHigherFirmGivenQ(BigDecimal perceivedQ) {

		return stream().filter(f -> f.getPerceivedQuality().compareTo(perceivedQ) > 0).findFirst();

	}

	/*
	 * It returns the price below which f is expelled.
	 * 
	 * In case any price expels f, it returns empty
	 * 
	 * In case no price expels f it returns 0
	 */
	public Optional<BigDecimal> getPriceToExpel(BigDecimal q, Optional<Firm> optF) {

		Firm f;

		if (optF.isPresent())
			f = optF.get();
		else
			return Optional.empty();

		BigDecimal firmPerceivedQ = f.getPerceivedQuality();
		BigDecimal retval;

		if (q.compareTo(firmPerceivedQ) == 0)
			retval = f.getPrice();

		else if (firmPerceivedQ.compareTo(q) < 0) {
			// price + loLimit * (q - percQ)
			if (getLoLimit(f) == Double.POSITIVE_INFINITY)
				// Any price expels f
				return Optional.empty();
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
		return Optional.of(retval.setScale(Offer.getPriceScale(), RoundingMode.FLOOR));
	}

	public static BigDecimal getMaxPriceToEnter(BigDecimal perceivedQ, Optional<Firm> loF, Optional<Firm> hiF) {

		BigDecimal loP, loQ, hiP, hiQ;

		if (!hiF.isPresent())
			return Offer.getMaxPrice();

		// note that null will never be assigned because hiF is present
		hiP = hiF.map(Firm::getPrice).orElse(null);
		hiQ = hiF.map(Firm::getQuality).orElse(null);

		loP = loF.map(Firm::getPrice).orElse(BigDecimal.ZERO);
		loQ = loF.map(Firm::getPerceivedQuality).orElse(BigDecimal.ZERO);

		if (hiQ.compareTo(loQ) <= 0)
			return BigDecimal.ZERO;
		else

		{

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
