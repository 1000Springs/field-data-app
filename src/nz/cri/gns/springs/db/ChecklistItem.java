package nz.cri.gns.springs.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

/**
 * CheckListItems are named true/false flags that can be grouped and each may be
 * associated with other objects stored in the database.
 * @author duncanw
 *
 */
@DatabaseTable
public class ChecklistItem {
	
	@DatabaseField(generatedId = true) 
	private Long id;
	
	// Used to group ChecklistItems and/or identify which type of database object
	// the ChecklistItem is associated with
	@DatabaseField private String checklistName;
	@DatabaseField private String itemName;
	@DatabaseField private Boolean itemValue;
	@DatabaseField private Long objectId;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChecklistName() {
		return checklistName;
	}
	public void setChecklistName(String checklistName) {
		this.checklistName = checklistName;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public Boolean getItemValue() {
		return itemValue;
	}
	public void setItemValue(Boolean itemValue) {
		this.itemValue = itemValue;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	
	
	public static List<ChecklistItem> getBy(String checklistName, Long objectId, SpringsDbHelper dbHelper) {
		
		RuntimeExceptionDao<ChecklistItem, Long> dao = dbHelper.getChecklistItemDao();
		QueryBuilder<ChecklistItem, Long> queryBuilder = dao.queryBuilder();
		List<ChecklistItem> checklistItemList;
		try {
			queryBuilder
				.where().eq("checklistName", checklistName)
				.and().eq("objectId", objectId);
			PreparedQuery<ChecklistItem> preparedQuery = queryBuilder.prepare();
			checklistItemList = dao.query(preparedQuery);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			checklistItemList = new ArrayList<ChecklistItem>();
		}
		return checklistItemList;
	}

}
