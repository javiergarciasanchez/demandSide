package firms;

import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

public class DecisionComparator implements Comparator<Optional<Decision>> {

	@Override
	public int compare(Optional<Decision> dr1, Optional<Decision> dr2) {

		// Empty decision have the lowest value
		if (!dr1.isPresent())
			return -1;

		else if (!dr2.isPresent())
			return 1;

		else
			return (int) FastMath.signum(dr1.get().expInf.grossProfit - dr2.get().expInf.grossProfit);

	}

}
