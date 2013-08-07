package nz.cri.gns.springs.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class BiologicalSample extends PersistentObject {

	private static final long serialVersionUID = -7201406858581042942L;

	@DatabaseField(foreign = true)
	private Survey survey;
	
	@DatabaseField private Integer sampleNumber;
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
	public void setSurvey(Survey survey) {
		this.survey = survey;
	}
	public Integer getSampleNumber() {
		return sampleNumber;
	}
	public void setSampleNumber(Integer sampleNumber) {
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
	
	public String getFormattedSampleNumber() {
		return formatSampleNumber(getSampleNumber());
	}
	
	public static String formatSampleNumber(Integer sampleNumber) {
		return "P1." + String.format("%04d", sampleNumber);
	}
	
	public static Integer getMaxSampleNumber(SpringsDbHelper dbHelper) {
		RuntimeExceptionDao<BiologicalSample, Long> dao = dbHelper.getBiologicalSampleDao();
		try {
			return (int)dao.queryRawValue("select max(sampleNumber) from BiologicalSample");
		} catch (Exception e) {
			Log.e(BiologicalSample.class.getSimpleName(), "Error retrieving max sampleNumber", e);
			return 0;
		}
	}
	
	
	public static class SamplesForExportResult {
		
		public Long sampleId;
		public Integer sampleNumber;
		public String featureName;
		public Long surveyDate;
	}
	
	public static List<SamplesForExportResult> getSamplesForExport(SpringsDbHelper dbHelper) {
		RuntimeExceptionDao<BiologicalSample, Long> dao = dbHelper.getBiologicalSampleDao();
		String query = 
				"select samp.id, samp.sampleNumber, feat.featureName, surv.surveyDate " + 
				"from BiologicalSample samp " +
				"join Survey surv on samp.survey_id = surv.id " + 
			    "join Feature feat on surv.feature_id = feat.id " +
				"where samp.status in (?, ?)";
		
		DataType[] columnTypes = {DataType.LONG, DataType.INTEGER, DataType.STRING, DataType.LONG};
		
		GenericRawResults<Object[]> results = dao.queryRaw(query, columnTypes, Status.NEW.name(), Status.UPDATED.name());
		try {
			List<Object[]> rows = results.getResults();
			List<SamplesForExportResult> sampleList = new ArrayList<SamplesForExportResult>(rows.size());
			for (Object[] row : rows) {
				SamplesForExportResult sample = new SamplesForExportResult();
				sample.sampleId = (Long)row[0];
				sample.sampleNumber = (Integer)row[1];
				sample.featureName = (String)row[2];
				sample.surveyDate = (Long)row[3];
				sampleList.add(sample);
			}
			
			return sampleList;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	
}
