package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.GpsLocation;
import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.PersistentObject.Status;
import nz.cri.gns.springs.db.SpringsDbHelper;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Dialog box which allows the user to create or edit a Feature object
 * encapsulating information about a geothermal feature.
 * @author duncanw
 */
public class FeatureIdFragment extends SpringsDialogFragment {
	
	private Feature feature;
	private transient GpsLocation gpsLocation;
	
	/**
	 * Key for the intent extra-data containing the Feature object created or updated by the user.
	 * (returned to the onActivityResult method of the activity that opened the dialog box).
	 */
	public static final String FEATURE_KEY = "featureKey";
		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.dialog_edit_feature, container, false);
    	getDialog().setTitle(R.string.feature_dialog_title);
    	
    	addSaveButtonListener(rootView);
    	addCancelButtonListener(rootView);
    	addLocationButtonListener(rootView);
    	
    	if (feature != null) {
    		setInputFromFeature(rootView, feature);
    	} 
    	
    	return rootView;
    }
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putSerializable(FEATURE_KEY, feature);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		feature = (Feature)savedInstanceState.getSerializable(FEATURE_KEY);
    	}
    }
    
    public void addSaveButtonListener(final View rootView) {
    	
    	Button saveButton = (Button) rootView.findViewById(R.id.save_button);
    	final FeatureIdFragment dialogFragment = this;
    	final SpringsDbHelper dbHelper = getHelper();
    	saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	
            	Feature currentFeature = dialogFragment.getFeature();
            	boolean newFeature = (currentFeature == null);
            	if (newFeature) {
            		currentFeature = new Feature();
            	}
            	dialogFragment.setFeatureFromInput(rootView, currentFeature);
            	if (currentFeature.getFeatureName().isEmpty()) {
            		Toast.makeText(
            				SpringsApplication.getAppContext(), 
            				"Feature name is required", Toast.LENGTH_LONG
            				).show();
            		
            	} else if (newFeature && Feature.getByName(currentFeature.getFeatureName(), dbHelper) != null) {
            		Toast.makeText(
            				SpringsApplication.getAppContext(), 
            				"Feature named "+currentFeature.getFeatureName()+" already exists", 
            				Toast.LENGTH_LONG
            				).show();
            		
            	} else {
            		if (newFeature) {
            			dbHelper.getFeatureDao().create(currentFeature);
            		} else {
            			dbHelper.getFeatureDao().update(currentFeature);
            		}
	            	setFeature(currentFeature);
	            	getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent().putExtra(FEATURE_KEY, currentFeature));
	            	dismiss();
            	}
            }
        });
    } 
    
    public Feature setFeatureFromInput(View rootView, Feature currentFeature) {
    	 
    	EditText featureName = (EditText) rootView.findViewById(R.id.feature_name_input);
    	currentFeature.setFeatureName(featureName.getText().toString());
    	
    	EditText historicName = (EditText) rootView.findViewById(R.id.historic_local_name_input);
    	currentFeature.setHistoricName(historicName.getText().toString());
    	
    	EditText featureType = (EditText) rootView.findViewById(R.id.feature_type_input);
    	currentFeature.setFeatureType(featureType.getText().toString());
    	
    	EditText geothermalField = (EditText) rootView.findViewById(R.id.geothermal_field_input);
    	currentFeature.setGeothermalField(geothermalField.getText().toString());
    	
    	EditText description = (EditText) rootView.findViewById(R.id.description);
    	currentFeature.setDescription(description.getText().toString());
    	
    	String easting = ((EditText) rootView.findViewById(R.id.coord_latitude)).getText().toString();
    	if (!easting.isEmpty()) {
    		currentFeature.setCoordLatitude(Double.parseDouble(easting));
    	}
    	
    	String northing = ((EditText) rootView.findViewById(R.id.coord_longitude)).getText().toString();
    	if (!northing.isEmpty()) {
    		currentFeature.setCoordLongitude(Double.parseDouble(northing));
    	}
    	
    	String errorEst = ((EditText) rootView.findViewById(R.id.coord_error_est)).getText().toString();
    	if (!errorEst.isEmpty()) {
    		currentFeature.setCoordErrorEst(Float.parseFloat(errorEst));
    	}
    	
    	EditText featureRel = (EditText) rootView.findViewById(R.id.coord_feature_rel);
    	currentFeature.setCoordFeatureRel(featureRel.getText().toString());
    	
    	return currentFeature;
    }
    
    public void setInputFromFeature(View rootView, Feature currentFeature) {
    	
    	EditText featureName = (EditText) rootView.findViewById(R.id.feature_name_input);
    	featureName.setText(currentFeature.getFeatureName());
    	// Can't let the user change the name of the feature after it's been exported
    	// to the main DB, since it's used as a reference when exporting.
    	if (currentFeature.getStatus() != Status.NEW) {
    		featureName.setEnabled(false);
    	}
    	
    	EditText historicName = (EditText) rootView.findViewById(R.id.historic_local_name_input);
    	historicName.setText(currentFeature.getHistoricName());
    	
    	EditText featureType = (EditText) rootView.findViewById(R.id.feature_type_input);
    	featureType.setText(currentFeature.getFeatureType());
    	
    	EditText geothermalField = (EditText) rootView.findViewById(R.id.geothermal_field_input);
    	geothermalField.setText(currentFeature.getGeothermalField());
    	
    	EditText description = (EditText) rootView.findViewById(R.id.description);
    	description.setText(currentFeature.getDescription());
    	
    	setLocationFields(rootView, currentFeature.getCoordLatitude(), currentFeature.getCoordLongitude(), currentFeature.getCoordErrorEst());
    	
    	EditText relToFeature = (EditText) rootView.findViewById(R.id.coord_feature_rel);
    	relToFeature.setText(currentFeature.getCoordFeatureRel());
    	
    }
    
    private void setLocationFields(View rootView, Double east, Double north, Float accuracy) {
    	
    	if (east != null) {
    		EditText easting = (EditText) rootView.findViewById(R.id.coord_latitude);
    		easting.setText(String.valueOf(east));
    	}
    	
    	if (north != null) {
    		EditText northing = (EditText) rootView.findViewById(R.id.coord_longitude);
    		northing.setText(String.valueOf(north));
    	}
    	
    	if (accuracy != null) {
    		EditText errorEst = (EditText) rootView.findViewById(R.id.coord_error_est);
    		errorEst.setText(String.valueOf(accuracy));
    	}    	
    }
    
    public void addCancelButtonListener(final View rootView) {
    	
    	Button cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
    	cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	dismiss();
            }
        });
    }
    
    
    public void addLocationButtonListener(final View rootView) {
    	
    	Button locationButton = (Button) rootView.findViewById(R.id.use_current_location_button);
    	final GpsLocation gpsLocation = getGpsLocation();
    	locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Location current = gpsLocation.getLastLocation();
            	if (current != null) {
            		setLocationFields(rootView, current.getLatitude(), current.getLongitude(), current.getAccuracy());            		
            	} else {
            		Toast.makeText(SpringsApplication.getAppContext(), "Location unavailable", Toast.LENGTH_LONG).show();
            	}
            }
        });
    }

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public void setGpsLocation(GpsLocation gpsLocation) {
		this.gpsLocation = gpsLocation;
	} 
	
	private GpsLocation getGpsLocation() {
		if (this.gpsLocation == null) {
			this.gpsLocation = new GpsLocation(this.getActivity());
		}
		
		return gpsLocation;
	}
    

}
