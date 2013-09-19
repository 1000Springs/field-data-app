package nz.cri.gns.springs.db;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import nz.cri.gns.springs.util.Util;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A Feature is a geothermal feature (e.g a hot spring).
 * @author duncanw
 *
 */
@DatabaseTable
public class Feature extends PersistentObject {

	private static final long serialVersionUID = -5005994265425700595L;
	
	@DatabaseField private String featureName;
	@DatabaseField private String historicName;	
	@DatabaseField private String featureType;	
	@DatabaseField private String geothermalField;
	@DatabaseField private Double coordLatitude;
	@DatabaseField private Double coordLongitude;
	@DatabaseField private Float coordErrorEst;
	@DatabaseField private String coordFeatureRel;
	@DatabaseField private String description;
	@DatabaseField private String accessType;
	
	public enum AccessType {
		PRIVATE("Private"), PUBLIC_FREE("Public, free"), PUBLIC_PAID("Public, not free");

		String description;
		
		AccessType(String description) {
			this.description = description;
		}
		
		@Override
		public String toString() {
			return description;
		}
	}
	
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
	public Double getCoordLatitude() {
		return coordLatitude;
	}
	public void setCoordLatitude(Double coordLatitude) {
		this.coordLatitude = coordLatitude;
	}
	public Double getCoordLongitude() {
		return coordLongitude;
	}
	public void setCoordLongitude(Double coordLongitude) {
		this.coordLongitude = coordLongitude;
	}
	public Float getCoordErrorEst() {
		return coordErrorEst;
	}
	public void setCoordErrorEst(Float coordErrorEst) {
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
	public String getAccessType() {
		return accessType;
	}
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}
	public String toString() {
		return this.getFeatureName();
	}
	
	/**
	 * @return true if this Feature's status is NEW or UPDATED, otherwise returns false.
	 */
	public boolean isForExport() {
		return status == Status.NEW || status == Status.UPDATED;
	}
	
	/**
	 * @return a tab separated string of this Feature's values. Empty strings are used
	 *         for null values, numeric values are rounded to 4 decimal places.
	 */
	public String toTsvString() {
		
		String desc = (description != null) ? description.replace("\n", " ") : null;
		return Util.join("\t", 
				featureName, historicName, featureType, geothermalField, 
				Util.format(coordLatitude), Util.format(coordLongitude), Util.format(coordErrorEst),
				coordFeatureRel, desc, accessType
				);
	}
	
	public static String tsvStringColumns() {
		return Util.join("\t", 
				"FeatureName", "HistoricName", "FeatureType", "GeothermalField", 
				"LocationLatitude", "LocationLongitude", "LocationErrorEstimateMetres", 
				"LocationRelationShipToFeature", "Description", "AccessType");		
	}
	
	public static Feature getByName(String featureName, SpringsDbHelper dbHelper) {

		RuntimeExceptionDao<Feature, Long> dao = dbHelper.getFeatureDao();
		QueryBuilder<Feature, Long> queryBuilder = dao.queryBuilder();
		try {
			SelectArg selectArg = new SelectArg();
			selectArg.setValue(featureName);
			PreparedQuery<Feature> preparedQuery = queryBuilder.where().eq("featureName", selectArg).prepare();
			List<Feature> featureList = dao.query(preparedQuery);
			if (featureList.size() > 0) {
				return featureList.get(0);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;

	}
	
	public static List<Feature> getAll(SpringsDbHelper dbHelper) {
		
		RuntimeExceptionDao<Feature, Long> dao = dbHelper.getFeatureDao();
		List<Feature> featureList = new LinkedList<Feature>();
		CloseableIterator<Feature> iterator = dao.closeableIterator();
		try {
		    while (iterator.hasNext()) {
		    	featureList.add(iterator.next());
		    }
		} finally {
		    try {
				iterator.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return featureList;
	}
	
	
}
