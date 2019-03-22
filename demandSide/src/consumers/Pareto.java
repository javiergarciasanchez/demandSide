package consumers;

import org.apache.commons.math3.util.FastMath;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Exponential;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import demandSide.Market;

public class Pareto extends AbstractContinousDistribution {

	private static final long serialVersionUID = 1L;
	private Exponential implicitDistrib;
	private double minimum;

	Pareto(double lambda, double minimum) {
		this.minimum = minimum;
//		implicitDistrib = RandomHelper.createExponential(lambda);
		
		RandomEngine engine = new MersenneTwister(Market.seed);
		implicitDistrib = new Exponential(lambda, engine);
	}

	public static Pareto getPareto(double lambda, double minimum) {
		return new Pareto(lambda, minimum);
	}

	public double nextDouble() {
		double retval = minimum * FastMath.exp(implicitDistrib.nextDouble());
		return retval;
	}

	public static double inversePareto(double acumProb, double minimum, double lambda) {
		return minimum / (1 - acumProb) * FastMath.exp(-1.0 / lambda);
	}

}