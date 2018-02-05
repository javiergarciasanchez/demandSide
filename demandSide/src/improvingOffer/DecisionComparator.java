package improvingOffer;

import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

public class DecisionComparator implements Comparator<Optional<DecisionResult>> {

	@Override
	public int compare(Optional<DecisionResult> dr1, Optional<DecisionResult> dr2) {

		// Comparator is called after filtering for empty optionals
		assert (dr1.isPresent() && dr2.isPresent());

		return (int) FastMath.signum(dr1.get().getMargin() - dr2.get().getMargin());
	}

}
