package firms;

import java.util.Comparator;

import org.apache.commons.math3.util.FastMath;

public class CompareByPerceivedQ implements Comparator<Firm> {
	Firm owner;

	public CompareByPerceivedQ(Firm owner) {
		this.owner = owner;
	}

	@Override
	public int compare(Firm f1, Firm f2) {

		if (f1.equals(f2))
			return 0;

		// Order is based on perceived quality
		double pQ1 = owner.getCompetitorPerceivedOffer(f1).getQuality();
		double pQ2 = owner.getCompetitorPerceivedOffer(f2).getQuality();
		
		if (pQ1 != pQ2)
			return (int) FastMath.signum(pQ1 - pQ2);

		// if perceived quality is equal, real quality unties
		double q1 = f1.getQuality();
		double q2 = f2.getQuality();

		if (q1 != q2)
			return (int) FastMath.signum(q1 - q2);

		// if real quality is equal, price unties
		double p1 = f1.getPrice();
		double p2 = f2.getPrice();
		
		if (p1 != p2)
			return (int) FastMath.signum(p1 - p2);

		// if both qualities and price are equal, an arbitrary order is chosen
		return -1;

	}
}
