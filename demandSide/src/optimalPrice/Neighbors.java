package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;
import firms.Firm;
import firms.ExpectedMarket;

public class Neighbors {
	private Optional<Firm> loF, hiF;
	private BigDecimal loPriceLimit, hiPriceLimit;

	public Neighbors(ExpectedMarket expMkt, BigDecimal perceivedQ, BigDecimal minPrice, Optional<BigDecimal> maxPrice)
			throws NoMarketSegmentForFirm {

		loF = expMkt.getLowerFirmGivenQ(perceivedQ);
		hiF = expMkt.getHigherFirmGivenQ(perceivedQ);

		// Setting loPriceLimit
		// Low price limit is the maximum among the price to expel low firm,
		// price to expel high firm and minimum price
		loPriceLimit = minPrice;
		expMkt.getPriceToExpel(perceivedQ, loF).ifPresent(p -> loPriceLimit = loPriceLimit.max(p));
		expMkt.getPriceToExpel(perceivedQ, hiF).ifPresent(p -> loPriceLimit = loPriceLimit.max(p));

		// Setting hiLimit. It is the minimum among max price to enter and max (price to
		// expel previous firm or max price on the first call)
		hiPriceLimit = ExpectedMarket.getMaxPriceToEnter(perceivedQ, loF, hiF);
		maxPrice.ifPresent(p -> hiPriceLimit = hiPriceLimit.min(p));

		if (loPriceLimit.compareTo(hiPriceLimit) >= 0)
			throw new NoMarketSegmentForFirm();

	}

	public BigDecimal getLoPriceLimit() {
		return loPriceLimit;
	}

	public BigDecimal getHiPriceLimit() {
		return hiPriceLimit;
	}

	Optional<Firm> getLoF() {
		return loF;
	}

	Optional<Firm> getHiF() {
		return hiF;
	}

	public String toString() {
		String loFStr = loF.map(Firm::toString).orElse("null");
		String hiFStr = hiF.map(Firm::toString).orElse("null");

		return "LoF: " + loFStr + ", HiF: " + hiFStr;
	}

}