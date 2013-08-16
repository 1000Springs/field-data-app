package nz.cri.gns.springs.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.BiologicalSample.CurrentSample;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.PersistentObject.Status;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.db.SurveyImage;
import nz.cri.gns.springs.fragments.ImageFragment;
import nz.cri.gns.springs.util.UiUtil;
import nz.cri.gns.springs.util.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.Time;
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

public class ManageBioSamplesActivity extends OrmLiteBaseActivity<SpringsDbHelper> implements OnClickListener, CompoundButton.OnCheckedChangeListener {
	
	private static final int PICK_DIRECTORY = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bio_samples);
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
	            	
	        		List<BiologicalSample> sampleList = getSelectedSamples(); 
	        		if (sampleList.isEmpty()) {
	        			UiUtil.showWarningDialog(context, "Sample export", "Please select the samples you want to export");
	        			return;
	        		}
	            	
	                Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
	                intent.putExtra("org.openintents.extra.TITLE", "Select folder to export sample data to");
	                intent.putExtra("org.openintents.extra.BUTTON_TEXT", "Select current folder");
	                startActivityForResult(intent, PICK_DIRECTORY);
	            }
	        });
        } else {
        	exportButton.setVisibility(View.INVISIBLE);
        }
	}
	
	public void export(String directory) {
		// Select all features, biological samples, surveys (combine survey with sample)
        Time now = new Time(Time.getCurrentTimezone());
       	now.set(System.currentTimeMillis());
		String timestamp = now.format("%Y%m%d%H%M%S");	
		File exportDir = new File(directory + "/" + timestamp);
		if (!exportDir.exists()) {
			if (!exportDir.mkdirs()) {
				Log.e(ImageFragment.class.getName(), "Failed to create image directory "+exportDir.getAbsolutePath());
			}
		}		
		try {
			int exportCount = exportSamples(exportDir.getAbsolutePath(), timestamp);
			if (exportCount > 0) {
				String message = (exportCount == 1) ?
						exportCount + " sample exported to "+exportDir :
						exportCount + " samples exported to "+exportDir;
				Toast.makeText(SpringsApplication.getAppContext(), message, Toast.LENGTH_LONG).show();  
				listSamples();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(this.getClass().getName(), "Feature export error", e);
		}
	}
	
	public int exportSamples(String exportDir, String timestamp) throws IOException {
		
		List<BiologicalSample> sampleList = getSelectedSamples(); 
		String sampleFile = exportDir + "/" + "samples-"+timestamp+".tsv";
		String featureFile = exportDir + "/" + "features-"+timestamp+".tsv";
		BufferedWriter sampleWriter = null;
		BufferedWriter featureWriter = null;
		List<String> samplesNotExported = new LinkedList<String>();
		List<Feature> featuresExported = new LinkedList<Feature>();	
		List<Survey> surveysExported = new LinkedList<Survey>();
		List<BiologicalSample> samplesExported = new LinkedList<BiologicalSample>();
		try {
			String encoding = "UTF-8";
			for (BiologicalSample sample : sampleList) {	
				Survey survey = sample.getSurvey();
				getHelper().getSurveyDao().refresh(survey);
				Feature feature = survey.getFeature();
				if (feature != null) {	
					if (sampleWriter == null) {
						sampleWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(sampleFile), encoding)));
					}					
					getHelper().getFeatureDao().refresh(feature);
					if (feature.isForExport()) {
						if (featureWriter == null) {
							featureWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(featureFile), encoding)));
						}
						featureWriter.write(feature.toTsvString());
						featureWriter.newLine();
						featuresExported.add(feature);
					}
					sampleWriter.write(Util.join("\t", feature.getFeatureName(), sample.toTsvString(), survey.toTsvString()));
					sampleWriter.newLine();
					exportImages(exportDir, sample.getFormattedSampleNumber(), survey);
					surveysExported.add(survey);
					samplesExported.add(sample);
				} else {
					samplesNotExported.add(sample.getFormattedSampleNumber());
				}
			}

		} finally {
			if (sampleWriter != null) {
				sampleWriter.close();
			}
			if (featureWriter != null) {
				featureWriter.close();
			}			
		} 
		
		if (!samplesNotExported.isEmpty()) {
			String message = (samplesNotExported.size() == 1) ? 
					"The sample is not associated with a feature, so was not exported" :
					samplesNotExported.size() + " samples are not associated with features, so were not exported";
			UiUtil.showWarningDialog(this, "Sample export", message);
		}
		
		for (Feature feature : featuresExported) {
			feature.setStatus(Status.EXPORTED);
			getHelper().getFeatureDao().update(feature);
		}
		
		for (Survey survey : surveysExported) {
			survey.setStatus(Status.EXPORTED);
			getHelper().getSurveyDao().update(survey);
		}
		
		for (BiologicalSample sample : samplesExported) {
			sample.setStatus(Status.EXPORTED);
			getHelper().getBiologicalSampleDao().update(sample);
		}
		
		return samplesExported.size();
	}
	
	public List<BiologicalSample> getSelectedSamples() {
		final List<BiologicalSample> selectedSamples = new LinkedList<BiologicalSample>();
		final SpringsDbHelper helper = getHelper();
		Util.ViewFilter viewFilter = new Util.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox && ((CheckBox)view).isChecked()) {
					BiologicalSample sample = helper.getBiologicalSampleDao().queryForId((Long)view.getTag());
					selectedSamples.add(sample);
				}
				return false;
			}
		};
		Util.getChildren(this.findViewById(R.id.table_scrollview), null, viewFilter);
		return selectedSamples;
	}
	
	public void exportImages(String exportDir, String sampleNumber, Survey survey) throws IOException {
		List<SurveyImage> imageList = SurveyImage.getBySurvey(survey,getHelper());	
		for (SurveyImage image : imageList) {
			File imageSrc  = new File (image.getFileName());
			String imageType = (image.getImageType() != null) ? image.getImageType().replaceAll("_", "") : "";
			File exportDest = new File(exportDir + "/" + Util.join("_", sampleNumber, imageType, imageSrc.getName()));
			Util.copy(imageSrc, exportDest);
		}
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
	        Time now = new Time(Time.getCurrentTimezone());
	        now.set(sample.surveyDate);
	        collectionDate.setText(now.format("%c"));		
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
		Util.getChildren(this.findViewById(R.id.bio_sample_table), new ArrayList<View>(), new Util.ViewFilter() {		
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
}
