package nz.cri.gns.springs.activity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.BiologicalSample.CurrentSample;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.fragments.ExportSamplesFragment;
import nz.cri.gns.springs.fragments.ImageFragment;
import nz.cri.gns.springs.util.UiUtil;
import nz.cri.gns.springs.util.Util;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

/**
 * Activity which displays the list of biological samples which have not yet been exported,
 * and allows the user to select samples for viewing, updating or exporting to local storage as a text file.
 * Once samples are exported they are no longer displayed in the list.
 * 
 * The directory-chooser requires the 3rd party OI File Manager from Open Intents. This can downloaded (free) 
 * from the Google Play Store.
 * @author duncanw
 */
public class ManageBioSamplesActivity extends OrmLiteBaseActivity<SpringsDbHelper> implements OnClickListener, CompoundButton.OnCheckedChangeListener, ExportSamplesFragment.TaskCallbacks {
	
	private static final int PICK_DIRECTORY = 1;
	private static final String SELECTED_SAMPLES_LIST_KEY = "selectedSamples";
	private static final String EXPORT_SAMPLES_TASK = "exportSamplesTask";
	
	private ArrayList<Long> selectedSamples;
	private ProgressDialog progressDialog;
	private ExportSamplesFragment exportSamplesFragment;

	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bio_samples);
        if (savedInstanceState != null) {
	        Serializable ser = savedInstanceState.getSerializable(SELECTED_SAMPLES_LIST_KEY);
	        if (ser != null) {
	        	selectedSamples = (ArrayList<Long>)ser;
	        }
        }
        
        exportSamplesFragment = (ExportSamplesFragment) getFragmentManager().findFragmentByTag(EXPORT_SAMPLES_TASK);
     
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (exportSamplesFragment != null && exportSamplesFragment.isExportInProgress()) {
        	showProgressDialog();
        }
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final ArrayList<Long> selectedSamples = new ArrayList<Long>();
		UiUtil.ViewFilter viewFilter = new UiUtil.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox && ((CheckBox)view).isChecked()) {
					selectedSamples.add((Long)view.getTag());
				}
				return false;
			}
		};
		UiUtil.getChildren(this.findViewById(R.id.table_scrollview), null, viewFilter);
		outState.putSerializable(SELECTED_SAMPLES_LIST_KEY, selectedSamples);
	}
	
	public void setSelectedSamples() {
		if (selectedSamples != null) {
			View scrollView = this.findViewById(R.id.table_scrollview);
			for (Long tag : selectedSamples) {
				View checkBox = scrollView.findViewWithTag(tag);
				if (checkBox != null && checkBox instanceof CheckBox) {
					((CheckBox)checkBox).setChecked(true);
				}
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		listSamples();
        CheckBox selectAllNone = (CheckBox)this.findViewById(R.id.select_all_none);
        selectAllNone.setOnCheckedChangeListener(this);
        setSelectAllNoneText(selectAllNone, false);
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		if (requestCode == PICK_DIRECTORY) {
    			 Uri fileUri = data.getData();
                 if (fileUri != null) {
                     String filePath = fileUri.getPath();
                     if (filePath != null) {
                    	 export(filePath);
                     }
                 }    			
    		}
    	}
    }
	
	
	public void listSamples() {

		List<CurrentSample> sampleList = BiologicalSample.getCurrentSamples(getHelper());
		
		ScrollView scrollView = (ScrollView)findViewById(R.id.table_scrollview);
		scrollView.removeAllViews();
		
		TableLayout sampleTable = (TableLayout)getLayoutInflater().inflate(R.layout.template_table, null);
		for (CurrentSample sample : sampleList) {
			sampleTable.addView(getTableRow(sample));
		}
		
		scrollView.addView(sampleTable);
		
        View exportButton = findViewById(R.id.export_button);
        final Context context = this;

        if (sampleList.size() > 0) {
	        exportButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	String action = "org.openintents.action.PICK_DIRECTORY";
	            	if (!SpringsApplication.isIntentAvailable(context, action)) {
	            		UiUtil.showMessageDialog(context, "Sample export", 
	            				"Folder selection module (OI File Manager) must be installed, please install from Play Store");	
	            		return;
	            	}
	            	
	        		List<BiologicalSample> sampleList = getSelectedSamples(); 
	        		if (sampleList.isEmpty()) {
	        			UiUtil.showMessageDialog(context, "Sample export", "Please select the samples you want to export");
	        			return;
	        		}
	            	
	                Intent intent = new Intent(action);
	                intent.putExtra("org.openintents.extra.TITLE", "Select folder to export sample data to");
	                intent.putExtra("org.openintents.extra.BUTTON_TEXT", "Select current folder");
	                startActivityForResult(intent, PICK_DIRECTORY);
	            }
	        });
	        
	        setSelectedSamples();
	        
        } else {
        	exportButton.setVisibility(View.INVISIBLE);
        }
	}
	
	public void export(String directory) {

		String timestamp = Util.getTimestampSeconds();
		File exportDir = new File(directory + "/" + timestamp);
		if (!exportDir.exists()) {
			if (!exportDir.mkdirs()) {
				Log.e(ImageFragment.class.getName(), "Failed to create export directory "+exportDir.getAbsolutePath());
			}
		}
		
        exportSamplesFragment = (ExportSamplesFragment) getFragmentManager().findFragmentByTag(EXPORT_SAMPLES_TASK);
        if (exportSamplesFragment == null) {
        	exportSamplesFragment = new ExportSamplesFragment();
        }
		// List<BiologicalSample> sampleList, SpringsDbHelper helper, String exportDir, String timestamp, String directory
		exportSamplesFragment.setParameters(getSelectedSamples(), getHelper(), exportDir.getAbsolutePath(), timestamp, directory);
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().add(exportSamplesFragment, EXPORT_SAMPLES_TASK).commit();
	}
		
	
	public List<BiologicalSample> getSelectedSamples() {
		final List<BiologicalSample> selectedSamples = new LinkedList<BiologicalSample>();
		final SpringsDbHelper helper = getHelper();
		UiUtil.ViewFilter viewFilter = new UiUtil.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox && ((CheckBox)view).isChecked()) {
					BiologicalSample sample = helper.getBiologicalSampleDao().queryForId((Long)view.getTag());
					selectedSamples.add(sample);
				}
				return false;
			}
		};
		UiUtil.getChildren(this.findViewById(R.id.table_scrollview), null, viewFilter);
		return selectedSamples;
	}
	
	public TableRow getTableRow(CurrentSample sample) {
		
		TableRow tableRow = new TableRow(this.getBaseContext());
		
		CheckBox checkRow = (CheckBox)getLayoutInflater().inflate(R.layout.template_checkbox, null);
		checkRow.setTag(sample.sampleId);
		tableRow.addView(checkRow);
		
		TextView sampleNumber = (TextView)getLayoutInflater().inflate(R.layout.template_link, null);
		SpannableString content = new SpannableString(BiologicalSample.formatSampleNumber(sample.sampleNumber));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		sampleNumber.setText(content);
		sampleNumber.setTag(sample.sampleId);
		sampleNumber.setOnClickListener(this);
		tableRow.addView(sampleNumber);
		
		TextView featureName = (TextView)getLayoutInflater().inflate(R.layout.template_column_fixed_width, null);
		if (sample.featureName != null) {
			featureName.setText(sample.featureName);
		}
		tableRow.addView(featureName);
		
		TextView collectionDate = (TextView)getLayoutInflater().inflate(R.layout.template_column, null);
		if (sample.surveyDate != null) {
	        collectionDate.setText(UiUtil.getDisplayDate(sample.surveyDate));		
		}
		tableRow.addView(collectionDate);
		
		return tableRow;
		
	}


	@Override
	public void onClick(View v) {
		
		Long sampleId = (Long)v.getTag();
        Intent intent = new Intent(ManageBioSamplesActivity.this, BioSampleActivity.class);        
        intent.putExtra(BioSampleActivity.BIOLOGICAL_SAMPLE, sampleId);
        startActivity(intent);
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
		
		setSelectAllNoneText(buttonView, isChecked);
		UiUtil.getChildren(this.findViewById(R.id.bio_sample_table), new ArrayList<View>(), new UiUtil.ViewFilter() {		
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox) {
					((CheckBox)view).setChecked(isChecked);
				}
				
				return false;
			}
		});
		
	}
	
	private void setSelectAllNoneText(CompoundButton selectAllNone, boolean isChecked) {
		
		String text = (isChecked) ? "Select none" : "Select all";
		selectAllNone.setText(text);
	}

	@Override
	public void onPreSampleExport() {
		showProgressDialog();
	}
	
	private void showProgressDialog() {
		progressDialog = ProgressDialog.show(this, "Exporting samples", 
                "Please wait...", true);
	}
	
	private void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onPostSampleExport(Integer exportCount, Integer notExportedCount, String exportDir) {
		
		hideProgressDialog();
		
		if (notExportedCount > 0) {
			String message = (notExportedCount == 1) ? 
					"The sample is not associated with a feature, so was not exported" :
						notExportedCount + " samples are not associated with features, so were not exported";
			UiUtil.showMessageDialog(this, "Sample export", message);
		}
		
		if (exportCount > 0) {
			String message = (exportCount == 1) ?
					exportCount + " sample exported to "+exportDir :
					exportCount + " samples exported to "+exportDir;
			Toast.makeText(SpringsApplication.getAppContext(), message, Toast.LENGTH_LONG).show();  
			this.listSamples();
		}
	}
	
	@Override
	public void onSampleExportError(String directory) {
		
		hideProgressDialog();
		
		// Most likely cause is the user selected a system folder or a USB drive when no memory stick
		// is inserted.
		String message = "Export to "+directory+" failed. Please confirm the selected folder is accessible before retrying the export.";
		UiUtil.showMessageDialog(this, "Export failed", message);
	}
}
