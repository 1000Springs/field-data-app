package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.Survey;
import android.os.Bundle;

public class BioSampleActivityFragment extends SpringsFragment {

	protected BiologicalSample currentSample;
	protected Survey currentSurvey;
	
	public static final String SAMPLE_KEY = "currentSample";

	public BiologicalSample getCurrentSample() {
		return currentSample;
	}

	public BioSampleActivityFragment setCurrentSample(BiologicalSample currentSample) {
		this.currentSample = currentSample;
		setCurrentSurvey();
		return this;
	}
	
    protected void setCurrentSurvey() {
    	
    	currentSurvey = currentSample.getSurvey();
    	if (currentSurvey == null) {
    		Survey survey = new Survey();
    		survey.setSurveyDate(System.currentTimeMillis());
    		getHelper().getSurveyDao().create(survey);
    		currentSample.setSurvey(survey);
    		getHelper().getBiologicalSampleDao().update(currentSample);
    		currentSurvey = survey;
    	} else {
    		getHelper().getSurveyDao().refresh(currentSurvey);
    		getHelper().getFeatureDao().refresh(currentSurvey.getFeature());
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putSerializable(SAMPLE_KEY, currentSample);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		currentSample = (BiologicalSample)savedInstanceState.getSerializable(SAMPLE_KEY);
    		getHelper().getBiologicalSampleDao().refresh(currentSample);
    		setCurrentSurvey();
    	}
    }
    
}
