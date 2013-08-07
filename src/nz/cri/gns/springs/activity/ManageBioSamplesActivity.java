package nz.cri.gns.springs.activity;

import java.util.ArrayList;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.Util;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.BiologicalSample.SamplesForExportResult;
import nz.cri.gns.springs.db.SpringsDbHelper;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class ManageBioSamplesActivity extends OrmLiteBaseActivity<SpringsDbHelper> implements OnClickListener, CompoundButton.OnCheckedChangeListener{

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
	
	
	public void listSamples() {
		
		List<SamplesForExportResult> sampleList = BiologicalSample.getSamplesForExport(getHelper());
		
		ScrollView scrollView = (ScrollView)findViewById(R.id.table_scrollview);
		scrollView.removeAllViews();
		
		TableLayout sampleTable = (TableLayout)getLayoutInflater().inflate(R.layout.template_table, null);
		for (SamplesForExportResult sample : sampleList) {
			sampleTable.addView(getTableRow(sample));
		}
		
		scrollView.addView(sampleTable);
	}
	
	public TableRow getTableRow(SamplesForExportResult sample) {
		
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
