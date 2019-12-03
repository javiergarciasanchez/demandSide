package optimalPrice;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.analysis.UnivariateFunction;

import demandSide.Market;
import firms.Firm;
import firms.Offer;

public class ExpectedProfitForMaximization implements UnivariateFunction {

	private Market market;
	private double perceivedQ;
	private double cost;
	private double fixedCost;
	private DoubleUnaryOperator adjDemand;
	Optional<Offer> loOffer, hiOffer;

	public ExpectedProfitForMaximization(Firm firm, BigDecimal realQ, Optional<Offer> loOf, Optional<Offer> hiOf) {

		this.market = firm.market;
		this.perceivedQ = firm.getPerceivedQuality(realQ).doubleValue();
		this.cost = firm.getUnitCost(realQ);
		this.adjDemand = firm::getAdjustedDemand;
		this.fixedCost = firm.getFixedCost();
		this.loOffer = loOf;
		this.hiOffer = hiOf;

	}

	@Override
	public double value(double p) {

		double fullKnowledgeExpDemand = market.consumers.getExpectedQuantity(new Offer(p, perceivedQ), loOffer,
				hiOffer);

		double expDemand = adjDemand.applyAsDouble(fullKnowledgeExpDemand);

		return (p - cost) * expDemand - fixedCost;

	}

}
