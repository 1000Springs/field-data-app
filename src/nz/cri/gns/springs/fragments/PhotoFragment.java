package nz.cri.gns.springs.fragments;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import nz.cri.gns.springs.MddbApplication;
import nz.cri.gns.springs.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.aviary.android.feather.FeatherActivity;
import com.aviary.android.feather.library.Constants;

public class PhotoFragment extends Fragment {
	
	private static final int IMAGE_CAPTURE = 1;
	private static final int IMAGE_EDIT = 2;
	
	private View rootView;
	private LinkedList<String> images = new LinkedList<String>();
	private String currentImageFile;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(R.layout.fragment_photo, container, false);
    	
    	View addPhotoButton = rootView.findViewById(R.id.add_photo_button);
    	addPhotoButton.setEnabled(MddbApplication.isIntentAvailable(MddbApplication.getAppContext(), MediaStore.ACTION_IMAGE_CAPTURE));   	
    	addPhotoButton.setOnClickListener(new View.OnClickListener() {
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
    	
    	layoutImages();
    	
    	return rootView;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("images", images);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        try {
        	
	        if (savedInstanceState != null) {
	            // Restore last state for checked position.
	            this.images = (LinkedList<String>)savedInstanceState.getSerializable("images");
	            if (images == null) {
	            	images = new LinkedList<String>();
	            }
	            layoutImages();
	        }
        } catch (Exception e) {
    		Log.e(this.getClass().getName(), "Error restoring images", e);
        }
    }
    
	public File createImageFile() throws IOException {
		File storageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"1000 Springs App");
		if (!storageDir.exists()) {
			Log.d(this.getClass().getName(), "Creating image directory "+storageDir.getAbsolutePath());
			if (!storageDir.mkdirs()) {
				Log.e(this.getClass().getName(), "Failed to create image directory "+storageDir.getAbsolutePath());
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
    			images.add(currentImageFile);
    			layoutImages();
    			
    		} else if (requestCode == IMAGE_EDIT) {
                Bundle extra = data.getExtras();
                if( null != extra ) {
                    // image was changed by the user?
                    if (extra.getBoolean( Constants.EXTRA_OUT_BITMAP_CHANGED )) {
                        Uri editedImageFile = data.getData();
            			images.add(editedImageFile.getPath());
            			layoutImages();
                    }
                }    			
    		}
    	}
    }
       
    private void layoutImages() {
    	
    	GridLayout photoList = (GridLayout)rootView.findViewById(R.id.photo_list);
    	int currentId = 0;
    	for (String imageFile : images) {
    		View imageView = photoList.findViewById(currentId);    		
    		if (imageView == null) {
    	        ImageView imgView = new ImageView(MddbApplication.getAppContext());
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
    	        imgView.setId(currentId);
    	        
    	        addEditImageListener(imgView);
    	        
    	        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
    	        layoutParams.setMargins(20, 20, 20, 20);
    	        photoList.addView(imgView, layoutParams);
    		}	
    		currentId++;
    	}
    }
    
    private void addEditImageListener(ImageView image) {
    	final Activity activity = this.getActivity();
    	image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	String fileName = images.get(view.getId());
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
