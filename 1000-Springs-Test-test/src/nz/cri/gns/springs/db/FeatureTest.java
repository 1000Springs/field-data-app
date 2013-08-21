package nz.cri.gns.springs.db;


import org.junit.Test;

import junit.framework.TestCase;

public class FeatureTest extends TestCase {

	@Test
	public void testToTsvStringNullValue() {
		Feature feature = new Feature();
		feature.setCoordErrorEst(8.0f);
		feature.setCoordFeatureRel("In the middle");
		feature.setCoordLatitude(-37.2585246);
		feature.setCoordLongitude(176.25856);
		feature.setDescription("This is a description");
		feature.setFeatureName("Wairakei Springs");
		feature.setFeatureType("spring");
		feature.setGeothermalField(null);
		feature.setHistoricName("Ye olde Wairakei");
		
		String expected = "Wairakei Springs\tYe olde Wairakei\tspring\t\t-37.2585\t176.2586\t8\tIn the middle\tThis is a description";
		String actual = feature.toTsvString();
		assertEquals(expected, actual);
	}

	@Test
	public void testToTsvStringMultilineDescription() {
		Feature feature = new Feature();
		feature.setCoordErrorEst(8.0f);
		feature.setCoordFeatureRel("In the middle");
		feature.setCoordLatitude(-37.2585246);
		feature.setCoordLongitude(176.25856);
		feature.setDescription("This\nis\na\nmultiline\ndescription");
		feature.setFeatureName("Wairakei Springs");
		feature.setFeatureType("spring");
		feature.setGeothermalField("geo field");
		feature.setHistoricName("Ye olde Wairakei");
		
		String expected = "Wairakei Springs\tYe olde Wairakei\tspring\tgeo field\t-37.2585\t176.2586\t8\tIn the middle\tThis is a multiline description";
		String actual = feature.toTsvString();
		assertEquals(expected, actual);

	}
}
