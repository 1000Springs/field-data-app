package nz.cri.gns.springs.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

public class SurveyTest extends TestCase {
	
	public void testToTsvString() throws ParseException {
		
		Survey survey = new Survey();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		Date surveyDate =  dateFormat.parse("2013-08-21 11:38:21");
		survey.setSurveyDate(surveyDate.getTime());
		survey.setEbullition("muddy");
		survey.setColour(0xAB34ff);
		survey.setObserver("old mate");
		survey.setSize("3 x 4 metres");
		survey.setTemperature(78.36945);
		
		String expected = "2013-08-21 11:38:21\t3 x 4 metres\tab34ff\tmuddy\t78.3694\told mate";
		String actual = survey.toTsvString();
		assertEquals(expected, actual);		
	}

}
