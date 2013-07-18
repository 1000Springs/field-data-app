package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FeatureIdentificationFragment extends Fragment {
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.fragment_feature_id, container, false);
    	
    	return rootView;
    }

}
