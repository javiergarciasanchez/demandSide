package optimalPrice;

import java.util.function.Function;

import firms.Firm;
import firms.FirmsPerceivedQSegments;

public class AddPriceToBeExpelled implements Function<Firm, ToBeExpelled> {
	FirmsPerceivedQSegments seg;
	double perceivedQ;

	AddPriceToBeExpelled(FirmsPerceivedQSegments seg, double perceivedQ) {
		this.seg = seg;
		this.perceivedQ = perceivedQ;
	}

	@Override
	public ToBeExpelled apply(Firm f) {
		ToBeExpelled retval = new ToBeExpelled();
		retval.f = f;
		retval.priceToBeExpelled = seg.getPriceToExpel(perceivedQ, f);
		return retval;
	}

}