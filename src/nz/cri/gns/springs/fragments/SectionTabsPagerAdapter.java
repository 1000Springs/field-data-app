package nz.cri.gns.springs.fragments;

import java.util.Arrays;
import java.util.List;

import nz.cri.gns.springs.MddbApplication;
import nz.cri.gns.springs.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionTabsPagerAdapter extends FragmentPagerAdapter {
	
	private List<SectionTab> tabs;
	
    public SectionTabsPagerAdapter(FragmentManager fm) {
        super(fm);
        tabs = Arrays.asList(new SectionTab[]{
        	new SectionTab(MddbApplication.getAppContext().getString(R.string.feature_id_tab), new FeatureIdentificationFragment()),
        	new SectionTab(MddbApplication.getAppContext().getString(R.string.appearance_tab), new AppearanceFragment()),
        	new SectionTab(MddbApplication.getAppContext().getString(R.string.photos_tab), new PhotoFragment())
        });
    }

	@Override
	public Fragment getItem(int i) {
		return tabs.get(i).getFragment();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tabs.size();
	}
	
    @Override
    public CharSequence getPageTitle(int i) {
        return  tabs.get(i).getTitle();
    }

}
