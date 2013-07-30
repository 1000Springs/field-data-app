package nz.cri.gns.springs.db;

import android.content.ContentValues;
import android.database.Cursor;
import nz.cri.gns.springs.Util;
import nz.cri.gns.springs.db.PersistentObject.Status;

public class PersistentObjectDb {
	
	protected SpringsDbHelper dbHelper;
	
	public PersistentObjectDb(SpringsDbHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public static String getBaseColumnsForCreateTable() {
		return Util.join(
				new String[] { 
						"_ID integer primary key",
						"CREATED_DATE integer", 
						"UPDATED_DATE integer",
						"STATUS text" 
						}, ",");
	}
	
	public void addBaseValuesForCreate(PersistentObject object, ContentValues values) {
		values.put("CREATED_DATE", object.getCreatedDate());
		values.put("UPDATED_DATE", object.getUpdatedDate());
		values.put("STATUS", object.getStatus());
	}
	
	public String getBaseColumnsCsv() {
		return Util.join(new String[]{
				"_ID",
				"CREATED_DATE",
				"UPDATED_DATE",
				"STATUS",
				}, ", ");
	}
	
	public void setObjectValuesFromRow(PersistentObject object, Cursor currentRow) {
		object.setId(currentRow.getLong(currentRow.getColumnIndex("_ID")));
		object.setCreatedDate(currentRow.getLong(currentRow.getColumnIndex("CREATED_DATE")));
		object.setUpdatedDate(currentRow.getLong(currentRow.getColumnIndex("UPDATED_DATE")));
		object.setStatus(currentRow.getString(currentRow.getColumnIndex("STATUS")));
	}
	
	public String getBaseColumnsForUpdate() {
		return Util.join(new String[]{
				"CREATED_DATE=?",
				"UPDATED_DATE=?",
				"STATUS=?",
				}, ", ");
	}
	
	
	public Object[] getBaseColumnValuesForUpdate(PersistentObject object) {
		object.setUpdatedDate(System.currentTimeMillis());
		object.setStatus(Status.UPDATED);
		return new Object[]{object.getCreatedDate(), object.getUpdatedDate(), object.getStatus()};
	}
}
