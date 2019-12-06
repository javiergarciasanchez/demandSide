package optimalPrice;

import java.util.Comparator;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;

// It orders the list from the highest price to the lowest
public class CompareByPriceToExpel implements Comparator<ToBeExpelled> {

	@Override
	public int compare(ToBeExpelled fToE1, ToBeExpelled fToE2) {

		Firm f1 = fToE1.f;
		Firm f2 = fToE2.f;

		if (f1.equals(f2))
			return 0;

		// Both prices to be expelled are present
		double pToE1 = fToE1.optPriceToBeExpelled.get();
		double pToE2 = fToE2.optPriceToBeExpelled.get();

		if (pToE1 != pToE2)
			return (int) FastMath.signum(pToE2 - pToE1);

		// prices to expel and quality are equal, using arbitrary order
		return -1;

	}

}