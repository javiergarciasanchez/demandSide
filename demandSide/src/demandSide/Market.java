package demandSide;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import firms.Firm;
import firms.Firms;
import firms.Offer;
import graphs.ConsumersProjection;
import graphs.ConsumptionProjection;
import graphs.Firms2DProjection;
import graphs.FirmsDemandProjection;
import graphs.FirmsProfitProjection;
import graphs.FirmsSalesProjection;
import graphs.MargUtilProjection;
import graphs.Scale;

import java.util.ArrayList;

import consumers.Consumer;
import consumers.Consumers;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.random.RandomHelper;

public class Market extends DefaultContext<Object> implements
		ContextBuilder<Object> {

	// Defining market components: consumers and firms
	public static Consumers consumers;
	public static ConsumersProjection consumersProjection;

	public static Firms firms;
	public static Firms2DProjection firms2DProjection;
	public static FirmsDemandProjection firmsDemandProjection;
	public static FirmsProfitProjection firmsProfitProjection;
	public static FirmsSalesProjection firmsSalesProjection;

	public static ConsumptionProjection consumptionProjection;
	public static MargUtilProjection margUtilProjection;

	public static ArrayList<Firm> toBeKilled;

	@Override
	public Context<Object> build(Context<Object> context) {		
		
		if (RunEnvironment.getInstance().isBatch())
			System.out.println("Run: " + RunState.getInstance().getRunInfo().getRunNumber());


		// Reset seed
		RandomHelper.setSeed((Integer) GetParameter("randomSeed"));

		// Set end of run
		RunEnvironment.getInstance().endAt((Double) GetParameter("stopAt"));

		// Reset static variables
		Consumer.resetStaticVars();
		Consumers.resetStaticVars();
		Firm.resetStaticVars();
		Firms.resetStaticVars();
		Offer.resetStaticVars();
		Scale.resetStaticVars();

		// Initialize ToBeKilled
		toBeKilled = new ArrayList<Firm>();

		context.setId("Market");

		// Create RecessionsHandler Handler
		new RecessionsHandler(context);

		// Create Consumers
		consumers = new Consumers();
		context.addSubContext(consumers);
		Consumers.createConsumers();

		// Consumers Projection
		// Dimension is Marginal Utility of Quality
		consumersProjection = new ConsumersProjection(consumers);
		consumptionProjection = new ConsumptionProjection(consumers);
		// Create Marginal utility projection
		margUtilProjection = new MargUtilProjection(context);

		// AddConsumers to projections
		consumers.forEach((c)-> c.updateProjections());

		// Create firms
		firms = new Firms();
		context.addSubContext(firms);

		// Firms Projections
		// Dimensions are price, quality and consumers
		firms2DProjection = new Firms2DProjection(firms);
		firmsDemandProjection = new FirmsDemandProjection(firms);
		firmsProfitProjection = new FirmsProfitProjection(firms);
		firmsSalesProjection = new FirmsSalesProjection(firms);

		return context;

	}


}
