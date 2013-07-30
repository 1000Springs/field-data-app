package nz.cri.gns.springs.fragments;

import java.util.List;

import nz.cri.gns.springs.MddbApplication;
import nz.cri.gns.springs.R;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.FeatureDb;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.fragments.FeatureIdentificationFragment.DialogAction;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class BioSampleFragment extends Fragment {
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.fragment_bio_data, container, false);
    	
    	SpringsDbHelper helper = new SpringsDbHelper(this.getActivity().getBaseContext());
    	listFeatures(rootView, helper);
    	addButtonListener(rootView, helper);
    	return rootView;
    }
    
    public void listFeatures(View rootView, SpringsDbHelper helper) {
    	
    	// populate spinner with list of features from the DB
    	FeatureDb featureDb = new FeatureDb(helper);
    	Spinner featureSpinner = (Spinner) rootView.findViewById(R.id.feature_spinner);
    	List<Feature> featureList = featureDb.readAll();
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
    
    void showFeatureDialog(final View rootView, final SpringsDbHelper helper, Feature currentFeature) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        final FeatureIdentificationFragment featureDialog = new FeatureIdentificationFragment();
        featureDialog.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {				
				if (featureDialog.getDialogAction() == DialogAction.SAVE) {
					listFeatures(rootView, helper);
					setSelectedFeature(rootView, featureDialog.getFeature());
					Toast.makeText(MddbApplication.getAppContext(), "Feature saved", Toast.LENGTH_LONG).show();
				}
				
			}
        	
        });
        
        featureDialog.setFeature(currentFeature);
        featureDialog.show(ft, "dialog");
    }
        

}
