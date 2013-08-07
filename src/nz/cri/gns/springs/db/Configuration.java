package nz.cri.gns.springs.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Configuration {
	
	@DatabaseField(canBeNull = false, id = true)
	private String key;
	
	@DatabaseField private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static void setConfiguration(String key, String value, SpringsDbHelper helper) {
		
		Configuration config = helper.getConfigurationDao().queryForId(key);
		if (config == null) {
			config = new Configuration();
			config.setKey(key);
			config.setValue(value);
			helper.getConfigurationDao().create(config);
		} else {
			config.setValue(value);
			helper.getConfigurationDao().update(config);
		}
	}
	
	public static String getConfiguration(String key, SpringsDbHelper helper) {
		Configuration config = helper.getConfigurationDao().queryForId(key);
		if (config == null) {
			return null;
		}
		
		return config.getValue();
	}

}
