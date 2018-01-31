package optimalPrice;

import java.math.BigDecimal;

import firms.Firm;
import firms.ExpectedMarket;

public class Neighbors {
	private Firm loF, hiF;
	ExpectedMarket expMkt;
	BigDecimal perceivedQ, minPrice;
	BigDecimal loPriceLimit, hiPriceLimit;

	public Neighbors(ExpectedMarket expMkt, BigDecimal perceivedQ, BigDecimal minPrice, Firm loF, Firm hiF) {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ;
		this.minPrice = minPrice;
		this.loF = loF;
		this.hiF = hiF;

		setPriceLimits(null);

	}

	private void setPriceLimits(Firm prevF) {

		// Setting loPriceLimit
		loPriceLimit = minPrice;

		// Note that if price to Expel is null means that any price would expel loF
		// thus previous loLimit is kept (loLimit = minPrice or price to expel loF)
		if ((loF != null) && expMkt.getPriceToExpel(perceivedQ, loF) != null)
			loPriceLimit = loPriceLimit.max(expMkt.getPriceToExpel(perceivedQ, loF));

		if ((hiF != null) && expMkt.getPriceToExpel(perceivedQ, hiF) != null)
			loPriceLimit = loPriceLimit.max(expMkt.getPriceToExpel(perceivedQ, hiF));

		// Setting hiLimit
		hiPriceLimit = ExpectedMarket.getMaxPriceToEnter(perceivedQ, loF, hiF);
		if ((prevF != null) && expMkt.getPriceToExpel(perceivedQ, prevF) != null)
			hiPriceLimit = hiPriceLimit.min(expMkt.getPriceToExpel(perceivedQ, prevF));

	}

	Firm getLoF() {
		return loF;
	}

	void setLoF(Firm loF) {
		Firm prevF = this.loF;
		this.loF = loF;
		setPriceLimits(prevF);
	}

	Firm getHiF() {
		return hiF;
	}

	void setHiF(Firm hiF) {
		Firm prevF = this.hiF;
		this.hiF = hiF;
		setPriceLimits(prevF);
	}

	public String toString() {
		String loFStr = (loF != null) ? loF.toString() : "null";
		String hiFStr = (hiF != null) ? hiF.toString() : "null";
		return "LoF: " + loFStr + ", HiF: " + hiFStr;
	}
}