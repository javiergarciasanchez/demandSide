package firms;

import java.util.Comparator;

public class CompareByPerceivedQ implements Comparator<Firm> {

	@Override
	public int compare(Firm f1, Firm f2) {

		if (f1.equals(f2))
			return 0;

		double pQ1 = f1.getPerceivedQuality();
		double pQ2 = f2.getPerceivedQuality();

		if (pQ1 == pQ2) {
			// perceived qualities are equal, using real quality to compare
			double q1 = f1.getQuality();
			double q2 = f2.getQuality();
			if (q1 == q2)
				throw new Error("Qualities of different firms cannot be equal");
			else
				return (q1 < q2) ? -1 : 1;
		}

		else
			return (pQ1 < pQ2) ? -1 : 1;
	}

}
