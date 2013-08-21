package nz.cri.gns.springs.fragments;

import static nz.cri.gns.springs.util.UiUtil.DOUBLE_TAP_DELAY_MILLIS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.SurveyImage;
import nz.cri.gns.springs.util.UiUtil;
import nz.cri.gns.springs.util.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aviary.android.feather.FeatherActivity;
import com.aviary.android.feather.library.Constants;

/**
 * Screen that allows the user to add photos and sketches for a biological sample.
 * Requires the 3rd party Aviary-SDK library, which can be downloaded from aviary.com (free, but requires sign-up).
 * The library must be included with this app, instructions for adding it to the project
 * are available on the Aviary site.
 * @author duncanw
 */
public class ImageFragment extends BioSampleActivityFragment implements OnDragListener, OnTouchListener, OnLongClickListener {
	
	public static final int IMAGE_THUMBNAIL_WIDTH = 200;
	public static final int IMAGE_THUMBNAIL_HEIGHT = 200;
	
	private static final int IMAGE_CAPTURE = 1;
	private static final int IMAGE_EDIT = 2;
	private static final int CREATE_SKETCH = 3;
	
	private static final String IMAGE_PARENT_TAG = "ImageParent";
	
	private static final String IMAGE_FILE_KEY = "currentImageFile";
	
	private String currentImageFile;
	private View rootView;
	
