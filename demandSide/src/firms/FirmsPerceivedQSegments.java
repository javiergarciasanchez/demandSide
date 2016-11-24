package firms;

import java.util.Comparator;

public class FirmsPerceivedQSegments extends FirmsSegments {

	private static final long serialVersionUID = 1L;

	private static class CompareBy implements Comparator<Firm> {

		@Override
		public int compare(Firm f1, Firm f2) {

			if (f1.equals(f2))
				return 0;
			else
				return (f1.getPerceivedQuality() < f2.getPerceivedQuality() ? -1
						: 1);
		}

	}

	public FirmsPerceivedQSegments() {
		super(new CompareBy());
	}


	@Override
	public double getQuality(Firm f) {
		return f.getPerceivedQuality();
	}

}
