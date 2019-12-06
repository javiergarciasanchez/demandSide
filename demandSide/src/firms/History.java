package firms;

import java.util.Optional;

import demandSide.Market;

public class History {
	
	private Market market;

	Optional<NeighborData> higherNeighbor = Optional.empty(), lowerNeighbor = Optional.empty();

	public History(Market market) {
		this.market = market;
	}

	public void updateCompetitorsPerceivedOffers(Firm f) {

		Optional<Firm> optF;

		// Lower Neighbor
		optF = Optional.ofNullable(market.firms.firmsByQ.lower(f));
		NeighborData.updateNeighborData(lowerNeighbor, optF);

		// Higher Neighbor
		optF = Optional.ofNullable(market.firms.firmsByQ.higher(f));
		NeighborData.updateNeighborData(higherNeighbor, optF);

	}

	/*
	 * 
	 * If it is a neighbor returns adjusted perceived offer using historical data
	 * otherwise returns firm perceived offer
	 * 
	 */
	public Offer getCompetitorPerceivedOffer(Firm f) {

		assert (f != null);

		Optional<Offer> hOf, lOf;

		hOf = higherNeighbor.filter(nD -> nD.getNeighbor() == f).map(NeighborData::getPerceivedOffer);

		lOf = lowerNeighbor.filter(nD -> nD.getNeighbor() == f).map(NeighborData::getPerceivedOffer);

		return hOf.orElse(lOf.orElse(f.getPerceivedOffer()));

	}

}
