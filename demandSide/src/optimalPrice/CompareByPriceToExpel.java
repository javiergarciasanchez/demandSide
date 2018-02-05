package optimalPrice;

import java.math.BigDecimal;
import java.util.Comparator;
import firms.Firm;

// It orders the list from the highest price to the lowest
public class CompareByPriceToExpel implements Comparator<ToBeExpelled> {

	@Override
	public int compare(ToBeExpelled fToE1, ToBeExpelled fToE2) {

		Firm f1 = fToE1.f;
		Firm f2 = fToE2.f;

		if (f1.equals(f2))
			return 0;
		else {
			// Both prices to be expelled are present
			BigDecimal pToE1 = fToE1.optPriceToBeExpelled.get();
			BigDecimal pToE2 = fToE2.optPriceToBeExpelled.get();

			if (pToE1.compareTo(pToE2) == 0)
				// prices to expel are equal, using quality to untie
				return f2.getQuality().compareTo(f1.getQuality());
			else
				return pToE2.compareTo(pToE1);

		}
	}

}