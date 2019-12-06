package optimalPrice;

import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

import consumers.UtilityFunction;
import firms.Firm;
import firms.ExpectedMarket;

public class Neighbors {
	private Optional<Firm> loF, hiF;
	private double loPriceLimit, hiPriceLimit;

	public Neighbors(Firm f, ExpectedMarket expMkt, double perceivedQ, double minPrice, Optional<Double> optional)
			throws NoMarketSegmentForFirm {

		loF = expMkt.getLowerFirmGivenQ(perceivedQ);
		hiF = expMkt.getHigherFirmGivenQ(perceivedQ);

		// Setting loPriceLimit
		// Low price limit is the maximum among the price to expel low firm,
		// price to expel high firm and minimum price
		loPriceLimit = minPrice;
		expMkt.getPriceToExpel(perceivedQ, loF).ifPresent(p -> loPriceLimit = FastMath.max(loPriceLimit, p));
		expMkt.getPriceToExpel(perceivedQ, hiF).ifPresent(p -> loPriceLimit = FastMath.max(loPriceLimit, p));

		// Setting hiLimit. It is the minimum among max price to enter and max (price to
		// expel previous firm or max price on the first call)
		hiPriceLimit = UtilityFunction.getMaxPriceToEnter(f, perceivedQ, loF, hiF);
		optional.ifPresent(p -> hiPriceLimit = FastMath.min(hiPriceLimit, p));

		if (loPriceLimit >= hiPriceLimit)
			throw new NoMarketSegmentForFirm();

	}

	public double getLoPriceLimit() {
		return loPriceLimit;
	}

	public double getHiPriceLimit() {
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

		return "LoF: " + loFStr + ", HiF: " + hiFStr + ", LoPriceLim: " + loPriceLimit + ", HiPriceLim: "
				+ hiPriceLimit;
	}

}