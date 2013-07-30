package nz.cri.gns.springs.db;

public class Feature extends PersistentObject {

	private String featureName;
	private String historicName;
	private String featureType;
	private String geothermalField;
	private Double coordNztmEast;
	private Double coordNztmNorth;
	private Double coordErrorEst;
	private String coordFeatureRel;
	private String description;
	
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getHistoricName() {
		return historicName;
	}
	public void setHistoricName(String historicName) {
		this.historicName = historicName;
	}
	public String getFeatureType() {
		return featureType;
	}
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	public String getGeothermalField() {
		return geothermalField;
	}
	public void setGeothermalField(String geothermalField) {
		this.geothermalField = geothermalField;
	}
	public Double getCoordNztmEast() {
		return coordNztmEast;
	}
	public void setCoordNztmEast(Double coordNztmEast) {
		this.coordNztmEast = coordNztmEast;
	}
	public Double getCoordNztmNorth() {
		return coordNztmNorth;
	}
	public void setCoordNztmNorth(Double coordNztmNorth) {
		this.coordNztmNorth = coordNztmNorth;
	}
	public Double getCoordErrorEst() {
		return coordErrorEst;
	}
	public void setCoordErrorEst(Double coordErrorEst) {
		this.coordErrorEst = coordErrorEst;
	}
	public String getCoordFeatureRel() {
		return coordFeatureRel;
	}
	public void setCoordFeatureRel(String coordFeatureRel) {
		this.coordFeatureRel = coordFeatureRel;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return this.getFeatureName();
	}
	
	
}
