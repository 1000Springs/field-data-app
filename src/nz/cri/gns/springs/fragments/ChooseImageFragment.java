package nz.cri.gns.springs.fragments;

import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.db.SurveyImage;
import nz.cri.gns.springs.util.UiUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class ChooseImageFragment extends SpringsDialogFragment implements OnTouchListener {

	private Survey currentSurvey;
	
	private static final String SURVEY_KEY = "currentSurvey";
	
	public static final String IMAGE_FILE_KEY = "imageFile";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.shared_image_grid, container, false);
    	getDialog().setTitle(R.string.choose_image_dialog_title);
    	
    	displayImages(rootView);
    	
    	return rootView;
    }
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putSerializable(SURVEY_KEY, currentSurvey);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		currentSurvey = (Survey)savedInstanceState.getSerializable(SURVEY_KEY);
    		getHelper().getSurveyDao().refresh(currentSurvey);
    	}
    }    
    
    private void displayImages(View rootView) {
    	
    	List<SurveyImage> images = SurveyImage.getBySurvey(currentSurvey, getHelper());
    	GridLayout photoList = (GridLayout)rootView.findViewById(R.id.photo_list);
    	photoList.removeAllViews();
    	
    	for (SurveyImage surveyImage : images) {
    		displayImage(photoList, surveyImage);
    	}
    }  
    
    private void displayImage(GridLayout photoList, SurveyImage surveyImage) {
    	
		String imageFile = surveyImage.getFileName();
        ImageView imgView = new ImageView(SpringsApplication.getAppContext());
        imgView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        options.inSampleSize = UiUtil.calculateInSampleSize(options, 200, 200);
        options.inJustDecodeBounds = false;
        imgView.setImageBitmap(BitmapFactory.decodeFile(imageFile, options));
        
        imgView.setAdjustViewBounds(true);
        imgView.setMaxHeight(200);
        imgView.setMaxWidth(200);
        imgView.setId(surveyImage.getId().intValue());
        imgView.setOnTouchListener(this);

                
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setMargins(20, 20, 20, 20);
        
        RelativeLayout imageWrapper = new RelativeLayout(this.getActivity().getBaseContext());
        imageWrapper.addView(imgView);       
        
        photoList.addView(imageWrapper, layoutParams);
 	
    }    


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		
		boolean consumedEvent = false;
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {		
			imageSelected(view);
			consumedEvent = true;
		} 
		
		return consumedEvent;
	} 
	
	private void imageSelected(View imageView) {
		
    	String imageFile = getHelper().getSurveyImageDao().queryForId(Long.valueOf(imageView.getId())).getFileName();
    	dismiss();
    	getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent().putExtra(IMAGE_FILE_KEY, imageFile));
	}
	
	public void setCurrentSurvey(Survey currentSurvey) {
		this.currentSurvey = currentSurvey;
	}
}
