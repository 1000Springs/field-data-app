package nz.cri.gns.springs.activity;

import java.util.ArrayList;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.db.Configuration;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.util.Util;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class SettingsActivity extends OrmLiteBaseActivity<SpringsDbHelper> implements OnClickListener {

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		Util.getChildren(this.findViewById(R.id.settings_table), new ArrayList<View>(), new Util.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				Object tag = view.getTag();
				if (view instanceof EditText && tag != null) {
					String value = Configuration.getConfiguration(tag.toString(), getHelper());
					if (value != null) {
						((EditText)view).setText(value);
					}
				}
				return false;
			}
		});
		
        Button saveSettings = (Button)this.findViewById(R.id.save_settings_button);
        saveSettings.setOnClickListener(this);
        
	}
	@Override
	public void onClick(View v) {
		Util.getChildren(this.findViewById(R.id.settings_table), new ArrayList<View>(), new Util.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				Object tag = view.getTag();
				if (view instanceof EditText && tag != null) {
					Configuration.setConfiguration(tag.toString(), ((EditText)view).getText().toString(), getHelper());
				}
				return false;
			}
		});
		
		this.finish();
		
	}

}
