package firms;

public class FirmsRealQSegments extends FirmsSegments {
	
	private static final long serialVersionUID = 1L;

	
	public FirmsRealQSegments(){
		super(new CompareByQ());
	}

	@Override
	public double getQuality(Firm f) {
		return f.getQuality();
	}
	
	@Override
	public double getQuality(Firm f, double q){
		return q;
	}

}
