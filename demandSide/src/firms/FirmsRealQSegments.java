package firms;

import java.util.Comparator;

public class FirmsRealQSegments extends FirmsSegments {
	
	private static final long serialVersionUID = 1L;

	private static class CompareBy implements Comparator<Firm> {

		@Override
		public int compare(Firm f1, Firm f2) {

			if (f1.equals(f2))
				return 0;
			else
				return (f1.getQuality() < f2.getQuality() ? -1 : 1);
		}

	}
	
	public FirmsRealQSegments(){
		super(new CompareBy());
	}

	@Override
	public double getQuality(Firm f) {
		// TODO Auto-generated method stub
		return f.getQuality();
	}

}
