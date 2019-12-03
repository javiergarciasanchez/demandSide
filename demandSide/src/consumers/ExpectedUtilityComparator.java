package consumers;

import java.util.Comparator;
import java.util.Map.Entry;

import org.apache.commons.math3.util.FastMath;

import firms.Firm;

public class ExpectedUtilityComparator implements Comparator<Entry<Firm, Double>> {
	Consumer c;
	
	public ExpectedUtilityComparator(Consumer c) {
		this.c = c;
	}

	@Override
	public int compare(Entry<Firm, Double> knownFirm_1, Entry<Firm, Double> knownFirm_2) {

			return (int) FastMath.signum(c.expectedUtility(knownFirm_1)  
					- c.expectedUtility(knownFirm_2));

	}

}
