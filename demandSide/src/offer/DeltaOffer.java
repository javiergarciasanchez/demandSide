package offer;

import java.math.BigDecimal;

public class DeltaOffer {
	private BigDecimal deltaPrice;
	private BigDecimal deltaQuality;

	public DeltaOffer(BigDecimal dP, BigDecimal dQ) {
		setDeltaPrice(dP);
		setDeltaQuality(dQ);
	}

	public DeltaOffer() {
		deltaPrice = BigDecimal.ZERO;
		deltaQuality = BigDecimal.ZERO;
		
	}

	public BigDecimal getDeltaPrice() {
		return deltaPrice;
	}

	public BigDecimal getDeltaQuality() {
		return deltaQuality;
	}

	public void setDeltaPrice(BigDecimal dP) {
		this.deltaPrice = dP;
	}

	public void setDeltaQuality(BigDecimal dQ) {
		this.deltaQuality = dQ;
	}

	public static DeltaOfferSignCompare deltaOfferCompare(DeltaOffer dOffer1, DeltaOffer dOffer2) {

		int priceSign = dOffer1.getDeltaPrice().signum() * dOffer2.getDeltaPrice().signum(); 
		int qualitySign = dOffer1.getDeltaQuality().signum() * dOffer2.getDeltaQuality().signum();
		
		if ((priceSign == -1) && (qualitySign == -1))
			return DeltaOfferSignCompare.BOTH_UNEQUAL;

		else if ((priceSign == 1) && (qualitySign == -1))
			return DeltaOfferSignCompare.UNEQUAL_QUALITY;

		else if ((priceSign == -1) && (qualitySign == 1))
			return DeltaOfferSignCompare.UNEQUAL_PRICE;

		else
			return DeltaOfferSignCompare.BOTH_EQUAL;

	}
	
	public String toString(){
		return "DP: " + deltaPrice + "; DQ: " + deltaQuality; 
	}

}
