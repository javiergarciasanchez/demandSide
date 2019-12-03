package firms;

import java.util.Optional;

public class NeighborData {
	Firm neighbor;
	OfferHistory perceivedOfferHist;

	public NeighborData(Firm neighbor) {
		this.neighbor = neighbor;
		perceivedOfferHist = new OfferHistory(neighbor.getPerceivedOffer());
	}

	public static Optional<NeighborData> updateNeighborData(Optional<NeighborData> optND, Optional<Firm> optF) {

		if (optF.isEmpty())
			return Optional.empty();

		else {

			Firm f = optF.get();

			if (optND.isEmpty())
				return Optional.of(new NeighborData(f));

			else {

				NeighborData nd = optND.get();
				nd.updateOffer(f);
				return Optional.of(nd);

			}
		}

	}

	private void updateOffer(Firm f) {

		assert (f != null);
		
		Offer of = f.getPerceivedOffer();

		if (neighbor == f)
			perceivedOfferHist.update(of);
		else
			perceivedOfferHist = new OfferHistory(of);
	}

	public Offer getPerceivedOffer() {
		return perceivedOfferHist.getOffer();
	}
	
	public Firm getNeighbor() {
		return neighbor;
	}
	
}

