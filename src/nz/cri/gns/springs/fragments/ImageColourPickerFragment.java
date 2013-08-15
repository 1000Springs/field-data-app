package nz.cri.gns.springs.fragments;

import nz.cri.gns.springs.R;
import nz.cri.gns.springs.util.DataStatistics;
import nz.cri.gns.springs.util.UiUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ImageColourPickerFragment extends SpringsDialogFragment implements OnTouchListener {

	private Integer selectedColour;
	private String imageFile;
	
	private static final String IMAGE_FILE_KEY = "imageFile";
	
	public static final String COLOUR_KEY = "colourKey";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView = inflater.inflate(R.layout.fragment_image_colour_picker, container, false);
    	getDialog().setTitle(R.string.image_colour_picker_dialog_title);
    	
    	displayImage(rootView);
    	addOkButtonListener(rootView);
    	addCancelButtonListener(rootView);
    	
    	setColour(rootView);
    	
    	return rootView;
    }
    
    private void setColour(View rootView) {
    	if (selectedColour != null)  {
    		View colour = rootView.findViewById(R.id.selected_image_colour);
    		colour.setBackgroundColor(selectedColour);
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putString(IMAGE_FILE_KEY, imageFile);
    	instanceState.putInt(COLOUR_KEY, selectedColour);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		imageFile = savedInstanceState.getString(IMAGE_FILE_KEY);
    		selectedColour = savedInstanceState.getInt(COLOUR_KEY);
    	}
    }      
    
    public void displayImage(View rootView) {
    	
    	ImageView imgView = (ImageView) rootView.findViewById(R.id.colour_picker_image);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = UiUtil.calculateInSampleSize(options, 700, 700);
        options.inJustDecodeBounds = false;
        
        Bitmap bitmap;
        if (imageFile == null) {
        	bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.colour_picker, options);
        } else {
        	bitmap = BitmapFactory.decodeFile(imageFile);
        }
        
        imgView.setImageBitmap(bitmap);
        
        imgView.setAdjustViewBounds(true);
        imgView.setMaxHeight(700);
        imgView.setMaxWidth(700);
        imgView.setOnTouchListener(this);
    }
    
    
    public void addOkButtonListener(final View rootView) {
    	
    	Button okButton = (Button) rootView.findViewById(R.id.ok_colour_select_button);
    	okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	dismiss();
            	if (selectedColour != null) {
                	getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent().putExtra(COLOUR_KEY, selectedColour));
            	}
            }
        });
    } 
    
    public void addCancelButtonListener(final View rootView) {
    	
    	Button cancelButton = (Button) rootView.findViewById(R.id.cancel_colour_select_button);
    	cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	dismiss();
            }
        });
    }    
    
    
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			ImageView imageView = (ImageView)view;
			BitmapDrawable image = (BitmapDrawable)imageView.getDrawable();
			selectedColour = getColour(imageView, image.getBitmap(), (int)event.getX(), (int)event.getY());
			setColour(this.getView());
		}
		
		return true;
	}
	
	
	private int getColour(ImageView imageView, Bitmap image, int x, int y) {
		
		int gridRadius = 10;
		int minX = Math.max(0, x - gridRadius);
		int minY = Math.max(0, y - gridRadius);
		int maxX = Math.min(image.getWidth(), x + gridRadius);
		int maxY = Math.min(image.getHeight(), y + gridRadius);
		
		int pixelCount = (1 + maxX - minX) * (1 + maxY - minY);
		int[] redValues = new int[pixelCount];
		int[] greenValues = new int[pixelCount];
		int[] blueValues = new int[pixelCount];
		int i = 0;
		for (x = minX; x <= maxX; x++) {
			int transformedX = transformX(imageView, image, (double)x);
			for (y = minY; y <= maxY; y++) {
				int transformedY = transformY(imageView, image, (double)y);
				int pixel = image.getPixel(transformedX, transformedY);
				redValues[i] = Color.red(pixel);
				greenValues[i] = Color.green(pixel);
				blueValues[i] = Color.blue(pixel);
				i++;
			}
		}
		
		int red = new DataStatistics(redValues).calculate().getWeightedAverage();
		int green = new DataStatistics(greenValues).calculate().getWeightedAverage();
		int blue = new DataStatistics(blueValues).calculate().getWeightedAverage();
		
		return Color.rgb(red, green, blue);
	}
	

	private int transformX(ImageView imageView, Bitmap image, double x) {
		double displayWidth = imageView.getWidth();
		int transformedX = (int)(((double)image.getWidth() / displayWidth) * x);
		return Math.min(image.getWidth() - 1, transformedX);
	}
	
	private int transformY(ImageView imageView, Bitmap image, double y) {
		double displayHeight = imageView.getHeight();
		int transformedY = (int)(((double)image.getHeight() / displayHeight) * y);
		return Math.min(image.getHeight() - 1, transformedY);
	}
	
	public void setImageFile(String file) {
		this.imageFile = file;
	}
	
	public void setInitialColour(int colour) {
		this.selectedColour = colour;
	}

}
