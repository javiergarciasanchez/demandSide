package decisionTools;

import java.util.Comparator;

import firms.Firm;
import firms.FirmsSegments;

// It orders the list from the highest price to the lowest
public class CompareByPriceToExpel implements Comparator<Firm> {
	FirmsSegments seg;
	double q;

	public CompareByPriceToExpel(FirmsSegments seg, double q) {
		this.q = q;
		this.seg = seg;
	}

	@Override
	public int compare(Firm f1, Firm f2) {

		if (f1.equals(f2))
			return 0;
		else {
			double pToE_1 = seg.getPriceToExpel(q, f1);
			double pToE_2 = seg.getPriceToExpel(q, f2);

			if (pToE_1 == pToE_2)
				// price to expel are equal, the easier firm to expel is the one higher price
				return (f1.getPrice() < f2.getPrice()) ? -1 : 1;
			else

				return (pToE_1 > pToE_2) ? -1 : 1;
			
		}
	}

}