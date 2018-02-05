package improvingOffer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import firms.ExpectedMarket;
import firms.Firm;
import firms.Offer;
import firms.StrategicPreference;
import optimalPrice.OptimalPrice;

public class ImprovingOffer {

	/*
	 * Estimates the optimal price for the qualities that stratPref provides to
	 * try Returns the offer that maximizes margin
	 */
	public static Optional<Offer> get(Firm f, StrategicPreference stratPref) {

		//ArrayList<BigDecimal> realQOpts;
		Stream<BigDecimal> realQOpts;

		ExpectedMarket expMkt = new ExpectedMarket(f);

		realQOpts = stratPref.getRealQualityOptions(f.getQuality());

		return realQOpts.map(q -> getDecisionResult(f, q, expMkt)).filter(dr -> dr.isPresent())
				.max(new DecisionComparator()).map(Offer::new);

	}

	private static Optional<DecisionResult> getDecisionResult(Firm f, BigDecimal realQ, ExpectedMarket expMkt) {

		return OptimalPrice.get(f.getPerceivedQuality(realQ), f.getUnitCost(realQ), expMkt)
				.map(r -> new DecisionResult(r.getPrice(), realQ, r.getMargin()));

	}

}
