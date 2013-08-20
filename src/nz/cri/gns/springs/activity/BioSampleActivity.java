package nz.cri.gns.springs.activity;

import java.util.Arrays;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.R;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.Configuration;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.fragments.AppearanceFragment;
import nz.cri.gns.springs.fragments.BioSampleActivityFragment;
import nz.cri.gns.springs.fragments.BioSampleFragment;
import nz.cri.gns.springs.fragments.ImageFragment;

/**
 * Biological sampling activity, creates the screens (fragments) that enable users to
 * enter geological feature survey and biological sample information, then glues
 * them together by providing the action bar with a tab for each screen. 
 * @author duncanw
 */
public class BioSampleActivity extends FragmentActivity implements ActionBar.TabListener {

	private SectionTabsPagerAdapter mAppSectionsPagerAdapter;
	private BiologicalSample currentSample;
    private ViewPager mViewPager;
	private SpringsDbHelper databaseHelper = null;
	
	private static final String SAMPLE_KEY = "currentSample";
	
	/**
	 * Extra intent data key for the ID of the biological sample selected for viewing by the user.
	 * The value is the numeric portion only, e.g '23', not 'P1.0023'.
	 */
	public static final String BIOLOGICAL_SAMPLE = "nz.cri.gns.springs.activity.BiologicalSample";


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
	            OpenHelperManager.getHelper(this, SpringsDbHelper.class);
	    }
	    return databaseHelper;
	}    

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_bio_sample);
        
    	if (savedInstanceState != null) {
    		currentSample = (BiologicalSample)savedInstanceState.getSerializable(SAMPLE_KEY);
    		getHelper().getBiologicalSampleDao().refresh(currentSample);
    	} 
    	if (currentSample == null) {
    		setCurrentSample();
    	}

        mAppSectionsPagerAdapter = new SectionTabsPagerAdapter(getSupportFragmentManager(), currentSample);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(currentSample.getFormattedSampleNumber()); 
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle instanceState) {
    	super.onSaveInstanceState(instanceState);
    	instanceState.putSerializable(SAMPLE_KEY, currentSample);
    }
        
    private void setCurrentSample() {
    	
    	Bundle extras = this.getIntent().getExtras();
    	if (extras != null) {
    		Long sampleId = extras.getLong(BIOLOGICAL_SAMPLE);
    		currentSample = getHelper().getBiologicalSampleDao().queryForId(sampleId);
    	} else {
    		currentSample = null;
    	}
    	
    	if (currentSample == null) {
    		currentSample = new BiologicalSample();
    		String nextSampleNumberConfig = Configuration.getConfiguration(getResources().getString(R.string.config_next_sample_number), getHelper());
    		int sampleNumber = BiologicalSample.getMaxSampleNumber(getHelper()) + 1;
    		if (nextSampleNumberConfig != null) {
    			sampleNumber = Math.max(sampleNumber, Integer.parseInt(nextSampleNumberConfig));
    		}
    		currentSample.setSampleNumber(sampleNumber);
    		getHelper().getBiologicalSampleDao().create(currentSample);
    	}
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
    static class SectionTabsPagerAdapter extends FragmentPagerAdapter {
    	
    	private List<SectionTab> tabs;
    	
        public SectionTabsPagerAdapter(FragmentManager fm, BiologicalSample currentSample) {
            super(fm);
            tabs = Arrays.asList(new SectionTab[]{
                new SectionTab(SpringsApplication.getAppContext().getString(R.string.survey_data_tab), 
                			new AppearanceFragment().setCurrentSample(currentSample)),            		
            	new SectionTab(SpringsApplication.getAppContext().getString(R.string.sample_data_tab), 
            			new BioSampleFragment().setCurrentSample(currentSample)),
            	new SectionTab(SpringsApplication.getAppContext().getString(R.string.images_tab), 
            			new ImageFragment().setCurrentSample(currentSample))
            });
        }

    	@Override
    	public Fragment getItem(int i) {
    		return tabs.get(i).getFragment();
    	}

    	@Override
    	public int getCount() {
    		return tabs.size();
    	}
    	
        @Override
        public CharSequence getPageTitle(int i) {
            return  tabs.get(i).getTitle();
        }

    } 
    
    
    static class SectionTab {
    	
    	private String title;
    	private BioSampleActivityFragment fragment;
    	
    	public SectionTab(String title, BioSampleActivityFragment fragment) {
    		this.title = title;
    		this.fragment = fragment;
    	}
    	
    	
    	public String getTitle() {
    		return title;
    	}
    	public void setTitle(String title) {
    		this.title = title;
    	}
    	public BioSampleActivityFragment getFragment() {
    		return fragment;
    	}
    	public void setFragment(BioSampleActivityFragment fragment) {
    		this.fragment = fragment;
    	}

    }
}
