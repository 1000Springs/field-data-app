package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.activity.BioSampleActivity;

import nz.cri.gns.springs.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppearanceFragment extends Fragment {
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.fragment_appearance, container, false);
    	
        rootView.findViewById(R.id.open_bio_sample_button)
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BioSampleActivity.class);
                startActivity(intent);
            }
        });
    	
    	return rootView;
    }

}
