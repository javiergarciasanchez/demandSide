package demandSide;

import static repast.simphony.essentials.RepastEssentials.GetParameter;
import firms.Firm;
import firms.Firms;
import firms.Offer;

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

	public static Firms firms;

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

		// Initialize ToBeKilled
		toBeKilled = new ArrayList<Firm>();

		context.setId("Market");

		// Create RecessionsHandler Handler
		new RecessionsHandler(context);

		// Create Consumers
		consumers = new Consumers();
		context.addSubContext(consumers);
		Consumers.createConsumers();

		// Create firms
		firms = new Firms();
		context.addSubContext(firms);

		return context;

	}


}
