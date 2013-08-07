/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class BioSampleActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
	private SectionTabsPagerAdapter mAppSectionsPagerAdapter;
	
	private BiologicalSample currentSample;
	
	public static final String BIOLOGICAL_SAMPLE = "nz.cri.gns.springs.activity.BiologicalSample";

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    private ViewPager mViewPager;
    
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
	            OpenHelperManager.getHelper(this, SpringsDbHelper.class);
	    }
	    return databaseHelper;
	}    

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bio_sample);
        
        setCurrentSample();

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new SectionTabsPagerAdapter(getSupportFragmentManager(), currentSample);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(currentSample.getFormattedSampleNumber());
 
        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
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
