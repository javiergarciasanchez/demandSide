package optimalPrice;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import firms.Firm;
import firms.ExpectedMarket;

public class NeighborsByPriceToExpel implements Iterator<Neighbors> {
	ExpectedMarket expMkt;
	BigDecimal perceivedQ;
	BigDecimal minPrice;
	Neighbors currNeighbors;
	Iterator<ToBeExpelled> itToBeExpelled;

	NeighborsByPriceToExpel(ExpectedMarket expMkt, BigDecimal perceivedQ, BigDecimal minPrice) throws NoPrice {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ;
		this.minPrice = minPrice;

		// Note that a neighbor that is expelled with any price has a
		// priceToBeExpelled == null
		// They are take out of the iterator
		itToBeExpelled = expMkt.stream().map(new AddPriceToBeExpelled(expMkt, perceivedQ))
				.filter(toBeExp -> toBeExp.priceToBeExpelled != null)
				.filter(toBeExp -> toBeExp.priceToBeExpelled.compareTo(minPrice) >= 0)
				.sorted(new CompareByPriceToExpel(expMkt, perceivedQ)).collect(Collectors.toList()).iterator();

		setInitialcurrNeighbors();

	}

	private void setInitialcurrNeighbors() throws NoPrice {

		Firm loF = expMkt.getLowerFirmGivenQ(perceivedQ);
		Firm hiF = expMkt.getHigherFirmGivenQ(perceivedQ);

		currNeighbors = new Neighbors(expMkt, perceivedQ, minPrice, loF, hiF);

		while ((currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) >= 0) && hasNext()) {
			currNeighbors = next();
		}

		if (currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) >= 0)
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
			if (firmToBeExpelled == currNeighbors.getLoF())
				currNeighbors.setLoF(expMkt.lower(firmToBeExpelled));

			else {
				assert (firmToBeExpelled == currNeighbors.getHiF());
				currNeighbors.setHiF(expMkt.higher(firmToBeExpelled));

			}

			if (currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) >= 0)
				return null;
			else
				return currNeighbors;
		}

	}

}
