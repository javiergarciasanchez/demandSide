package firms;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class ProfitDerivativeRespPrice implements UnivariateFunction {
	MktSegmentOLD seg;
	Firm f;

	ProfitDerivativeRespPrice(MktSegmentOLD seg, Firm f) {
		this.seg = seg;
		this.f = f;
	}

	@Override
	public double value(double p) {
		double gini = (double) GetParameter("gini");
		double lambda = (1.0 + gini) / (2.0 * gini);

		double q = f.getQuality();
		double cost = f.unitCost(q);

		double loP = 0.0, loQ = 0.0;

		if (seg.lowerSegment != null) {
			loP = seg.lowerSegment.getPrice();
			loQ = seg.lowerSegment.getQuality();
		}

		double hiP = seg.higherSegment.getPrice();
		double hiQ = seg.higherSegment.getQuality();

		return MktSegmentsOLD.expectedConsumersAbove((p - loP) / (q - loQ))
				* (1 - (p - cost) * lambda / (p - loP))
				- MktSegmentsOLD.expectedConsumersAbove((hiP - p) / (hiQ - q))
				* (1 - (p - cost) * lambda / (hiP - p));
	}
}
