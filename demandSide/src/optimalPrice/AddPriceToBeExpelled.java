package optimalPrice;

import java.util.Optional;
import java.util.function.Function;

import firms.Firm;
import firms.ExpectedMarket;

public class AddPriceToBeExpelled implements Function<Firm, ToBeExpelled> {
	ExpectedMarket expMkt;
	double perceivedQ;

	AddPriceToBeExpelled(ExpectedMarket expMkt, double perceivedQ2) {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ2;
	}

	@Override
	public ToBeExpelled apply(Firm f) {
		ToBeExpelled retval = new ToBeExpelled();
		retval.f = f;
		retval.optPriceToBeExpelled = expMkt.getPriceToExpel(perceivedQ, Optional.of(f));
		return retval;
	}

}