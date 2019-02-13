package firmTypes;

import demandSide.Market;

public enum FirmTypes {
	
	STANDARD_FIRM,
	NO_Q_CHANGE_FIRM,
	NO_Q_REDUCTION_FIRM,
	NO_Q_INCREASE_FIRM,
	INCREASE_Q_FIRM;
	
	public static void createRandomTypeFirm() {
		
		int i = Market.firms.firmTypes.nextInt() - 1;
		
		(values()[i]).createFirm();
		
	}
	
	public void createFirm(){
		
		switch (this) {
		case STANDARD_FIRM:
			new StandardFirm();
			break;
		case NO_Q_CHANGE_FIRM:
			new NoQChangeFirm();
			break;
		case NO_Q_REDUCTION_FIRM:
			new NoQReductionFirm();
			break;
		case NO_Q_INCREASE_FIRM:
			new NoQIncreaseFirm();
			break;
		case INCREASE_Q_FIRM:
			new IncreaseQFirm();
			break;
		}
		
	}
	
	public String toString() {
		switch (this) {
		case STANDARD_FIRM:
			return "STD";		
		case NO_Q_CHANGE_FIRM:
			return "NO_CHG";
		case NO_Q_REDUCTION_FIRM:
			return "NO_RED";
		case NO_Q_INCREASE_FIRM:
			return "NO_INC";
		case INCREASE_Q_FIRM:
			return "INC";
		}
		return "NO_TYPE";
	}
	
}
