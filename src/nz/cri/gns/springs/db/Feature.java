package nz.cri.gns.springs.db;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Feature extends PersistentObject {

	private static final long serialVersionUID = -5005994265425700595L;
	
	@DatabaseField private String featureName;
	@DatabaseField private String historicName;	
	@DatabaseField private String featureType;	
	@DatabaseField private String geothermalField;
	@DatabaseField private Double coordNztmEast;
	@DatabaseField private Double coordNztmNorth;
	@DatabaseField private Double coordErrorEst;
	@DatabaseField private String coordFeatureRel;
	@DatabaseField private String description;
	
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
	
	public static Feature getByName(String featureName, SpringsDbHelper dbHelper) {

		RuntimeExceptionDao<Feature, Long> dao = dbHelper.getFeatureDao();
		QueryBuilder<Feature, Long> queryBuilder = dao.queryBuilder();
		try {
			queryBuilder.where().eq("featureName", featureName);
			PreparedQuery<Feature> preparedQuery = queryBuilder.prepare();
			List<Feature> featureList = dao.query(preparedQuery);
			if (featureList.size() > 0) {
				return featureList.get(0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return featureList;
	}
	
	
}
