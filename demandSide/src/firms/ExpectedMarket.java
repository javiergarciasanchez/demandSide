package firms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import consumers.Consumers;
import consumers.UtilityFunction;
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

	private double getLoLimit(Firm f) {
		
		Optional<Offer> loOf = Optional.ofNullable(lower(f)).map(lo -> lo.getPerceivedOffer());
		Optional<Offer> of = Optional.of(f.getPerceivedOffer());
		
		return Consumers.limitingWelfareParamPerceivedByFirms(loOf, of);
	}

	private double getHiLimit(Firm f) {
		
		Optional<Offer> hiOf = Optional.ofNullable(higher(f)).map(hi -> hi.getPerceivedOffer());
		Optional<Offer> of = Optional.of(f.getPerceivedOffer());
		
		return Consumers.limitingWelfareParamPerceivedByFirms(of, hiOf);
	}

	public Optional<Firm> getLowerFirmGivenQ(BigDecimal perceivedQ) {

		// The reduce operation is to get the highest element of the ones that
		// have lower perceived q
		// This is the last element because the tree is ordered
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
			// Any price below f's price expels f
			retval = f.getPrice();

		else if (q.compareTo(firmPerceivedQ) > 0)
			
			retval = UtilityFunction.priceToExpelFromAbove(q, f, Optional.ofNullable(lower(f)));
		
		else
			retval = UtilityFunction.priceToExpelFromBelow(q, f, Optional.ofNullable(higher(f)));		

		// It rounds downward to make sure the price returned would expel f
		return Optional.of(retval.setScale(Offer.getPriceScale(), RoundingMode.FLOOR));
	}

}
