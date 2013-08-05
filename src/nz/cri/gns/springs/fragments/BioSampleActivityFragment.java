package nz.cri.gns.springs.fragments;

import java.util.ArrayList;

import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import nz.cri.gns.springs.Util;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.Survey;

public class BioSampleActivityFragment extends SpringsFragment {

	protected BiologicalSample currentSample;
	protected Survey currentSurvey;

	public BiologicalSample getCurrentSample() {
		return currentSample;
	}

	public BioSampleActivityFragment setCurrentSample(BiologicalSample currentSample) {
		this.currentSample = currentSample;
		setCurrentSurvey();
		return this;
	}
	
    protected void setCurrentSurvey() {
    	
    	BiologicalSample sample = getCurrentSample();
    	currentSurvey = sample.getSurvey();
    	if (currentSurvey == null) {
    		Survey survey = new Survey();
    		survey.setSurveyDate(System.currentTimeMillis());
    		getHelper().getSurveyDao().create(survey);
    		sample.setSurvey(survey);
    		getHelper().getBiologicalSampleDao().update(sample);
    		currentSurvey = survey;
    	}
    }
    
}
