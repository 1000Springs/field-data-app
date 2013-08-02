package nz.cri.gns.springs.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class BiologicalSample extends PersistentObject {
	
	@DatabaseField(foreign = true)
	private Survey survey;
	
	@DatabaseField private String sampleNumber;
	@DatabaseField private Double temperature;
	@DatabaseField private Double pH;
	@DatabaseField private Double orp;
	@DatabaseField private Double conductivity;
	@DatabaseField private Double dO;
	@DatabaseField private Double turbidity;
	@DatabaseField private Double dnaVolume;
	@DatabaseField private Double ferrousIronAbs;
	
	public Survey getSurvey() {
		return survey;
	}
	public void setSurveyId(Survey survey) {
		this.survey = survey;
	}
	public String getSampleNumber() {
		return sampleNumber;
	}
	public void setSampleNumber(String sampleNumber) {
		this.sampleNumber = sampleNumber;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	public Double getPh() {
		return pH;
	}
	public void setPh(Double pH) {
		this.pH = pH;
	}
	public Double getOrp() {
		return orp;
	}
	public void setOrp(Double orp) {
		this.orp = orp;
	}
	public Double getConductivity() {
		return conductivity;
	}
	public void setConductivity(Double conductivity) {
		this.conductivity = conductivity;
	}
	public Double getDo() {
		return dO;
	}
	public void setDo(Double dO) {
		this.dO = dO;
	}
	public Double getTurbidity() {
		return turbidity;
	}
	public void setTurbidity(Double turbidity) {
		this.turbidity = turbidity;
	}
	public Double getDnaVolume() {
		return dnaVolume;
	}
	public void setDnaVolume(Double dnaVolume) {
		this.dnaVolume = dnaVolume;
	}
	public Double getFerrousIronAbs() {
		return ferrousIronAbs;
	}
	public void setFerrousIronAbs(Double ferrousIronAbs) {
		this.ferrousIronAbs = ferrousIronAbs;
	}
	
	
}
