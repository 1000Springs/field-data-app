package nz.cri.gns.springs.fragments;


import java.util.List;

import nz.cri.gns.springs.GpsLocation;
import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.db.SurveyImage;
import nz.cri.gns.springs.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AppearanceFragment extends BioSampleActivityFragment implements OnFocusChangeListener, TextWatcher, OnItemSelectedListener {
	
	private View rootView;
	private boolean surveyUpdatedSinceLastSave = false;
	private GpsLocation gpsLocation;
	
	private static final int UPDATE_FEATURE = 0;
	private static final int SELECT_IMAGE = 1;
	private static final int SELECT_COLOUR = 2;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_appearance, container, false);
    	  	  	   	
    	listFeatures(rootView, getHelper());
    	addButtonListener(rootView, getHelper());
    	
        setClarityTurbidityOptions();  
    	
    	setInputFromSurvey();
    	
    	Util.addEditTextListener(this, this, rootView);
    	
        TextView dateView = (TextView) rootView.findViewById(R.id.survey_date);       
        Time now = new Time(Time.getCurrentTimezone());
       	now.set(currentSurvey.getSurveyDate());
        dateView.setText(now.format("%c"));
        
        gpsLocation = new GpsLocation(this.getActivity());
        
        setObserverOptions();      
        
    	return rootView;
    }

	public void setObserverOptions() {
		// Set options for lead observer name
        AutoCompleteTextView observerView = (AutoCompleteTextView) rootView.findViewById(R.id.observer_input);
        List<String> options = Survey.getObservers(getHelper());
        observerView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}

	public void setClarityTurbidityOptions() {
		// Set options for clarity/turbidity description
        Spinner spinner = (Spinner) rootView.findViewById(R.id.clarity_turbidity_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
             R.array.clarity_turbidity_array, R.layout.widget_spinner);
        adapter.setDropDownViewResource(R.layout.widget_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
	}
    
    public void listFeatures(View rootView, SpringsDbHelper helper) {
    	
    	// populate spinner with list of features from the DB
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
    	List<Feature> featureList = Feature.getAll(getHelper());
    	ArrayAdapter<Feature> dataAdapter = new ArrayAdapter<Feature>(this.getActivity().getBaseContext(),
    				R.layout.widget_spinner, featureList);
        dataAdapter.setDropDownViewResource(R.layout.widget_spinner_item);
    	featureSpinner.setAdapter(dataAdapter);
    	featureSpinner.setOnItemSelectedListener(this);
    	
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
    	
    	if (feature != null) {
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
    }
    
    public void setSelectedClarityTurbidity(View rootView, String selection) {
    	if (selection != null) {
    		Spinner clarityTurbiditySpinner = (Spinner) rootView.findViewById(R.id.clarity_turbidity_spinner);   
    		SpinnerAdapter adapter = clarityTurbiditySpinner.getAdapter();
        	for (int i = 0; i < adapter.getCount(); i++) {
        		if (selection.equals(adapter.getItem(i))) {
        			clarityTurbiditySpinner.setSelection(i);
        			return;
        		}
        	}     		
    	}
    }
    
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		updateSurveyFromInput();	
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		updateSurveyFromInput();	
	}
    
    
    public void addButtonListener(final View rootView, final SpringsDbHelper helper) {
    	
    	Button addButton = (Button) rootView.findViewById(R.id.add_feature_button);
    	addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	showFeatureDialog(helper, null);
            }
        });
    	
    	Button editButton = (Button) rootView.findViewById(R.id.edit_feature_button);
    	editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
            	Feature selectedFeature = (Feature) featureSpinner.getSelectedItem();
            	showFeatureDialog(helper, selectedFeature);
            }
        });
    	
    	Button saveButton = (Button) rootView.findViewById(R.id.save_survey_button);
    	saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	updateSurveyFromInput();
        		Toast.makeText(SpringsApplication.getAppContext(), "Update saved", Toast.LENGTH_LONG).show();           	
            }
        });
    	
    	Button chooseFromImageButton = (Button) rootView.findViewById(R.id.choose_colour_from_image_button);
    	chooseFromImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	showChooseImageDialog();
            }
        });
    	if (SurveyImage.getImageCount(currentSurvey, getHelper()) == 0) {
    		chooseFromImageButton.setVisibility(View.GONE);
    	}
    	
    	Button chooseColourButton = (Button) rootView.findViewById(R.id.choose_colour_from_colour_picker_button);
    	chooseColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	showChooseColourDialog(null);
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
    
    void showFeatureDialog(final SpringsDbHelper helper, Feature currentFeature) {

        // Create and show the dialog.
        FeatureIdFragment featureDialog = new FeatureIdFragment();
        featureDialog.setGpsLocation(gpsLocation);       
        featureDialog.setFeature(currentFeature);
        featureDialog.setTargetFragment(this, UPDATE_FEATURE);
        featureDialog.show(getFragmentManager(), "featureDialog");
    }
    
    void showChooseImageDialog() {

        // Create and show the dialog.
        ChooseImageFragment chooseImageDialog = new ChooseImageFragment();     
        chooseImageDialog.setCurrentSurvey(currentSurvey);
        chooseImageDialog.setTargetFragment(this, SELECT_IMAGE);
        chooseImageDialog.show(getFragmentManager(), "chooseImageDialog");  
    }
    
    void showChooseColourDialog(String imageFile) {
    	
    	ImageColourPickerFragment colourPickerDialog = new ImageColourPickerFragment();   	
    	colourPickerDialog.setImageFile(imageFile);
    	colourPickerDialog.setInitialColour(currentSurvey.getColour());
    	colourPickerDialog.setTargetFragment(this, SELECT_COLOUR);  	
    	colourPickerDialog.show(getFragmentManager(), "imageColourPickerDialog");
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		if (requestCode == UPDATE_FEATURE) {
    			Feature feature = (Feature)data.getSerializableExtra(FeatureIdFragment.FEATURE_KEY);
				listFeatures(rootView, getHelper());
				setSelectedFeature(rootView, feature);
				Toast.makeText(SpringsApplication.getAppContext(), "Feature saved", Toast.LENGTH_LONG).show();
				
    		} else if (requestCode == SELECT_IMAGE) {
    			String imageFile = data.getStringExtra(ChooseImageFragment.IMAGE_FILE_KEY);
    			showChooseColourDialog(imageFile);
    			
    		} else if (requestCode == SELECT_COLOUR) {
    			int colour = data.getIntExtra(ImageColourPickerFragment.COLOUR_KEY, 0xffffff);
    			setColour(colour);
    		}
    	}
    }
    
    private void setColour(int colour) {
		View colourInput = this.getView().findViewById(R.id.colour_input);
		colourInput.setBackgroundColor(colour);
		colourInput.setTag(colour);
		updateSurveyFromInput();    	
    }
    
    public void updateSurveyFromInput() {
    	
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
        if (featureSpinner.getSelectedItem() != null) {
        	currentSurvey.setFeature((Feature)featureSpinner.getSelectedItem());
        }
    	
        currentSurvey.setSize(((EditText) rootView.findViewById(R.id.feature_size_input)).getText().toString()); 
        
        Object colour = rootView.findViewById(R.id.colour_input).getTag();
        if (colour != null) {
        	currentSurvey.setColour((Integer)colour);
        }
        
        Spinner clarityTurbiditySpinner = (Spinner) rootView.findViewById(R.id.clarity_turbidity_spinner);
        if (clarityTurbiditySpinner.getSelectedItem() != null) {
        	currentSurvey.setClarityTurbidity((String)clarityTurbiditySpinner.getSelectedItem());
        }
    	
    	String temperature = ((EditText) rootView.findViewById(R.id.feature_temperature_input)).getText().toString();
    	if (!temperature.isEmpty()) {
    		currentSurvey.setTemperature(Double.parseDouble(temperature));
    	}
    	
    	currentSurvey.setObserver(((EditText) rootView.findViewById(R.id.observer_input)).getText().toString());
    	
    	getHelper().getSurveyDao().update(currentSurvey);
    	surveyUpdatedSinceLastSave = false;
    }
    
    public void setInputFromSurvey() {
    	
   		setSelectedFeature(rootView, currentSurvey.getFeature());		
    	
    	((EditText) rootView.findViewById(R.id.feature_size_input)).setText(currentSurvey.getSize());

    	if (currentSurvey.getColour() != null) {
        	View colourInput = rootView.findViewById(R.id.colour_input);
    		colourInput.setBackgroundColor(currentSurvey.getColour());
    		colourInput.setTag(currentSurvey.getColour());
    	}
    	
    	setSelectedClarityTurbidity(rootView, currentSurvey.getClarityTurbidity());
    	
    	if (currentSurvey.getTemperature() != null) {
    		((EditText) rootView.findViewById(R.id.feature_temperature_input)).setText(String.valueOf(currentSurvey.getTemperature()));
    	}
    	((EditText) rootView.findViewById(R.id.observer_input)).setText(currentSurvey.getObserver());
    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

}
