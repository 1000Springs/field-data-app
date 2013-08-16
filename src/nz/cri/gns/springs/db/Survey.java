package nz.cri.gns.springs.db;


import nz.cri.gns.springs.util.Util;

import android.text.format.Time;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Survey extends PersistentObject {

	private static final long serialVersionUID = 8672919678197291499L;

	@DatabaseField(foreign = true)
	private Feature feature;
	
	@DatabaseField private Long surveyDate;
	@DatabaseField private String size; // e.g "4 x 3 metres"
	@DatabaseField private Integer colour;
	@DatabaseField private String clarityTurbidity;
	@DatabaseField private Double temperature;
	@DatabaseField private String observer1;
	@DatabaseField private String observer2;
	
	public Feature getFeature() {
		return feature;
	}
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public Long getSurveyDate() {
		return surveyDate;
	}
	public void setSurveyDate(Long surveyDate) {
		this.surveyDate = surveyDate;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public Integer getColour() {
		return colour;
	}
	public void setColour(Integer colour) {
		this.colour = colour;
	}
	public String getClarityTurbidity() {
		return clarityTurbidity;
	}
	public void setClarityTurbidity(String clarityTurbidity) {
		this.clarityTurbidity = clarityTurbidity;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	public String getObserver1() {
		return observer1;
	}
	public void setObserver1(String observer1) {
		this.observer1 = observer1;
	}
	public String getObserver2() {
		return observer2;
	}
	public void setObserver2(String observer2) {
		this.observer2 = observer2;
	}
	
	public String toTsvString() {
		String date = null;
		if (surveyDate != null) {
	        Time now = new Time(Time.getCurrentTimezone());
	       	now.set(surveyDate);
	        date = now.format("%Y-%m-%d %H:%M:%S");			
		}
		
		String hexColour = (colour != null) ? Integer.toHexString(colour) : null;

		return Util.join("\t", date, size, hexColour, clarityTurbidity, Util.format(temperature), observer1, observer2);
	}
	

}
