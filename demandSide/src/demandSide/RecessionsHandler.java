package demandSide;


import static repast.simphony.essentials.RepastEssentials.GetParameter;

import consumers.Consumers;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class RecessionsHandler {
	private static double recessionMagnitude = 0.0;
	
	
	public RecessionsHandler(Context<Object> context) {
		context.add(this);
	}

	@ScheduledMethod(start = 1, priority = RunPriority.SCHEDULE_RECESSIONS_PRIORITY)
	public void scheduleRecessions() {
		double[] start, dur, recesMag;

		// Read start of recessions
		String[] tmp = ((String) GetParameter("recessionStart")).split(":");
		start = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			start[i] = Double.valueOf(tmp[i]);
		}

		// Read Duration of recessions
		tmp = ((String) GetParameter("recessionDuration")).split(":");
		dur = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			dur[i] = Double.valueOf(tmp[i]);
		}

		// Read magnitude of recessions
		tmp = ((String) GetParameter("recessionMagnitude")).split(":");
		recesMag = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			recesMag[i] = Double.valueOf(tmp[i]);
		}

		// Schedule recessions
		for (int i = 0; i < tmp.length; i++) {
			ISchedule sch = RunEnvironment.getInstance().getCurrentSchedule();

			// Set start
			ScheduleParameters params = ScheduleParameters.createOneTime(
					start[i], ScheduleParameters.FIRST_PRIORITY);
			sch.schedule(params, this, "setRecesMagnitude", recesMag[i]);

			// Set end
			params = ScheduleParameters.createOneTime((start[i] + dur[i])
					, ScheduleParameters.FIRST_PRIORITY);
			sch.schedule(params, this, "setRecesMagnitude", 0.0);

		}

	}

	public static double getMinWelfareParamPerceivedByFirms() {
		double recessionImpact = 1 - getRecesMagnitude();
		return Consumers.getMinRawWelfareParam() * recessionImpact;
	}

	public static double getWelfareParamPerceivedByFirms(double rawWelfareParam) {

		double recessionImpact = 1 - getRecesMagnitude();
		return rawWelfareParam * recessionImpact;

	}

	public static double getWelfareParamForConsumers(double rawWelfareParameter) {
		double recessionImpact = 1 - getRecesMagnitude();
		return rawWelfareParameter * recessionImpact;
	}

	public static void setRecesMagnitude(double mag) {
		recessionMagnitude = mag;
	}

	public static double getRecesMagnitude() {
		return recessionMagnitude;
	}


}

