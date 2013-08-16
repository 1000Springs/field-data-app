package nz.cri.gns.springs.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UiUtil {
		
	public static final long DOUBLE_TAP_DELAY_MILLIS = 500l; 
	
	
	public static Bitmap loadImage(String imageFile, int targetHeight, int targetWidth) {
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        options.inSampleSize = UiUtil.calculateInSampleSize(options, targetHeight, targetWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageFile, options);        		
	}
	
	public static Bitmap loadImage(Resources resources, int imageId, int targetHeight, int targetWidth) {
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, imageId, options);
        options.inSampleSize = UiUtil.calculateInSampleSize(options, targetHeight, targetWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, imageId, options);       		
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
	
	public static void showWarningDialog(Context context, String title, String message) {
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
}
