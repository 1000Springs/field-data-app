package nz.cri.gns.springs.db;

import junit.framework.TestCase;
import nz.cri.gns.springs.db.BiologicalSample;

public class BiologicalSampleTest extends TestCase {

	public void testFormatSampleNumber() {
		assertEquals("P1.0001", BiologicalSample.formatSampleNumber(1));
		assertEquals("P1.12345", BiologicalSample.formatSampleNumber(12345));
		
	}
	
	public void testToTsvStringNullValue() {
		BiologicalSample sample = new BiologicalSample();
		sample.setId(1l);
		sample.setComments("This is a comment");
		sample.setConductivity(0.98);
		sample.setDnaVolume(2.123456);
		sample.setDo(3.14956);
		sample.setFerrousIronAbs(null);
		sample.setOrp(4.568);
		sample.setPh(6.5);
		sample.setSampleNumber(12);
		sample.setTemperature(556.36954);
		sample.setTurbidity(-56.3);
		
		String expected = "P1.0012\t556.3695\t6.5\t4.568\t0.98\t3.1496\t-56.3\t2.1235\t\tThis is a comment";
		String actual = sample.toTsvString();
		assertEquals(expected, actual);
	}
	
	public void testToTsvStringMultilineComment() {
		BiologicalSample sample = new BiologicalSample();
		sample.setId(1l);
		sample.setComments("This is a\nmultiline comment");
		sample.setConductivity(0.98);
		sample.setDnaVolume(2.123456);
		sample.setDo(3.14956);
		sample.setFerrousIronAbs(789.654123);
		sample.setOrp(4.568);
		sample.setPh(6.5);
		sample.setSampleNumber(12);
		sample.setTemperature(556.36954);
		sample.setTurbidity(-56.3);
		
		String expected = "P1.0012\t556.3695\t6.5\t4.568\t0.98\t3.1496\t-56.3\t2.1235\t789.6541\tThis is a multiline comment";
		String actual = sample.toTsvString();
		assertEquals(expected, actual);
	}	

}
