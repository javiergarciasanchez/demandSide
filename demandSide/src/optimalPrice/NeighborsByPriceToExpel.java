package optimalPrice;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import firms.Firm;
import firms.FirmsPerceivedQSegments;
import offer.Offer;

public class NeighborsByPriceToExpel implements Iterator<Neighbors> {
	FirmsPerceivedQSegments seg;
	double perceivedQ, cost;
	Neighbors currNeighbors;
	Iterator<ToBeExpelled> itToBeExpelled;

	NeighborsByPriceToExpel(FirmsPerceivedQSegments seg, double perceivedQ, double cost) throws NoPrice {
		this.seg = seg;
		this.perceivedQ = perceivedQ;
		this.cost = cost;

		double minPrice = Offer.getMinPrice(cost, perceivedQ);

		itToBeExpelled = seg.stream().map(new AddPriceToBeExpelled(seg, perceivedQ))
				.filter(toBeExp -> toBeExp.priceToBeExpelled > minPrice)
				.sorted(new CompareByPriceToExpel(seg, perceivedQ)).collect(Collectors.toList()).iterator();

		setInitialcurrNeighbors();

	}

	private void setInitialcurrNeighbors() throws NoPrice {

		Firm loF = seg.getLowerFirmGivenQ(perceivedQ);
		Firm hiF = seg.getHigherFirmGivenQ(perceivedQ);

		currNeighbors = new Neighbors(seg, perceivedQ, cost, loF, hiF);

		while ((currNeighbors.loLimit >= currNeighbors.hiLimit) && hasNext()) {
			currNeighbors = next();
		}

		if (currNeighbors.loLimit >= currNeighbors.hiLimit)
			throw new NoPrice();

	}

	@Override
	public boolean hasNext() {
		return itToBeExpelled.hasNext();
	}

	@Override
	public Neighbors next() {

		if (!itToBeExpelled.hasNext())
			throw new NoSuchElementException();

		else {

			ToBeExpelled toBeExpelled = itToBeExpelled.next();
			Firm firmToBeExpelled = toBeExpelled.f;

			// Expel the firm to be expelled
			if (firmToBeExpelled.equals(currNeighbors.getLoF()))
				currNeighbors.setLoF(seg.lower(firmToBeExpelled));

			else {
				assert firmToBeExpelled.equals(currNeighbors.getHiF());
				currNeighbors.setHiF(seg.higher(firmToBeExpelled));

			}

			return currNeighbors;
		}

	}

}
