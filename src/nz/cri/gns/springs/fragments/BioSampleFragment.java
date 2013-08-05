package nz.cri.gns.springs.fragments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.Util;
import nz.cri.gns.springs.db.ChecklistItem;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class BioSampleFragment extends BioSampleActivityFragment implements OnFocusChangeListener, TextWatcher,  CompoundButton.OnCheckedChangeListener {
	
	private View rootView;
	private boolean sampleUpdatedSinceLastSave = false;
	
	private Map<String, ChecklistItem> checklistItemMap;
	
	public static final String BIO_CHECKLIST_NAME = "bio-sample";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_bio_data, container, false);
    	
    	setInputFromSample();    	
    	Util.addEditTextListener(this, this, rootView);
    	
    	setChecklistState();
    	Util.addCheckBoxListener(this, rootView);
    	
    	return rootView;
    }
    
    
    
    
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (sampleUpdatedSinceLastSave) {
			updateSampleFromInput();
		}
	}
	
    public void updateSampleFromInput() {
    	   	
    	String temperature = ((EditText) rootView.findViewById(R.id.sample_temperature_input)).getText().toString();
    	if (!temperature.isEmpty()) {
    		currentSample.setTemperature(Double.parseDouble(temperature));
    	}
    	
    	String pH = ((EditText) rootView.findViewById(R.id.ph_input)).getText().toString();
    	if (!pH.isEmpty()) {
    		currentSample.setPh(Double.parseDouble(pH));
    	}
    	
    	String orp = ((EditText) rootView.findViewById(R.id.orp_input)).getText().toString();
    	if (!orp.isEmpty()) {
    		currentSample.setOrp(Double.parseDouble(orp));
    	}
    	
    	String conductivity = ((EditText) rootView.findViewById(R.id.conductivity_input)).getText().toString();
    	if (!conductivity.isEmpty()) {
    		currentSample.setConductivity(Double.parseDouble(conductivity));
    	}
    	
    	String dO = ((EditText) rootView.findViewById(R.id.do_input)).getText().toString();
    	if (!dO.isEmpty()) {
    		currentSample.setDo(Double.parseDouble(dO));
    	}
    	
    	String turbidity = ((EditText) rootView.findViewById(R.id.turbidity_input)).getText().toString();
    	if (!turbidity.isEmpty()) {
    		currentSample.setTurbidity(Double.parseDouble(turbidity));
    	}
    	
    	String dna = ((EditText) rootView.findViewById(R.id.dna_input)).getText().toString();
    	if (!dna.isEmpty()) {
    		currentSample.setDnaVolume(Double.parseDouble(dna));
    	}
    	
    	String ferrousIron = ((EditText) rootView.findViewById(R.id.ferrous_iron_input)).getText().toString();
    	if (!ferrousIron.isEmpty()) {
    		currentSample.setFerrousIronAbs(Double.parseDouble(ferrousIron));
    	}

    	
    	getHelper().getBiologicalSampleDao().update(currentSample);
    	sampleUpdatedSinceLastSave = false;
		Toast.makeText(SpringsApplication.getAppContext(), "Update saved", Toast.LENGTH_SHORT).show();
    }
    
    
    
    public void setInputFromSample() {
    	
    	if (currentSample.getTemperature()!= null) {
    		((EditText) rootView.findViewById(R.id.sample_temperature_input)).setText(String.valueOf(currentSample.getTemperature()));
    	}
    	
    	if (currentSample.getPh()!= null) {
    		((EditText) rootView.findViewById(R.id.ph_input)).setText(String.valueOf(currentSample.getPh()));
    	}
    	
    	if (currentSample.getOrp()!= null) {
    		((EditText) rootView.findViewById(R.id.orp_input)).setText(String.valueOf(currentSample.getOrp()));
    	}
    	
    	if (currentSample.getConductivity()!= null) {
    		((EditText) rootView.findViewById(R.id.conductivity_input)).setText(String.valueOf(currentSample.getConductivity()));
    	}
    	
    	if (currentSample.getDo()!= null) {
    		((EditText) rootView.findViewById(R.id.do_input)).setText(String.valueOf(currentSample.getDo()));
    	}
    	
    	if (currentSample.getTurbidity()!= null) {
    		((EditText) rootView.findViewById(R.id.turbidity_input)).setText(String.valueOf(currentSample.getTurbidity()));
    	}
    	
    	if (currentSample.getDnaVolume() != null) {
    		((EditText) rootView.findViewById(R.id.dna_input)).setText(String.valueOf(currentSample.getDnaVolume()));
    	}
    	
    	if (currentSample.getFerrousIronAbs() != null) {
    		((EditText) rootView.findViewById(R.id.ferrous_iron_input)).setText(String.valueOf(currentSample.getFerrousIronAbs()));
    	}

    }
    
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		String itemName = buttonView.getTag().toString();
		ChecklistItem item = checklistItemMap.get(itemName);
		if (item == null) {
			item = new ChecklistItem();
			item.setChecklistName(BIO_CHECKLIST_NAME);
			item.setItemName(itemName);
			item.setObjectId(currentSample.getId());
			item.setItemValue(isChecked);
			getHelper().getChecklistItemDao().create(item);
			checklistItemMap.put(itemName, item);
			
		} else {
			item.setItemValue(isChecked);
			getHelper().getChecklistItemDao().update(item);
		}
	}
    
    public void setChecklistState() {
    	
    	checklistItemMap = new HashMap<String, ChecklistItem>();
    	List<ChecklistItem> checklistItems =  ChecklistItem.getBy(BIO_CHECKLIST_NAME, currentSample.getId(), getHelper());
    	for (ChecklistItem item : checklistItems) {
    		View view = rootView.findViewWithTag(item.getItemName());
    		if (view != null) {
    			checklistItemMap.put(item.getItemName(), item);
    			((CheckBox)view).setChecked(item.getItemValue());
    		}
    	}
    }
	
	@Override
	public void afterTextChanged(Editable s) {
		sampleUpdatedSinceLastSave = true;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

            

}
