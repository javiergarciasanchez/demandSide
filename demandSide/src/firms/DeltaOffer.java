package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

public class DeltaOffer {
	static final double DIRECTION_TOLERANCE = Math.nextUp(0.0) * 10.0;

	private double deltaQ = 0.0;
	private double deltaP = 0.0;

	public DeltaOffer() {
	}

	private DeltaOffer(double p, double q) {
		deltaP = p;
		deltaQ = q;
	}

	public DeltaOffer(DeltaOffer deltaOffer) {
		deltaP = deltaOffer.deltaP;
		deltaQ = deltaOffer.deltaQ;
	}

	public static double getDefaultSize() {
		double defP = (Double) GetParameter("defaultPriceStep");
		double defQ = (Double) GetParameter("defaultQualityStep");
		
		return Math.sqrt(Math.pow(defP, 2) + Math.pow(defQ, 2));
	}

	public static DeltaOffer createNormalizedMaximizingDirection(Firm f) {
		DeltaOffer dOffer = new DeltaOffer(Utils.getMarginalProfitOfPrice(f),
				Utils.getMarginalProfitOfQuality(f));

		// Normalize
		dOffer.setSize(1.0);

		return dOffer;

	}

	// sine and cosine of vectors should be equal but with opposite sign
	// There is a tolerance for being equal
	public static boolean oppositeDirections(DeltaOffer dOffer1,
			DeltaOffer dOffer2) {

		// Check p1 = - p2 and q1 = -q2 of the normalized vectors
		double size1 = dOffer1.getSize();
		double size2 = dOffer2.getSize();

		return (dOffer1.deltaP / size1 - dOffer2.deltaP / size2 < DIRECTION_TOLERANCE && dOffer1.deltaQ
				/ size1 - dOffer2.deltaQ / size2 < DIRECTION_TOLERANCE);

	}

	public void setSize(double size) {
		double prevSize = getSize();
		deltaP = deltaP / prevSize * size;
		deltaQ = deltaQ / prevSize * size;

	}

	public double getSize() {
		return Math.sqrt(Math.pow(deltaP, 2) + Math.pow(deltaQ, 2));
	}

	public void addTo(Offer offer) {
		offer.setPrice(offer.getPrice() + deltaP);
		offer.setQuality(offer.getQuality() + deltaQ);		
	}

}
