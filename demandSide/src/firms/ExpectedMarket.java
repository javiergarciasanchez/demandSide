package firms;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import consumers.Consumers;
import consumers.UtilityFunction;
import demandSide.Market;

public class ExpectedMarket extends TreeSet<Firm> {

	private static final long serialVersionUID = 1L;
	Firm owner;
	Market market;
	Consumers consumers;

	public ExpectedMarket(Firm owner) {

		super(new CompareByPerceivedQ(owner));

		this.owner = owner;
		market = owner.market;
		consumers = market.consumers;

		market.firms.stream().filter(f -> (!f.equals(owner))).forEach(this::add);

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

		Offer of = owner.getCompetitorPerceivedOffer(firm);
		BigDecimal q = of.getQuality();
		BigDecimal p = of.getPrice();

		Stream.concat(headSet(firm).stream(), tailSet(firm, false).stream())
				.filter(f -> getPriceToExpel(q, Optional.of(f)).isPresent())
				.filter(f -> p.compareTo(getPriceToExpel(q, Optional.of(f)).get()) <= 0).collect(Collectors.toList())
				.forEach(this::remove);

	}

	private double getLoLimit(Firm f) {

		Optional<Offer> loOf, of;

		loOf = Optional.ofNullable(lower(f)).map(loF -> owner.getCompetitorPerceivedOffer(loF));

		of = Optional.of(owner.getCompetitorPerceivedOffer(f));

		return consumers.limitingWelfareParamPerceivedByFirms(loOf, of);
	}

	private double getHiLimit(Firm f) {

		Optional<Offer> hiOf, of;

		hiOf = Optional.ofNullable(higher(f)).map(hiF -> owner.getCompetitorPerceivedOffer(hiF));

		of = Optional.of(owner.getCompetitorPerceivedOffer(f));

		return consumers.limitingWelfareParamPerceivedByFirms(of, hiOf);

	}

	public Optional<Firm> getLowerFirmGivenQ(BigDecimal perceivedQ) {

		// The reduce operation is to get the highest element of the ones that
		// have lower perceived q
		// This is the last element because the tree is ordered
		return stream().filter(f -> owner.getCompetitorPerceivedOffer(f).getQuality().compareTo(perceivedQ) <= 0)
				.reduce((a, b) -> b);

	}

	public Optional<Firm> getHigherFirmGivenQ(BigDecimal perceivedQ) {

		return stream().filter(f -> owner.getCompetitorPerceivedOffer(f).getQuality().compareTo(perceivedQ) > 0)
				.findFirst();

	}

	/*
	 * It returns the price below which f is expelled.
	 * 
	 * Returns empty if any price expels f
	 * 
	 * Returns 0 if no price expels f
	 */
	public Optional<BigDecimal> getPriceToExpel(BigDecimal q, Optional<Firm> optF) {

		Firm f;

		if (optF.isPresent())
			f = optF.get();
		else
			return Optional.empty();

		Offer firmOf = owner.getCompetitorPerceivedOffer(f);
		BigDecimal firmPerceivedQ = firmOf.getQuality();
		Optional<BigDecimal> retval;

		if (q.compareTo(firmPerceivedQ) == 0)
			// Any price below f's price expels f
			retval = Optional.of(firmOf.getPrice().subtract(Offer.getMinDeltaPrice()));

		else if (q.compareTo(firmPerceivedQ) > 0)

			retval = UtilityFunction.priceToExpelFromAbove(q, owner, f, Optional.ofNullable(lower(f)));

		else
			retval = Optional.of(UtilityFunction.priceToExpelFromBelow(q, owner, f, Optional.ofNullable(higher(f))));

		return retval;

	}

}
