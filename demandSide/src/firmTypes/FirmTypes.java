package firmTypes;

import demandSide.Market;

public enum FirmTypes {
	
	STANDARD_FIRM,
	NO_Q_CHANGE_FIRM,
	NO_Q_REDUCTION_FIRM,
	NO_Q_INCREASE_FIRM,
	INCREASE_Q_FIRM;
	
	public void createRandomTypeFirm(Market market) {
		
		int i = market.firms.firmTypes.nextInt() - 1;
		
		(values()[i]).createFirm(market);
		
	}
	
	public void createFirm(Market market){
		
		switch (this) {
		case STANDARD_FIRM:
			new StandardFirm(market);
			break;
		case NO_Q_CHANGE_FIRM:
			new NoQChangeFirm(market);
			break;
		case NO_Q_REDUCTION_FIRM:
			new NoQReductionFirm(market);
			break;
		case NO_Q_INCREASE_FIRM:
			new NoQIncreaseFirm(market);
			break;
		case INCREASE_Q_FIRM:
			new IncreaseQFirm(market);
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
