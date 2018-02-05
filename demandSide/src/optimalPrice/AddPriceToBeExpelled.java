package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import firms.Firm;
import firms.ExpectedMarket;

public class AddPriceToBeExpelled implements Function<Firm, ToBeExpelled> {
	ExpectedMarket expMkt;
	BigDecimal perceivedQ;

	AddPriceToBeExpelled(ExpectedMarket expMkt, BigDecimal perceivedQ) {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ;
	}

	@Override
	public ToBeExpelled apply(Firm f) {
		ToBeExpelled retval = new ToBeExpelled();
		retval.f = f;
		retval.optPriceToBeExpelled = expMkt.getPriceToExpel(perceivedQ, Optional.of(f));
		return retval;
	}

}