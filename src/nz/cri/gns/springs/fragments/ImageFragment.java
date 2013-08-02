package nz.cri.gns.springs.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.db.SurveyImage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class ImageFragment extends SpringsFragment implements OnDragListener, OnTouchListener {
	
	private static final int IMAGE_CAPTURE = 1;
	private static final int IMAGE_EDIT = 2;
	private static final int CREATE_SKETCH = 3;
	
	private static final String IMAGE_PARENT_TAG = "ImageParent";
	
	private String currentImageFile;
	private Survey currentSurvey;
	private View rootView;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_image, container, false);
    	
    	View cameraButton = rootView.findViewById(R.id.camera_button);
    	cameraButton.setEnabled(SpringsApplication.isIntentAvailable(SpringsApplication.getAppContext(), MediaStore.ACTION_IMAGE_CAPTURE));   	
    	cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	try {
	                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	                File file = createImageFile();
	                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
	                currentImageFile = file.getAbsolutePath();
	                startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
            	} catch (Exception e) {
            		String message = "An error occurred, unable to open camera";
            		new AlertDialog.Builder(container.getContext())
            			.setMessage(message)
            			.setPositiveButton("OK", null)
            			.show();
            		Log.e(this.getClass().getName(), message, e);
            	}
            }
        });
    	
    	View sketchButton = rootView.findViewById(R.id.sketch_button);
    	final Activity activity = this.getActivity();
    	
    	sketchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				try {
					File dest = createImageFile();
					copySketch(dest);

		            Intent editImageIntent = new Intent(activity, FeatherActivity.class);
		            editImageIntent.setData( Uri.parse(dest.getAbsolutePath()));
		            editImageIntent.putExtra( "output",  Uri.parse(dest.getAbsolutePath()));
		            editImageIntent.putExtra( "output-format", Bitmap.CompressFormat.JPEG.name() );
		            editImageIntent.putExtra( "output-quality", 85 );
		            editImageIntent.putExtra( "tools-list", new String[]{"DRAWING", "TEXT" } );
		            startActivityForResult( editImageIntent, IMAGE_EDIT );
		            
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("badness", "badness", e);
				}

            }
        });
    	
    	addTagDragListener(rootView);
    	rootView.findViewById(R.id.image_types).setOnDragListener(this);
    	rootView.findViewById(R.id.rubbish_bin).setOnDragListener(this);
    	rootView.setOnDragListener(this);
    	
    	setSurvey();
    	displayImages();
    	
    	return rootView;
    }
    
    public void copySketch(File dst) throws IOException {
  
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
    
    private void setSurvey() {
    	// TODO: this is just a temporary hack, need to replace with way to 
    	// set the currently-being-entered sample/survey
		RuntimeExceptionDao<Survey, Long> dao = getHelper().getSurveyDao();
		CloseableIterator<Survey> iterator = dao.closeableIterator();
		try {
		    while (iterator.hasNext()) {
		    	currentSurvey = iterator.next();
		    	break;
		    }
		} finally {
		    try {
				iterator.close();
				if (currentSurvey == null) {
					currentSurvey = new Survey();
					dao.create(currentSurvey);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

    @Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
    	
		//TextView tag = (TextView) view;
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			ClipData data = ClipData.newPlainText("", "");
			DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
					view);
			view.startDrag(data, shadowBuilder, view, 0);
			view.setVisibility(View.INVISIBLE);
			return true;
		} else {
			return false;
		}
	}
    
    private void addTagDragListener(View rootView) {
    	
    	int[] ids = {R.id.best_photo_tag, R.id.best_sketch_tag, R.id.annotated_photo_tag};
    	for (int id : ids) {
    		View tag = rootView.findViewById(id);
    		if (tag != null) {
    			tag.setOnTouchListener(this);
    		}
    	}
    }
    
	static File createImageFile() throws IOException {
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

		// TODO: add feature identifier to file name or directory
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date());
		String imageFileName = timeStamp + "_";
		File image = File.createTempFile(imageFileName, "jpg", storageDir);
		return image;
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
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        options.inSampleSize = calculateInSampleSize(options, 200, 200);
        options.inJustDecodeBounds = false;
        imgView.setImageBitmap(BitmapFactory.decodeFile(imageFile, options));
        
        imgView.setAdjustViewBounds(true);
        imgView.setMaxHeight(200);
        imgView.setMaxWidth(200);
        imgView.setId(surveyImage.getId().intValue());
        imgView.setOnTouchListener(this);
        
        addEditImageListener(imgView);
                
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setMargins(20, 20, 20, 20);
        
        RelativeLayout imageWrapper = new RelativeLayout(this.getActivity().getBaseContext());
        imageWrapper.setOnDragListener(this);
        imageWrapper.setTag(IMAGE_PARENT_TAG);
        imageWrapper.addView(imgView);       
        
        photoList.addView(imageWrapper, layoutParams);
        
        if (surveyImage.getImageType() != null) {
        	moveImageTypeToImage(rootView.findViewWithTag(surveyImage.getImageType().name()), imgView);	
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
				targetSurveyImage.setImageType(SurveyImage.ImageType.valueOf(source.getTag().toString()));
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
			
		    ViewGroup imageTypes = (ViewGroup)rootView.findViewById(R.id.image_types);
		    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    		new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		    		);
		    params.setMargins(10, 10, 10, 10);
		    source.setLayoutParams(params);
			imageTypes.addView(source);
						
			source.setVisibility(View.VISIBLE);			
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
	
	private String getImageViewTag(View source) {
		return source.getTag()+"_IMAGE";
	}
	
    
    private void addEditImageListener(ImageView image) {
    	final Activity activity = this.getActivity();
    	final SpringsDbHelper dbHelper = getHelper();
    	image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	SurveyImage surveyImage = dbHelper.getSurveyImageDao().queryForId(Long.valueOf(view.getId()));
            	String fileName = surveyImage.getFileName();
	            Intent editImageIntent = new Intent(activity, FeatherActivity.class);
	            editImageIntent.setData( Uri.parse(fileName) );
	            editImageIntent.putExtra( "output", Uri.parse(fileName+"-edited"));
	            editImageIntent.putExtra( "output-format", Bitmap.CompressFormat.JPEG.name() );
	            editImageIntent.putExtra( "output-quality", 85 );
	            editImageIntent.putExtra( "tools-list", new String[]{"DRAWING", "TEXT" } );
	            startActivityForResult( editImageIntent, IMAGE_EDIT );
            }
        });
    }
    
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}
