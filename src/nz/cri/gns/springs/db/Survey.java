package nz.cri.gns.springs.db;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Survey extends PersistentObject {

	private static final long serialVersionUID = 8672919678197291499L;

	@DatabaseField(foreign = true)
	private Feature feature;
	
	@DatabaseField private Long surveyDate;
	@DatabaseField private Double size;
	@DatabaseField private String colour;
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
	public Double getSize() {
		return size;
	}
	public void setSize(Double size) {
		this.size = size;
	}
	public String getColour() {
		return colour;
	}
	public void setColour(String colour) {
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
	

}
