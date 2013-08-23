package nz.cri.gns.springs.activity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.util.UiUtil;
import nz.cri.gns.springs.util.UiUtil.ViewFilter;
import nz.cri.gns.springs.util.Util;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.misc.TransactionManager;


/**
 * Screen which allows users to change the sample numbers of any sample in the database.
 * Required to recover from user errors.
 * @author duncanw
 *
 */
public class EditBiologicalSamplesActivity extends OrmLiteBaseActivity<SpringsDbHelper> {
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bio_samples);
        listSamples();
        
        final EditBiologicalSamplesActivity activity = this;
        findViewById(R.id.edit_bio_samples_cancel_btn)
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	activity.finish();
            }
        });  
        
        findViewById(R.id.edit_bio_samples_save_btn)
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	updateSampleNumbers();
            }
        });        
    }	
	
	
	public void listSamples() {

		List<BiologicalSample> sampleList = BiologicalSample.getAll(getHelper());
		
		ScrollView scrollView = (ScrollView)findViewById(R.id.edit_bio_samples_scrollview);
		scrollView.removeAllViews();
		
		TableLayout sampleTable = (TableLayout)getLayoutInflater().inflate(R.layout.template_table, null);
		((TextView)sampleTable.findViewById(R.id.template_table_first_column)).setText("Status");
		
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.WRAP_CONTENT,      
				TableLayout.LayoutParams.WRAP_CONTENT
		);
		
		// Get 40dp in px equivalent
		int topMarginPx = (int) TypedValue.applyDimension(
		        TypedValue.COMPLEX_UNIT_DIP,
		        20, 
		        this.getResources().getDisplayMetrics()
		);
		
		params.topMargin = topMarginPx;
		
		
		for (BiologicalSample sample : sampleList) {
			TableRow row = getTableRow(sample);
			row.setLayoutParams(params);
			sampleTable.addView(row);
		}
		
		scrollView.addView(sampleTable);
	}	
	
	public TableRow getTableRow(BiologicalSample sample) {
		
		TableRow tableRow = new TableRow(this.getBaseContext());
		
		TextView status = (TextView)getLayoutInflater().inflate(R.layout.template_column, null);
		status.setText(sample.getStatus().name());
		tableRow.addView(status);
		
		EditText editSampleNumber = (EditText)getLayoutInflater().inflate(R.layout.template_edittext, null);
		editSampleNumber.setText(String.valueOf(sample.getSampleNumber()));
		editSampleNumber.setTag(sample.getId());
		editSampleNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
		tableRow.addView(editSampleNumber);
				
		TextView featureName = (TextView)getLayoutInflater().inflate(R.layout.template_column_fixed_width, null);
		TextView collectionDate = (TextView)getLayoutInflater().inflate(R.layout.template_column, null);
		
		Survey survey = sample.getSurvey();
		if (survey != null) {
			getHelper().getSurveyDao().refresh(survey);
			collectionDate.setText(UiUtil.getDisplayDate(survey.getSurveyDate()));
			Feature feature = survey.getFeature();
			if (feature != null) {
				getHelper().getFeatureDao().refresh(feature);
				featureName.setText(feature.getFeatureName());
			}
		}

		tableRow.addView(featureName);
		tableRow.addView(collectionDate);
		
		return tableRow;
		
	}	
	
	@SuppressLint("UseSparseArrays")
	public void updateSampleNumbers() {
		
		final Map<Integer, Long> sampleNumberToIdMap = new HashMap<Integer, Long>();
		final Set<Integer> duplicateSampleNumbers = new HashSet<Integer>();
		List<View> editTextList = new LinkedList<View>();
		UiUtil.getChildren(findViewById(R.id.edit_bio_samples_scrollview), editTextList,  new ViewFilter() {	
			@Override
			public boolean matches(View view) {
				if (view instanceof EditText) {
					Integer sampleNumber = Integer.parseInt(((EditText)view).getText().toString());
					if (sampleNumberToIdMap.containsKey(sampleNumber)) {
						duplicateSampleNumbers.add(sampleNumber);
					} else {
						sampleNumberToIdMap.put(sampleNumber, (Long)view.getTag());
					}
					return true;
				}
				
				return false;
			}
		});
		
		// confirm all sample numbers are unique
		if (!duplicateSampleNumbers.isEmpty()) {
			String message = "Sample numbers must be unique, the following numbers are duplicated: "+Util.join(",", duplicateSampleNumbers);
			UiUtil.showMessageDialog(this, "Sample number update failed", message);
			return;
		}
		
		// Perform update on a transaction, so either all succeed or all fail
		try {
			final RuntimeExceptionDao<BiologicalSample, Long> sampleDao = getHelper().getBiologicalSampleDao();
			TransactionManager.callInTransaction(getHelper().getConnectionSource(), new Callable<Void>(){

				@Override
				public Void call() throws Exception {
					for (Map.Entry<Integer, Long> entry : sampleNumberToIdMap.entrySet()) {
						BiologicalSample sample = sampleDao.queryForId(entry.getValue());
						sample.setSampleNumber(entry.getKey());
						sampleDao.update(sample);
					}
					
					return null;
				}
				
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		Toast.makeText(SpringsApplication.getAppContext(), "Sample number update successful", Toast.LENGTH_LONG).show();
		
	}

}