	private long lastImageTapMillis = 0;
	private View lastImageTapView = null;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_image, container, false);
    	
    	addCameraButtonListener();  	
    	addSketchButtonListener();
    	
    	addTagDragListener(rootView);
    	rootView.findViewById(R.id.image_types).setOnDragListener(this);
    	rootView.findViewById(R.id.rubbish_bin).setOnDragListener(this);
    	rootView.setOnDragListener(this);
    	
    	displayImages();
    	
    	return rootView;
    }

	public void addSketchButtonListener() {
		View sketchButton = rootView.findViewById(R.id.sketch_button);
    	final Activity activity = this.getActivity();
    	
    	sketchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				try {
					String destFile = createImageFileName();
					Util.copy(getResources(), R.raw.sketch_canvas, new File(destFile));
		            Intent editImageIntent = new Intent(activity, FeatherActivity.class);
		            editImageIntent.setData( Uri.parse(destFile));
		            editImageIntent.putExtra( "output",  Uri.parse(destFile));
		            editImageIntent.putExtra( "output-format", Bitmap.CompressFormat.JPEG.name() );
		            editImageIntent.putExtra( "output-quality", 85 );
		            editImageIntent.putExtra( "tools-list", new String[]{"DRAWING", "TEXT" } );
		            startActivityForResult( editImageIntent, IMAGE_EDIT );
		            
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

            }
        });
	}

	public void addCameraButtonListener() {
		View cameraButton = rootView.findViewById(R.id.camera_button);
    	cameraButton.setEnabled(SpringsApplication.isIntentAvailable(SpringsApplication.getAppContext(), MediaStore.ACTION_IMAGE_CAPTURE));   	
    	cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	try {
	                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	                currentImageFile = createImageFileName();
	                File currentFile = new File(currentImageFile);
	                currentFile.createNewFile();
	                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentFile));
	                startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
            	} catch (Exception e) {
            		String message = "An error occurred, unable to open camera";
            		new AlertDialog.Builder(rootView.getContext())
            			.setMessage(message)
            			.setPositiveButton("OK", null)
            			.show();
            		Log.e(this.getClass().getName(), message, e);
            	}
            }
        });
	}
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putString(IMAGE_FILE_KEY, currentImageFile);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		currentImageFile = savedInstanceState.getString(IMAGE_FILE_KEY);
    	}
    }
    
    public void copsySketch(File dst) throws IOException {
  
        InputStream in = getResources().openRawResource(R.raw.sketch_canvas);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }    
    
    
	@Override
	public boolean onLongClick(View view) {
		ClipData data = ClipData.newPlainText("", "");
		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
				view);
		view.startDrag(data, shadowBuilder, view, 0);
		view.setVisibility(View.INVISIBLE);
		return true;
	}    


    @Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
    	
		long tapTime = System.currentTimeMillis();
		boolean consumedEvent = false;
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {		
			if (view == lastImageTapView && tapTime - lastImageTapMillis <= DOUBLE_TAP_DELAY_MILLIS) {
				editImage(view);
				consumedEvent = true;
			} else {
				lastImageTapView = view;
				lastImageTapMillis = tapTime;
			}		
		} 
		
		return consumedEvent;
	}
    
    private void addTagDragListener(View rootView) {
    	
    	int[] ids = {R.id.best_photo_tag, R.id.best_sketch_tag, R.id.annotated_photo_tag};
    	for (int id : ids) {
    		View tag = rootView.findViewById(id);
    		if (tag != null) {
    			tag.setOnLongClickListener(this);
    		}
    	}
    }
    
	 String createImageFileName() throws IOException {
		File storageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"1000 Springs App");
		if (!storageDir.exists()) {
			Log.d(ImageFragment.class.getName(), "Creating image directory "+storageDir.getAbsolutePath());
			if (!storageDir.mkdirs()) {
				Log.e(ImageFragment.class.getName(), "Failed to create image directory "+storageDir.getAbsolutePath());
			}
		}
	
		String imageFileName = Util.getTimestampMillis();
		return storageDir.getAbsolutePath() + "/" + imageFileName + ".jpg";
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (resultCode == Activity.RESULT_OK) {
    		if (requestCode == IMAGE_CAPTURE) {
    			addImage(currentImageFile);
    			
    		} else if (requestCode == IMAGE_EDIT || requestCode == CREATE_SKETCH) {
                Bundle extra = data.getExtras();
                if( null != extra ) {
                    // image was changed by the user?
                    if (extra.getBoolean( Constants.EXTRA_OUT_BITMAP_CHANGED )) {
                    	addImage(data.getData().getPath());
                    }
                } else {   
	        		if (requestCode == CREATE_SKETCH) {
	        			new File(currentImageFile).delete();
	        		}
                }
    		}
    	} else {
    		if (requestCode == CREATE_SKETCH) {
    			new File(currentImageFile).delete();
    		}
    	}
    }
    
    private void addImage(String fileName) {
    	
		SurveyImage surveyImage = new SurveyImage();
		surveyImage.setFileName(fileName);
		surveyImage.setSurvey(currentSurvey);
		getHelper().getSurveyImageDao().create(surveyImage);
		displayImage((GridLayout)rootView.findViewById(R.id.photo_list), surveyImage);	
    }
       
    private void displayImages() {
    	
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
        
        imgView.setImageBitmap(UiUtil.loadImage(imageFile, IMAGE_THUMBNAIL_WIDTH, IMAGE_THUMBNAIL_HEIGHT));
        
        imgView.setAdjustViewBounds(true);
        imgView.setMaxWidth(IMAGE_THUMBNAIL_WIDTH);
        imgView.setMaxHeight(IMAGE_THUMBNAIL_HEIGHT);
        imgView.setId(surveyImage.getId().intValue());
        imgView.setOnTouchListener(this);
        imgView.setOnLongClickListener(this);
                
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setMargins(20, 20, 20, 20);
        
        RelativeLayout imageWrapper = new RelativeLayout(this.getActivity().getBaseContext());
        imageWrapper.setOnDragListener(this);
        imageWrapper.setTag(IMAGE_PARENT_TAG);
        imageWrapper.addView(imgView);       
        
        photoList.addView(imageWrapper, layoutParams);
        
        if (surveyImage.getImageType() != null) {
        	moveImageTypeToImage(rootView.findViewWithTag(surveyImage.getImageType()), imgView);	
        }
        
        imgView.setOnDragListener(this);    	
    }
    
	@Override
	public boolean onDrag(View target, DragEvent event) {
		int action = event.getAction();
		View source = (View) event.getLocalState();
		if (source instanceof ImageView && target.getId() == R.id.rubbish_bin) {
			imageDraggedToRubbishBin(source, target, action);	
			
		} else if (source instanceof TextView && target.getId() == R.id.image_types) {
			imageTypeDraggedToToolbar(source, target, action);
			
		} else if (source instanceof TextView && target instanceof ImageView && target.getId() != R.id.rubbish_bin) {
			imageTypeDraggedToImage(source, target, action);
			
		} else {
			
			if (action == DragEvent.ACTION_DROP || action == DragEvent.ACTION_DRAG_ENDED) {
				View tag = (View)event.getLocalState();
				tag.setVisibility(View.VISIBLE);
			}
			
		}
		
		return true;
	}
	
	private void imageTypeDraggedToImage(View source, View target, int action) {
		switch (action) {
		case DragEvent.ACTION_DRAG_ENTERED:
			if (target.getTag() == null) {
				target.setBackgroundResource(R.drawable.drop_target_background);
			}
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			target.setBackgroundResource(0);
			break;
			
		case DragEvent.ACTION_DROP:

			if (target.getTag() == null) {
				moveImageTypeToImage(source, target);				
				SurveyImage targetSurveyImage = getHelper().getSurveyImageDao().queryForId(Long.valueOf(target.getId()));
				targetSurveyImage.setImageType(source.getTag().toString());
				getHelper().getSurveyImageDao().update(targetSurveyImage);								
			}
			
			source.setVisibility(View.VISIBLE);
			break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			target.setBackgroundResource(0);
			break;
		}		
	}
	
	private void moveImageTypeToImage(View imageType, View image) {
		
		removeImageTypeFromPreviousParent(imageType);
		RelativeLayout imageWrapper = (RelativeLayout)image.getParent();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_TOP, image.getId());
		params.addRule(RelativeLayout.ALIGN_RIGHT, image.getId());	
		imageWrapper.addView(imageType, params);	
		image.setTag(getImageViewTag(imageType));
	}
	
	private void imageTypeDraggedToToolbar(View source, View target, int action) {
		switch (action) {
		case DragEvent.ACTION_DRAG_ENTERED:
			if (target.findViewById(source.getId()) == null) {
				target.setBackgroundResource(R.drawable.drop_target_border);
			}
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			target.setBackgroundResource(0);
			break;
			
		case DragEvent.ACTION_DROP:
			removeImageTypeFromPreviousParent(source);			
		    addImageTypeBackToToolbar(source);
			break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			target.setBackgroundResource(0);
		}			
	}
	
	private void imageDraggedToRubbishBin(View source, View target, int action) {
		switch (action) {
		case DragEvent.ACTION_DRAG_ENTERED:
			target.setBackgroundResource(R.drawable.drop_target_border);
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			target.setBackgroundResource(0);
			break;
			
		case DragEvent.ACTION_DROP:
			SurveyImage sourceSurveyImage = getHelper().getSurveyImageDao().queryForId(Long.valueOf(source.getId()));
			// delete image from file
			new File(sourceSurveyImage.getFileName()).delete();
			// delete surveyimage from db
			getHelper().getSurveyImageDao().delete(sourceSurveyImage);		
			
			// delete image from view
			RelativeLayout imageWrapper = (RelativeLayout)source.getParent();		
			imageWrapper.removeView(source);
			if (imageWrapper.getChildCount() > 0) {
				// Image was labelled, return label to home base
				View imageType = imageWrapper.getChildAt(0);
				imageWrapper.removeView(imageType);
			    addImageTypeBackToToolbar(imageType);				
			}
			((ViewGroup)imageWrapper.getParent()).removeView(imageWrapper);			
			break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			target.setBackgroundResource(0);
		}			
	}

	private void removeImageTypeFromPreviousParent(View source) {
		ViewGroup oldParent = (ViewGroup)source.getParent();
		if (IMAGE_PARENT_TAG.equals(oldParent.getTag())) {
			// remove the tag so other tags can be dropped on the image
			ImageView previousImage = (ImageView)oldParent.findViewWithTag(getImageViewTag(source));
			previousImage.setTag(null);
			SurveyImage previousSurveyImage = getHelper().getSurveyImageDao().queryForId(Long.valueOf(previousImage.getId()));
			previousSurveyImage.setImageType(null);
			getHelper().getSurveyImageDao().update(previousSurveyImage);
			
		}
		oldParent.removeView(source);
	}
	
	public void addImageTypeBackToToolbar(View imageType) {
		ViewGroup imageTypes = (ViewGroup)rootView.findViewById(R.id.image_types);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
				);
		params.setMargins(10, 10, 10, 10);
		imageType.setLayoutParams(params);
		imageTypes.addView(imageType);
		imageType.setVisibility(View.VISIBLE);	
	}
	
	private String getImageViewTag(View source) {
		return source.getTag()+"_IMAGE";
	}
	    
    private void editImage(View imageView) {
    	try {
	    	SurveyImage surveyImage = getHelper().getSurveyImageDao().queryForId(Long.valueOf(imageView.getId()));
	    	String fileName = surveyImage.getFileName();
	        Intent editImageIntent = new Intent(this.getActivity(), FeatherActivity.class);
	        editImageIntent.setData( Uri.parse(fileName) );
	        editImageIntent.putExtra( "output", Uri.parse(createImageFileName()));
	        editImageIntent.putExtra( "output-format", Bitmap.CompressFormat.JPEG.name() );
	        editImageIntent.putExtra( "output-quality", 85 );
	        editImageIntent.putExtra( "tools-list", new String[]{"DRAWING", "TEXT" } );
	        startActivityForResult( editImageIntent, IMAGE_EDIT );    	
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
    }
}
