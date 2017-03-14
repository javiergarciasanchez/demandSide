package firms;

import java.util.Comparator;

public class FirmsPerceivedQSegments extends FirmsSegments {

	private static final long serialVersionUID = 1L;

	private static class CompareBy implements Comparator<Firm> {

		@Override
		public int compare(Firm f1, Firm f2) {

			if (f1.equals(f2))
				return 0;

			double q1 = f1.getQuality();
			double q2 = f2.getQuality();

			double pQ1 = f1.getPerceivedQuality(q1);
			double pQ2 = f2.getPerceivedQuality(q2);

			if (pQ1 == pQ2)
				// perceived qualities are equal, using real quality to tie break
				return (q1 < q2) ? -1 : 1;
			else
				return (pQ1 < pQ2) ? -1 : 1;
		}

	}

	public FirmsPerceivedQSegments() {
		super(new CompareBy());
	}

	@Override
	public double getQuality(Firm f) {
		return f.getPerceivedQuality(f.getQuality());
	}

	@Override
	public double getQuality(Firm f, double realQ) {
		return f.getPerceivedQuality(realQ);
	}

}
