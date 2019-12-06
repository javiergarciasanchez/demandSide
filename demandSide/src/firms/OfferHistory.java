package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;


public class OfferHistory {

	private double pCurr, pPrev;
	Offer smoothedOffer;

	public OfferHistory(Offer of) {
		double p = (Double) GetParameter("smoothingCompetitorParam");
		pCurr = p;
		pPrev = 1 - p;

		smoothedOffer = new Offer(of);

	}

	public void update(Offer currOf) {

		double s1, s2;

		s1 = currOf.getPrice() * pCurr;
		s2 = smoothedOffer.getPrice() * pPrev;
		smoothedOffer.setPrice(s1 + s2);

		s1 = currOf.getQuality() * pCurr;
		s2 = smoothedOffer.getQuality() * pPrev;
		smoothedOffer.setQuality(s1 + s2);

	}

	public double getPrice() {
		return smoothedOffer.getPrice();
	}

	public double getQuality() {
		return smoothedOffer.getQuality();
	}

	public Offer getOffer() {
		return smoothedOffer;
	}

}
