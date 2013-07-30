package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.MddbApplication;
import nz.cri.gns.springs.R;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.FeatureDb;
import nz.cri.gns.springs.db.SpringsDbHelper;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeatureIdentificationFragment extends DialogFragment {
	
	private DialogAction dialogAction;
	private Feature feature;
	private OnDismissListener dismissListener;
	
	public enum DialogAction {
		SAVE, CANCEL
	}
	
	public void setOnDismissListener(OnDismissListener listener) {
		dismissListener  = listener;
	}
		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.fragment_feature_id, container, false);
    	getDialog().setTitle("Enter the feature details");
    	
    	SpringsDbHelper helper = new SpringsDbHelper(this.getActivity().getBaseContext());
    	addSaveButtonListener(rootView, helper);
    	addCancelButtonListener(rootView);
    	
    	if (feature != null) {
    		setInputFromFeature(rootView, feature);
    	} 
    	
    	return rootView;
    }
    
    public void addSaveButtonListener(final View rootView, final SpringsDbHelper helper) {
    	
    	Button saveButton = (Button) rootView.findViewById(R.id.save_button);
    	final FeatureIdentificationFragment dialogFragment = this;
    	saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	
            	Feature currentFeature = dialogFragment.getFeature();
            	boolean newFeature = (currentFeature == null);
            	if (newFeature) {
            		currentFeature = new Feature();
            	}
            	dialogFragment.setFeatureFromInput(rootView, currentFeature);
            	FeatureDb featureDb = new FeatureDb(helper);
            	if (currentFeature.getFeatureName().isEmpty()) {
            		Toast.makeText(MddbApplication.getAppContext(), "Feature name is required", Toast.LENGTH_LONG).show();
            	} else if (newFeature && featureDb.getByName(currentFeature.getFeatureName()) != null) {
            		Toast.makeText(MddbApplication.getAppContext(), "Feature named "+currentFeature.getFeatureName()+" already exists", Toast.LENGTH_LONG).show();
            	} else {
            		if (newFeature) {
            			featureDb.create(currentFeature);
            		} else {
            			featureDb.update(currentFeature);
            		}
	            	setDialogAction(DialogAction.SAVE);
	            	setFeature(currentFeature);
	            	if (dismissListener != null) {
	            		dialogFragment.getDialog().setOnDismissListener(dismissListener);
	            	}
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
    	
    	String easting = ((EditText) rootView.findViewById(R.id.coord_easting_input)).getText().toString();
    	if (!easting.isEmpty()) {
    		currentFeature.setCoordNztmEast(Double.parseDouble(easting));
    	}
    	
    	String northing = ((EditText) rootView.findViewById(R.id.coord_northing_input)).getText().toString();
    	if (!northing.isEmpty()) {
    		currentFeature.setCoordNztmNorth(Double.parseDouble(northing));
    	}
    	
    	String errorEst = ((EditText) rootView.findViewById(R.id.coord_error_est)).getText().toString();
    	if (!errorEst.isEmpty()) {
    		currentFeature.setCoordErrorEst(Double.parseDouble(errorEst));
    	}
    	
    	EditText featureRel = (EditText) rootView.findViewById(R.id.coord_feature_rel);
    	currentFeature.setCoordFeatureRel(featureRel.getText().toString());
    	
    	return currentFeature;
    }
    
    public void setInputFromFeature(View rootView, Feature currentFeature) {
    	
    	EditText featureName = (EditText) rootView.findViewById(R.id.feature_name_input);
    	featureName.setText(currentFeature.getFeatureName());
    	
    	EditText historicName = (EditText) rootView.findViewById(R.id.historic_local_name_input);
    	historicName.setText(currentFeature.getHistoricName());
    	
    	EditText featureType = (EditText) rootView.findViewById(R.id.feature_type_input);
    	featureType.setText(currentFeature.getFeatureType());
    	
    	EditText geothermalField = (EditText) rootView.findViewById(R.id.geothermal_field_input);
    	geothermalField.setText(currentFeature.getGeothermalField());
    	
    	EditText description = (EditText) rootView.findViewById(R.id.description);
    	description.setText(currentFeature.getDescription());
    	
    	if (currentFeature.getCoordNztmEast() != null) {
    		EditText easting = (EditText) rootView.findViewById(R.id.coord_easting_input);
    		easting.setText(String.valueOf(currentFeature.getCoordNztmEast()));
    	}
    	
    	if (currentFeature.getCoordNztmNorth() != null) {
    		EditText northing = (EditText) rootView.findViewById(R.id.coord_northing_input);
    		northing.setText(String.valueOf(currentFeature.getCoordNztmNorth()));
    	}
    	
    	if (currentFeature.getCoordErrorEst() != null) {
    		EditText errorEst = (EditText) rootView.findViewById(R.id.coord_error_est);
    		errorEst.setText(String.valueOf(currentFeature.getCoordErrorEst()));
    	}
    	
    	EditText relToFeature = (EditText) rootView.findViewById(R.id.coord_feature_rel);
    	relToFeature.setText(currentFeature.getCoordFeatureRel());
    	
    }
    
    public void addCancelButtonListener(final View rootView) {
    	
    	Button cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
    	cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	setDialogAction(DialogAction.CANCEL);
            	dismiss();
            }
        });
    }

	public DialogAction getDialogAction() {
		return dialogAction;
	}

	public void setDialogAction(DialogAction dialogAction) {
		this.dialogAction = dialogAction;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	} 
    

}
