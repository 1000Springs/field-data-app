package nz.cri.gns.springs.db;

import java.util.LinkedList;
import java.util.List;

import nz.cri.gns.springs.Util;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FeatureDb extends PersistentObjectDb {
	
	public FeatureDb(SpringsDbHelper dbHelper) {
		super(dbHelper);
	}
	
	public static void createTable(SQLiteDatabase db) {
        
		db.execSQL(Util.join(new String[]{
        		"create table FEATURE (", 
        		Util.join(new String[] {
        				getBaseColumnsForCreateTable(),
        				"FEATURE_NAME text",
        				"HISTORIC_NAME text", 
        				"GEOTHERMAL_FIELD text",
        				"FEATURE_TYPE text",       				
        				"COORD_NZTM_EAST real",
        				"COORD_NZTM_NORTH real",
        				"COORD_ERROR_EST real",
        				"COORD_FEATURE_REL text",  
        				"DESCRIPTION text"
        		}, ","),
        		")"
        	}, "\n"));		
	}
	
	public Feature create(Feature feature) {
		
		ContentValues values = new ContentValues();
		addBaseValuesForCreate(feature, values);
		values.put("FEATURE_NAME", feature.getFeatureName());
		values.put("HISTORIC_NAME", feature.getHistoricName());
		values.put("FEATURE_TYPE", feature.getFeatureType());
		values.put("GEOTHERMAL_FIELD", feature.getGeothermalField());
		values.put("COORD_NZTM_EAST", feature.getCoordNztmEast());
		values.put("COORD_NZTM_NORTH", feature.getCoordNztmNorth());
		values.put("COORD_ERROR_EST", feature.getCoordErrorEst());
		values.put("COORD_FEATURE_REL", feature.getCoordFeatureRel());	
		values.put("DESCRIPTION", feature.getDescription());
		
		long newRowId = dbHelper.getWritableDatabase().insert(
				"FEATURE", null, values);
		
		feature.setId(newRowId);
		
		return feature;
	}
	
	public List<Feature> readAll() {
		String query = Util.join(new String[]{
				getSelectBase(),
				"order by FEATURE_NAME"
				}, "\n");
		
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, null);
		
		List<Feature> featureList = new LinkedList<Feature>();
		while (cursor.moveToNext()) {
			featureList.add(getFeatureFromRow(cursor));
		}
		
		cursor.close();
		
		return featureList;
	}
	
	public Feature getByName(String name) {
			
		String query = Util.join(new String[]{
				getSelectBase(),
				"where FEATURE_NAME=?"
				}, "\n");
		
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{name});
		
		Feature feature = null;
		
		if (cursor.moveToNext()) {
			feature = getFeatureFromRow(cursor);
		}
		
		cursor.close();
		
		return feature;
	}
	
	private String getSelectBase() {
		
		return Util.join(new String[]{
        		"select",
    			Util.join(new String[]{
    					getBaseColumnsCsv(),
    					"FEATURE_NAME",
    					"HISTORIC_NAME",
    					"GEOTHERMAL_FIELD",
    					"FEATURE_TYPE",
    					"COORD_NZTM_EAST",
    					"COORD_NZTM_NORTH",
    					"COORD_ERROR_EST",
    					"COORD_FEATURE_REL",
    					"DESCRIPTION",
    					}, ", "),
        		"from FEATURE"
		}, "\n");
	}
	
	private Feature getFeatureFromRow(Cursor currentRow) {
		
		Feature feature = new Feature();
		setObjectValuesFromRow(feature, currentRow);
		feature.setFeatureName(currentRow.getString(currentRow.getColumnIndex("FEATURE_NAME")));
		feature.setHistoricName(currentRow.getString(currentRow.getColumnIndex("HISTORIC_NAME")));
		feature.setGeothermalField(currentRow.getString(currentRow.getColumnIndex("GEOTHERMAL_FIELD")));
		feature.setFeatureType(currentRow.getString(currentRow.getColumnIndex("FEATURE_TYPE")));
		
		int doubleIndex = currentRow.getColumnIndex("COORD_NZTM_EAST");
		if (!currentRow.isNull(doubleIndex)) {
			feature.setCoordNztmEast(currentRow.getDouble(doubleIndex));	
		} 
		
		doubleIndex = currentRow.getColumnIndex("COORD_NZTM_NORTH");
		if (!currentRow.isNull(doubleIndex)) {
			feature.setCoordNztmNorth(currentRow.getDouble(doubleIndex));	
		} 
		
		doubleIndex = currentRow.getColumnIndex("COORD_ERROR_EST");
		if (!currentRow.isNull(doubleIndex)) {
			feature.setCoordErrorEst(currentRow.getDouble(doubleIndex));	
		} 

		feature.setCoordFeatureRel(currentRow.getString(currentRow.getColumnIndex("COORD_FEATURE_REL")));
		feature.setDescription(currentRow.getString(currentRow.getColumnIndex("DESCRIPTION")));
		
		return feature;
	}
	
	public void update(Feature feature) {
		
		String update = Util.join(new String[]{
        		"update FEATURE",
        		"SET " + Util.join(new String[]{
        				getBaseColumnsForUpdate(),
    					"FEATURE_NAME=?",
    					"HISTORIC_NAME=?",
    					"GEOTHERMAL_FIELD=?",
    					"FEATURE_TYPE=?",
    					"COORD_NZTM_EAST=?",
    					"COORD_NZTM_NORTH=?",
    					"COORD_ERROR_EST=?",
    					"COORD_FEATURE_REL=?",
    					"DESCRIPTION=?",
    					}, ", "),
			"where _ID=?"
			}, "\n");
		
		
		dbHelper.getWritableDatabase().execSQL(update, 
				Util.concatArrays(
						getBaseColumnValuesForUpdate(feature),
						new Object[]{
							feature.getFeatureName(),
							feature.getHistoricName(),
							feature.getGeothermalField(),
							feature.getFeatureType(),
							feature.getCoordNztmEast(),
							feature.getCoordNztmNorth(),
							feature.getCoordErrorEst(),
							feature.getCoordFeatureRel(),
							feature.getDescription(),
							feature.getId()
						}
					)
				);
		
	}
}
