package optimalPrice;

import java.math.BigDecimal;
import java.util.Comparator;

import firms.Firm;
import firms.ExpectedMarket;

// It orders the list from the highest price to the lowest
public class CompareByPriceToExpel implements Comparator<ToBeExpelled> {
	ExpectedMarket expMkt;
	BigDecimal perceivedQ;

	public CompareByPriceToExpel(ExpectedMarket expMkt, BigDecimal perceivedQ2) {
		this.expMkt = expMkt;
		this.perceivedQ = perceivedQ2;
	}

	@Override
	public int compare(ToBeExpelled fToE1, ToBeExpelled fToE2) {

		Firm f1 = fToE1.f;
		Firm f2 = fToE2.f;

		if (f1.equals(f2))
			return 0;
		else {
			BigDecimal pToE1 = fToE1.priceToBeExpelled;
			BigDecimal pToE2 = fToE2.priceToBeExpelled;

			// null priceToBeExpelled means no price expels firm
			// It works as if priceToBeExpelled were positive infinity
			if ((pToE1 == null) && (pToE2 == null))
				// price to expel are equal, using price to untie
				return f2.getPrice().compareTo(f1.getPrice());
			else if (pToE1 == null)
				return -1;
			else if (pToE2 == null)
				return 1;
			else if (pToE1.compareTo(pToE2) == 0)
				// price to expel are equal, using price to untie
				return f2.getPrice().compareTo(f1.getPrice());
			else
				return pToE2.compareTo(pToE1);

		}
	}

}