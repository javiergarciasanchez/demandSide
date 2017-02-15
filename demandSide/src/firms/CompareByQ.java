package firms;

import java.util.Comparator;

public class CompareByQ implements Comparator<Firm> {

	@Override
	public int compare(Firm f1, Firm f2) {
		if (f1.equals(f2) || (f1.getQuality() == f2.getQuality()))
			return 0;
		else
			return (f1.getQuality() < f2.getQuality() ? -1 : 1);

	}

}
