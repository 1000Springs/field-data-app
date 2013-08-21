package nz.cri.gns.springs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import android.content.res.Resources;
import android.text.format.Time;

/**
 * General utility functions.
 * @author duncanw
 */
public class Util {
	
	/** 
	 * @param delimiter the value separator, e.g "," to get a CSV string
	 * @param values the values to join
	 * @return e.g join(",", "one", "two", "three") returns "one,two,three".
	 *         Null values will be included in the return value as empty strings,
	 *         e.g join(",", "one", null, "three") returns "one,,three".
	 */
	public static String join(String delimiter, String...values) {

		return join(delimiter, Arrays.asList(values));
	}

	/** 
	 * @param delimiter the value separator, e.g "," to get a CSV string
	 * @param s the values to join
	 * @return e.g join(",", ["one", "two", "three"]) returns "one,two,three".
	 *         Null values will be included in the return value as empty strings,
	 *         e.g join(",", ["one", null, "three"]) returns "one,,three".
	 */
	public static String join(String delimiter, Collection<?> s) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			Object value = iter.next();
			if (value != null) {
				builder.append(value);
			}
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}
	
	/**
	 * @param d a Float or Double
	 * @return the empty string if d is null, otherwise d rounded to 4 decimal places with half-even rounding
	 *         (Rounded towards the "nearest neighbour" unless both neighbours are equidistant, in which case, 
	 *          round towards the even neighbour).
	 */
	public static String format(Number d) {
		if (d == null) {
			return "";
		} else {
			return new DecimalFormat("#.####").format(d);
		}
	}
	
	/**
	 * Copies a file.
	 * @param src source file to copy.
	 * @param dst destination file to copy to.
	 * @throws IOException
	 */
	public static void copy(File src, File dst) throws IOException {
		copy(new FileInputStream(src), new FileOutputStream(dst));
	}
	
	/**
	 * Copies a resource to a file.
	 * @param resources the app's resources, e.g from myActivity.getResources()
	 * @param resourceId id of the resource to copy, e.g R.drawable.my_image
	 * @param dst destination file to copy to.
	 * @throws IOException
	 */
    public static void copy(Resources resources, int resourceId, File dst) throws IOException {
    	  
       copy(resources.openRawResource(resourceId), new FileOutputStream(dst));
        
    }
    
    /**
     * Copies data from in to out until exhausted, then closes both streams.
     * @param in source.
     * @param out destination.
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {

    	try {
	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
    	} finally {
    		in.close();
    		out.close();
    	}
    }

    /**
     * @return the current date in the tablet's local timezone, in yyyyMMddHHmmss format.
     */
    public static String getTimestampSeconds() {
        Time now = new Time(Time.getCurrentTimezone());
       	now.set(System.currentTimeMillis());
		return now.format("%Y%m%d%H%M%S");	
    }
    
    /**
     * @return the current date in the tablet's local timezone, in yyyyMMddHHmmssSSS format.
     */
    public static String getTimestampMillis() {

    	// Locale.US just ensures we only deal in ASCII characters
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
    	return formatter.format(new java.util.Date());	
    }
	
}
