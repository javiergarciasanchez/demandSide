package optimalPrice;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;
import firms.FirmsPerceivedQSegments;
import offer.Offer;

public class Neighbors {
	private Firm loF, hiF;
	FirmsPerceivedQSegments seg;
	double perceivedQ, cost;
	double loLimit, hiLimit;

	public Neighbors(FirmsPerceivedQSegments seg, double perceivedQ, double cost, Firm loF, Firm hiF) {
		this.seg = seg;
		this.perceivedQ = perceivedQ;
		this.cost = cost;
		this.loF = loF;
		this.hiF = hiF;

		setLimits(null);

	}

	private void setLimits(Firm prevF) {

		// Setting loLimit
		loLimit = Offer.getMinPrice(cost, perceivedQ);
		
		if (loF != null)		
			loLimit = FastMath.max(loLimit, seg.getPriceToExpel(perceivedQ, loF));

		if (hiF != null)
			loLimit = FastMath.max(loLimit, seg.getPriceToExpel(perceivedQ, hiF));		

		// Setting hiLimit		
		hiLimit = getHigherPriceToEnter(perceivedQ, loF, hiF);
		if (prevF != null)
			hiLimit = FastMath.min(hiLimit, seg.getPriceToExpel(perceivedQ, prevF));
		
	}

	private static double getHigherPriceToEnter(double perceivedQ, Firm loF, Firm hiF) {

		if (hiF == null)
			return Double.POSITIVE_INFINITY;

		double loP = 0, loQ = 0;
		if (loF != null) {
			loP = loF.getPrice();
			loQ = loF.getPerceivedQuality();
		}

		double maxPrice = (hiF.getPrice() * (perceivedQ - loQ) + loP * (hiF.getPerceivedQuality() - perceivedQ))
				/ (hiF.getPerceivedQuality() - loQ);

		return maxPrice - Double.MIN_VALUE;

	}

	Firm getLoF() {
		return loF;
	}

	void setLoF(Firm loF) {
		Firm prevF = this.loF;
		this.loF = loF;
		setLimits(prevF);
	}

	Firm getHiF() {
		return hiF;
	}

	void setHiF(Firm hiF) {
		Firm prevF = this.hiF;
		this.hiF = hiF;
		setLimits(prevF);
	}

	public String toString() {
		String loFStr = (loF != null) ? loF.toString() : "null";
		String hiFStr = (hiF != null) ? hiF.toString() : "null";
		return "LoF: " + loFStr + ", HiF: " + hiFStr;
	}
}