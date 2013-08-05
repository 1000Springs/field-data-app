package nz.cri.gns.springs.fragments;


import java.util.ArrayList;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.Util;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.fragments.FeatureIdFragment.DialogAction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AppearanceFragment extends BioSampleActivityFragment implements OnFocusChangeListener, TextWatcher {
	
	private View rootView;
	private boolean surveyUpdatedSinceLastSave = false;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_appearance, container, false);
    	  	  	   	
    	listFeatures(rootView, getHelper());
    	addButtonListener(rootView, getHelper());
    	
    	setInputFromSurvey();
    	
    	Util.addEditTextListener(this, this, rootView);
    	
        TextView dateView = (TextView) rootView.findViewById(R.id.survey_date);       
        // TODO: replace this with the Survey instance value
        Time now = new Time(Time.getCurrentTimezone());
        now.set(currentSurvey.getSurveyDate());
        dateView.setText(now.format("%c"));
        
        
    	
    	return rootView;
    }
    
    public void listFeatures(View rootView, SpringsDbHelper helper) {
    	
    	// populate spinner with list of features from the DB
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
    	List<Feature> featureList = Feature.getAll(getHelper());
    	ArrayAdapter<Feature> dataAdapter = new ArrayAdapter<Feature>(this.getActivity().getBaseContext(),
    				R.layout.widget_spinner, featureList);
        dataAdapter.setDropDownViewResource(R.layout.widget_spinner_item);
    	featureSpinner.setAdapter(dataAdapter);
    	
    	// Disable 'Edit' button if the number of features = 0
    	Button editButton = (Button) rootView.findViewById(R.id.edit_feature_button);
    	if (featureList.isEmpty()) {
        	featureSpinner.setVisibility(View.GONE);
        	editButton.setVisibility(View.GONE);
    	} else {
        	featureSpinner.setVisibility(View.VISIBLE);
        	editButton.setVisibility(View.VISIBLE);
    	}
    } 
    
    public void setSelectedFeature(View rootView, Feature feature) {
    	
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
    	SpinnerAdapter adapter = featureSpinner.getAdapter();
    	for (int i = 0; i < adapter.getCount(); i++) {
    		Feature item = (Feature)adapter.getItem(i);
    		if (item.getFeatureName().equals(feature.getFeatureName())) {
    			featureSpinner.setSelection(i);
    			return;
    		}
    	}
    	
    }
    
    
    public void addButtonListener(final View rootView, final SpringsDbHelper helper) {
    	
    	Button addButton = (Button) rootView.findViewById(R.id.add_feature_button);
    	addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	showFeatureDialog(rootView, helper, null);
            }
        });
    	
    	Button editButton = (Button) rootView.findViewById(R.id.edit_feature_button);
    	editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
            	Feature selectedFeature = (Feature) featureSpinner.getSelectedItem();
            	showFeatureDialog(rootView, helper, selectedFeature);
            }
        });
    } 
    
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (surveyUpdatedSinceLastSave) {
			updateSurveyFromInput();
		}
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		surveyUpdatedSinceLastSave = true;
	}
    
    void showFeatureDialog(final View rootView, final SpringsDbHelper helper, Feature currentFeature) {

        // Create and show the dialog.
        final FeatureIdFragment featureDialog = new FeatureIdFragment();
        featureDialog.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {				
				if (featureDialog.getDialogAction() == DialogAction.SAVE) {
					listFeatures(rootView, helper);
					setSelectedFeature(rootView, featureDialog.getFeature());
					Toast.makeText(SpringsApplication.getAppContext(), "Feature saved", Toast.LENGTH_LONG).show();
				}
				
			}
        	
        });
        
        featureDialog.setFeature(currentFeature);
        featureDialog.show(getFragmentManager(), "dialog");
    }
    
    public void updateSurveyFromInput() {
    	
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
        if (featureSpinner.getSelectedItem() != null) {
        	currentSurvey.setFeature((Feature)featureSpinner.getSelectedItem());
        }
    	
    	String size = ((EditText) rootView.findViewById(R.id.feature_size_input)).getText().toString();
    	if (!size.isEmpty()) {
    		currentSurvey.setSize(Double.parseDouble(size));
    	}
    	
    	currentSurvey.setColour(((EditText) rootView.findViewById(R.id.colour_input)).getText().toString());
    	currentSurvey.setClarityTurbidity(((EditText) rootView.findViewById(R.id.clarity_turbidity_input)).getText().toString());
    	
    	String temperature = ((EditText) rootView.findViewById(R.id.feature_temperature_input)).getText().toString();
    	if (!temperature.isEmpty()) {
    		currentSurvey.setTemperature(Double.parseDouble(size));
    	}
    	
    	currentSurvey.setObserver1(((EditText) rootView.findViewById(R.id.observer_1_input)).getText().toString());
    	currentSurvey.setObserver2(((EditText) rootView.findViewById(R.id.observer_2_input)).getText().toString());
    	
    	getHelper().getSurveyDao().update(currentSurvey);
    	surveyUpdatedSinceLastSave = false;
		Toast.makeText(SpringsApplication.getAppContext(), "Update saved", Toast.LENGTH_SHORT).show();
    }
    
    public void setInputFromSurvey() {
    	
    	if (currentSurvey.getFeature() != null) {
    		setSelectedFeature(rootView, currentSurvey.getFeature());		
    	}
    	
    	if (currentSurvey.getSize() != null) {
    		((EditText) rootView.findViewById(R.id.feature_size_input)).setText(String.valueOf(currentSurvey.getSize()));
    	}
    	((EditText) rootView.findViewById(R.id.colour_input)).setText(currentSurvey.getColour());
    	((EditText) rootView.findViewById(R.id.clarity_turbidity_input)).setText(currentSurvey.getClarityTurbidity());
    	if (currentSurvey.getTemperature() != null) {
    		((EditText) rootView.findViewById(R.id.feature_temperature_input)).setText(String.valueOf(currentSurvey.getTemperature()));
    	}
    	((EditText) rootView.findViewById(R.id.observer_1_input)).setText(currentSurvey.getObserver1());
    	((EditText) rootView.findViewById(R.id.observer_2_input)).setText(currentSurvey.getObserver2());
    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

}
