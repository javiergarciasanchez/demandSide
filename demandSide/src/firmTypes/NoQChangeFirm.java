package firmTypes;

import java.math.BigDecimal;
import java.util.stream.Stream;

import firms.Firm;

public class NoQChangeFirm extends Firm {

	public Stream<BigDecimal> getRealQualityOptions() {
		
		Stream.Builder<BigDecimal> b = Stream.builder();;		
		
		b.add(getQuality());
		
		return b.build();		
		
	}

}
