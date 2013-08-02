package nz.cri.gns.springs.fragments;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import nz.cri.gns.springs.db.SpringsDbHelper;
import android.support.v4.app.Fragment;

public class SpringsFragment extends Fragment {
	
	private SpringsDbHelper databaseHelper = null;

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}

	protected SpringsDbHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this.getActivity(), SpringsDbHelper.class);
	    }
	    return databaseHelper;
	}	

}
