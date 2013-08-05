package nz.cri.gns.springs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class Util {
	
	public static String join(String[] values, String delimiter) {

		return join(Arrays.asList(values), delimiter);
	}

	public static String join(Collection<?> s, String delimiter) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
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
