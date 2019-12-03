package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.math.BigDecimal;

public class OfferHistory {
	
	private BigDecimal pCurr, pPrev;
	Offer smoothedOffer;
	
	public OfferHistory(Offer of) {
		double p = (Double) GetParameter("smoothingCompetitorParam");
		pCurr = BigDecimal.valueOf(p);
		pPrev = BigDecimal.valueOf(1-p);
		
		smoothedOffer = new Offer(of);
		
	}
	
	public void update(Offer currOf) {
		
		BigDecimal s1, s2;
		
		s1 = currOf.getPrice().multiply(pCurr);
		s2 = smoothedOffer.getPrice().multiply(pPrev);
		smoothedOffer.setPrice(s1.add(s2));

		s1 = currOf.getQuality().multiply(pCurr);
		s2 = smoothedOffer.getQuality().multiply(pPrev);
		smoothedOffer.setQuality(s1.add(s2));
		
	}

	public BigDecimal getPrice() {
		return smoothedOffer.getPrice();
	}

	public BigDecimal getQuality() {
		return smoothedOffer.getQuality();
	}

	public Offer getOffer() {
		return smoothedOffer;
	}

}
