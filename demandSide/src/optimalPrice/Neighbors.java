package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;
import firms.Firm;
import firms.ExpectedMarket;

public class Neighbors {
	private Optional<Firm> loF, hiF;
	ExpectedMarket expMkt;
	BigDecimal perceivedQ, minPrice;
	BigDecimal loPriceLimit, hiPriceLimit;

	public Neighbors(ExpectedMarket expMkt, BigDecimal perceivedQ, BigDecimal minPrice, Optional<Firm> loF,
			Optional<Firm> hiF) {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ;
		this.minPrice = minPrice;
		this.loF = loF;
		this.hiF = hiF;

		// No previous firm
		setPriceLimits(Optional.empty());

	}

	private void setPriceLimits(Optional<Firm> prevF) {

		// Setting loPriceLimit
		// Low price limit is the maximum among the price to expel low firm,
		// price to expel high firm and minimum price
		loPriceLimit = minPrice;
		expMkt.getPriceToExpel(perceivedQ, loF).ifPresent(p-> loPriceLimit = loPriceLimit.max(p));
		expMkt.getPriceToExpel(perceivedQ, hiF).ifPresent(p-> loPriceLimit = loPriceLimit.max(p));
		
		// Setting hiLimit. It is the minimum among max price to enter and price to expel previous firm
		hiPriceLimit = ExpectedMarket.getMaxPriceToEnter(perceivedQ, loF, hiF);
		expMkt.getPriceToExpel(perceivedQ, prevF).ifPresent(p-> hiPriceLimit = hiPriceLimit.min(p));
		
	}

	Optional<Firm> getLoF() {
		return loF;
	}

	void setLoF(Optional<Firm> loF) {
		Optional<Firm> prevF = this.loF;
		this.loF = loF;
		setPriceLimits(prevF);
	}

	Optional<Firm> getHiF() {
		return hiF;
	}

	void setHiF(Optional<Firm> hiF) {
		Optional<Firm> prevF = this.hiF;
		this.hiF = hiF;
		setPriceLimits(prevF);
	}

	public String toString() {
		String loFStr = loF.map(Firm::toString).orElse("null");
		String hiFStr = hiF.map(Firm::toString).orElse("null");
				
		return "LoF: " + loFStr + ", HiF: " + hiFStr;
	}
}