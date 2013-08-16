package nz.cri.gns.springs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.res.Resources;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class Util {
	
	public static String join(String delimiter, String...values) {

		return join(delimiter, Arrays.asList(values));
	}

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
	
	public static <T> T[] concatArrays(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	
	public static String format(Number d) {
		if (d == null) {
			return "";
		} else {
			return new DecimalFormat("#.####").format(d);
		}
	}
	
	public static void copy(File src, File dst) throws IOException {
		copy(new FileInputStream(src), new FileOutputStream(dst));
	}
	
    public static void copy(Resources resources, int resourceId, File dst) throws IOException {
    	  
       copy(resources.openRawResource(resourceId), new FileOutputStream(dst));
        
    }
    
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
	
	public interface ViewFilter {
		boolean matches(View view);
	}
	
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
	
    public static void addEditTextListener(final OnFocusChangeListener onFocusChangeListener, final TextWatcher textChangedListener, View rootView) {

    	Util.getChildren(rootView, new ArrayList<View>(), new Util.ViewFilter() {
			
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
    
    public static void addCheckBoxListener(final CompoundButton.OnCheckedChangeListener changeListener, View rootView) {

    	Util.getChildren(rootView, new ArrayList<View>(), new Util.ViewFilter() {
			
			@Override
			public boolean matches(View view) {
				if (view instanceof CheckBox) {
					((CheckBox)view).setOnCheckedChangeListener(changeListener);
				}
				
				return false;
			}
		});
    }
	
}
