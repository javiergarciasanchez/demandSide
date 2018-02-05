package optimalPrice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import firms.Firm;
import firms.ExpectedMarket;

public class NeighborsByPriceToExpel implements Iterator<Neighbors> {
	ExpectedMarket expMkt;
	BigDecimal perceivedQ;
	BigDecimal minPrice;
	Neighbors currNeighbors;
	Queue<ToBeExpelled> itToBeExpelled;

	NeighborsByPriceToExpel(ExpectedMarket expMkt, BigDecimal perceivedQ, BigDecimal minPrice) throws NoPrice {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ;
		this.minPrice = minPrice;

		// Note that a neighbor that is expelled with any price has a
		// priceToBeExpelled == null
		// They are take out of the iterator
		itToBeExpelled = expMkt.stream().map(new AddPriceToBeExpelled(expMkt, perceivedQ))
				.filter(toBeExp -> toBeExp.optPriceToBeExpelled.isPresent())
				.filter(toBeExp -> toBeExp.getPriceToBeExpelled().compareTo(minPrice) >= 0)
				.collect(Collectors.toCollection(() -> new PriorityQueue<ToBeExpelled>(new CompareByPriceToExpel())));

		setInitialcurrNeighbors();

	}

	private void setInitialcurrNeighbors() throws NoPrice {

		Optional<Firm> loF = expMkt.getLowerFirmGivenQ(perceivedQ);
		Optional<Firm> hiF = expMkt.getHigherFirmGivenQ(perceivedQ);

		currNeighbors = new Neighbors(expMkt, perceivedQ, minPrice, loF, hiF);

		while ((currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) >= 0) && hasNext()) {
			currNeighbors = next();
		}

		if (currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) >= 0)
			throw new NoPrice();

	}

	@Override
	public boolean hasNext() {
		return !itToBeExpelled.isEmpty();
	}

	/*
	 * Expel the firm to be expelled and set new neighbor firm to be expelled
	 * should be equal to one of the current neighbors
	 * 
	 * As more than one firm may have the same price to be expelled, all firms
	 * with the same price to be expelled should be skipped when selecting next
	 * neighbors
	 */
	@Override
	public Neighbors next() {

		ToBeExpelled toBeExpelled;
		
		Optional<Firm> firmToBeExpelled;
		Optional<BigDecimal> priceToExpel;
		
		do {
			
			toBeExpelled = itToBeExpelled.remove();
			
			firmToBeExpelled = Optional.of(toBeExpelled.f);
			priceToExpel = toBeExpelled.optPriceToBeExpelled;
			
			if (firmToBeExpelled.equals(currNeighbors.getLoF())) {
				currNeighbors.setLoF(Optional.ofNullable(expMkt.lower(firmToBeExpelled.get())));
			}
			
			else {
				assert (firmToBeExpelled.equals(currNeighbors.getHiF()));
				currNeighbors.setHiF(Optional.ofNullable(expMkt.higher(firmToBeExpelled.get())));
			}
			
			
		} while();
			
		assert (currNeighbors.loPriceLimit.compareTo(currNeighbors.hiPriceLimit) < 0);

		return currNeighbors;

	}

}
