package optimalPrice;

import java.util.Comparator;

import firms.Firm;
import firms.FirmsPerceivedQSegments;

// It orders the list from the highest price to the lowest
public class CompareByPriceToExpel implements Comparator<ToBeExpelled> {
	FirmsPerceivedQSegments seg;
	double perceivedQ;

	public CompareByPriceToExpel(FirmsPerceivedQSegments seg, double perceivedQ) {
		this.seg = seg;
		this.perceivedQ = perceivedQ;
	}

	@Override
	public int compare(ToBeExpelled fToE1, ToBeExpelled fToE2) {
		
		Firm f1 = fToE1.f;
		Firm f2 = fToE2.f;

		if (f1.equals(f2))
			return 0;
		else {
			double pToE1 = fToE1.priceToBeExpelled;
			double pToE2 = fToE2.priceToBeExpelled;

			if (pToE1 == pToE2)
				// price to expel are equal, using price to untie
				return (f1.getPrice() > f2.getPrice()) ? -1 : 1;
			else

				return (pToE1 > pToE2) ? -1 : 1;
			
		}
	}

}