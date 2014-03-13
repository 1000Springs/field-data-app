package nz.cri.gns.springs.util;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * User interface utility functions.
 * @author duncanw
 */
public class UiUtil {
		
	/**
	 * Maximum milliseconds between user screen taps for them to be considered 
	 * a double-tap.
	 */
	public static final long DOUBLE_TAP_DELAY_MILLIS = 500l; 
	
	/**
	 * Loads an image from file, minimising memory usage by targeting the
	 * given dimensions.
	 * @param imageFile path of image file to load
	 * @param targetHeight image display height, in pixels
	 * @param targetWidth image display width, in pixels.
	 * @return the loaded image, as a Bitmap.
	 */
	public static Bitmap loadImage(String imageFile, int targetWidth, int targetHeight) {
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        options.inSampleSize = UiUtil.calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageFile, options);        		
	}
	
	/**
	 * Loads an image from this app's resourced directory, minimising memory usage by targeting the
	 * given dimensions.
	 * @param resources the app's resources, e.g from myActivity.getResources()
	 * @param imageId id of the image to load, e.g R.drawable.my_image
	 * @param targetHeight image display height, in pixels
	 * @param targetWidth image display width, in pixels.
	 * @return the loaded image, as a Bitmap.
	 */
	public static Bitmap loadImage(Resources resources, int imageId, int targetWidth, int targetHeight) {
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, imageId, options);
        options.inSampleSize = UiUtil.calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, imageId, options);       		
	}
	
	/**
	 * Calculates the best BitmapFactory.Options.inSampleSize value for minimising memory
	 * usage when loading an image.
	 * @param options
	 * @param reqWidth the image display width
	 * @param reqHeight the image display height
	 * @return a sensible BitmapFactory.Options.inSampleSize value.
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
	/**
	 * Displays a dialog box with the given title, message and an 'OK' button which
	 * closes the dialog.
	 * @param context
	 * @param title
	 * @param message
	 */
	public static void showMessageDialog(Context context, String title, String message) {
		new AlertDialog.Builder(context)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            dialog.dismiss();
	        }
	     })
	     .show();		
	}
	
	/**
	 * Used for filtering views by the UiUtil.getChildren() method below.
	 * @author duncanw
	 */
	public interface ViewFilter {
		
		/**
		 * @param view the View found in the search.
		 * @return true if the given view matches the search criteria, otherwise false.
		 */
		boolean matches(View view);
	}
	
	/**
	 * Performs a depth-first search for Views contained in the given view, and returns
	 * all views that match the given filter. Can be used to perform operations on the child
	 * views directly by putting the operation in the ViewFilter's matches() method
	 * @param v the root view.
	 * @param viewList views that match the filter are added to this list.
	 * @param filter specifies the search criteria for the search.
	 * @return the given list, populated with matching views.
	 */
	public static List<View> getChildren(View v, List<View> viewList, ViewFilter filter) {

	    if (v instanceof ViewGroup) {
		    ViewGroup viewGroup = (ViewGroup) v;
		    for (int i = 0; i < viewGroup.getChildCount(); i++) {
		        getChildren(viewGroup.getChildAt(i), viewList, filter);
		    }	    
	    }
	    
    	if (filter.matches(v)) {
    		viewList.add(v);
    	}
	   return viewList;
	}	
	
	/**
	 * Adds the given listeners to all text input fields contained in the given view.
	 * @param onFocusChangeListener
	 * @param textChangedListener
	 * @param rootView
	 */
    public static void addEditTextListener(final OnFocusChangeListener onFocusChangeListener, final TextWatcher textChangedListener, View rootView) {

    	getChildren(rootView, new ArrayList<View>(), new ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof EditText) {
					((EditText)view).setOnFocusChangeListener(onFocusChangeListener);
					if (textChangedListener != null) {
						((EditText)view).addTextChangedListener(textChangedListener);
					}
				}
				
				return false;
			}
		});
    }
    
    /**
     * Adds the given listener to all checkboxes contained in the given view.
     * @param changeListener
     * @param rootView
     */
    public static void addCheckBoxListener(final CompoundButton.OnCheckedChangeListener changeListener, View rootView) {

    	getChildren(rootView, new ArrayList<View>(), new ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox) {
					((CheckBox)view).setOnCheckedChangeListener(changeListener);
				}
				
				return false;
			}
		});
    }	
    
    /**
     * @param date milliseconds since 1 January 1970 UTC.
     * @return the given date in the tablet's local timezone, in the tablet's default date format.
     */
    public static String getDisplayDate(long date) {
        Time now = new Time(Time.getCurrentTimezone());
       	now.set(date);
       	return now.format("%c");
    }
    
    /**
     * Sets a spinner (drop-down/select box) selection to the given selection.
     * @param rootView view containing the specified spinner
     * @param spinnerId e.g R.id.district_spinner
     * @param selection value to set as the spinner's current selection.
     */
    public static void setSelectedValue(View rootView, int spinnerId, String selection) {
    	if (selection != null) {
    		Spinner spinner = (Spinner) rootView.findViewById(spinnerId);   
    		SpinnerAdapter adapter = spinner.getAdapter();
        	for (int i = 0; i < adapter.getCount(); i++) {
        		String option = adapter.getItem(i).toString();
        		if (selection.equals(option)) {
        			spinner.setSelection(i);
        			return;
        		}
        	}     		
    	}
    }
}
