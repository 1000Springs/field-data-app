package nz.cri.gns.springs.fragments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import nz.cri.gns.springs.SpringsApplication;
import nz.cri.gns.springs.activity.ManageBioSamplesActivity;
import nz.cri.gns.springs.db.BiologicalSample;
import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.PersistentObject;
import nz.cri.gns.springs.db.SpringsDbHelper;
import nz.cri.gns.springs.db.Survey;
import nz.cri.gns.springs.db.SurveyImage;
import nz.cri.gns.springs.util.Util;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * The ExportSamplesFragment class is closely coupled to the ManageBioSamplesActivity class.
 * Unlike other classes in the fragments package it is not used to create a screen in
 * the user interface, instead it is used to manage long running file export operations.
 * 
 * This is required to prevent exports from failing if the user changes the screen
 * orientation during a file export operation.
 * 
 * Based on code copied from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 * 
 * @author duncanw
 */
public class ExportSamplesFragment extends Fragment {
	
	/**
	 * Callback interface through which the fragment will report the task's
	 * progress and results back to the Activity.
	 */
	public static interface TaskCallbacks {
		void onPreSampleExport();
		void onPostSampleExport(Integer exportCount, Integer notExportedCount, String exportDir);
		void onSampleExportError(String directory);
	}

	private ExportSamplesTask mTask;
	private boolean exportInProgress = false;
	
	public void setParameters(List<BiologicalSample> sampleList, SpringsDbHelper helper, String exportDir, String timestamp, String directory) {
		this.mTask = new ExportSamplesTask(this, sampleList, exportDir, timestamp, directory);
	}
	
	public boolean isExportInProgress() {
		return exportInProgress;
	}

	/**
	 * Hold a reference to the parent Activity so we can report the task's
	 * current progress and results. The Android framework will pass us a
	 * reference to the newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mTask.activity = (ManageBioSamplesActivity)activity;
	}

	/**
	 * This method will only be called once when the retained Fragment is first
	 * created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);

		// Create and execute the background task.
		mTask.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Set the callback to null so we don't accidentally leak the Activity instance.
		mTask.activity = null;
	}
	
	
	private static class ExportSamplesTask extends AsyncTask<Void, Void, Integer> {
		
		ExportSamplesFragment fragment;
		TaskCallbacks activity;
		SpringsDbHelper helper;
		
		List<BiologicalSample> sampleList;
		String exportDir;
		String timestamp;
		String directory;
		IOException error;
		List<String> samplesNotExported = new LinkedList<String>();
		
		BufferedWriter sampleWriter = null;
		BufferedWriter featureWriter = null;
		
		private ExportSamplesTask(ExportSamplesFragment fragment, List<BiologicalSample> sampleList, String exportDir, String timestamp, String directory) {
			
			this.fragment = fragment;
			this.sampleList = sampleList;
			this.helper =  OpenHelperManager.getHelper(SpringsApplication.getAppContext(), SpringsDbHelper.class);;
			this.exportDir = exportDir;
			this.timestamp = timestamp;
			this.directory = directory;
		}
		
		@Override 
		protected void onPreExecute() {
			if (activity != null) {
				activity.onPreSampleExport();
			}
		}
		
		private SpringsDbHelper getHelper() {
			return helper;
		}

	    @Override
	    protected Integer doInBackground(Void...args) {
	    	
	    	fragment.exportInProgress = true;
			List<BiologicalSample> samplesExported = new LinkedList<BiologicalSample>();
	    	try {
				String fileExtension = ".xls";
				String sampleFile = exportDir + "/data-samples-" + timestamp + fileExtension;
				String featureFile = exportDir + "/data-features-"+ timestamp + fileExtension;
				List<Feature> featuresExported = new LinkedList<Feature>();	
				List<Survey> surveysExported = new LinkedList<Survey>();
				try {
					String encoding = "UTF-8";
					for (BiologicalSample sample : sampleList) {	
						exportSample(samplesExported, sampleFile, featureFile,
								featuresExported, surveysExported, encoding,
								sample);
					}
	
				} finally {
					if (sampleWriter != null) {
						sampleWriter.close();
					}
					if (featureWriter != null) {
						featureWriter.close();
					}			
				} 
				
				for (Feature feature : featuresExported) {
					feature.setStatus(PersistentObject.Status.EXPORTED);
					getHelper().getFeatureDao().update(feature);
				}
				
				for (Survey survey : surveysExported) {
					survey.setStatus(PersistentObject.Status.EXPORTED);
					getHelper().getSurveyDao().update(survey);
				}
				
				for (BiologicalSample sample : samplesExported) {
					sample.setStatus(PersistentObject.Status.EXPORTED);
					getHelper().getBiologicalSampleDao().update(sample);
				}
				
	    	} catch (IOException e) {
	    		error = e;    		
	    	} finally {
	    		fragment.exportInProgress = false;
	    		OpenHelperManager.releaseHelper();
	    	}
	    	
			return samplesExported.size();
	    }

	    private void exportSample(List<BiologicalSample> samplesExported,
				String sampleFile, String featureFile,
				List<Feature> featuresExported, List<Survey> surveysExported,
				String encoding, BiologicalSample sample)
				throws UnsupportedEncodingException, FileNotFoundException,
				IOException {
			
			Survey survey = sample.getSurvey();
			getHelper().getSurveyDao().refresh(survey);
			Feature feature = survey.getFeature();
			if (feature != null) {	
				if (sampleWriter == null) {
					sampleWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(sampleFile), encoding)));
					sampleWriter.write(toComment(Util.join("\t", "FeatureName", BiologicalSample.tsvStringColumns(), Survey.tsvStringColumns())));
					sampleWriter.newLine();
				}					
				getHelper().getFeatureDao().refresh(feature);
				if (feature.isForExport()) {
					if (featureWriter == null) {
						featureWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(featureFile), encoding)));
						featureWriter.write(toComment(Feature.tsvStringColumns()));
						featureWriter.newLine();
					}
					featureWriter.write(feature.toTsvString());
					featureWriter.newLine();
					featuresExported.add(feature);
				}
				sampleWriter.write(Util.join("\t", feature.getFeatureName(), sample.toTsvString(), survey.toTsvString()));
				sampleWriter.newLine();
				exportImages(exportDir, sample.getFormattedSampleNumber(), survey, helper);
				surveysExported.add(survey);
				samplesExported.add(sample);
			} else {
				samplesNotExported.add(sample.getFormattedSampleNumber());
			}
		}
	    
	    private void exportImages(String exportDir, String sampleNumber, Survey survey, SpringsDbHelper helper)  {
	    	
			List<SurveyImage> imageList = SurveyImage.getBySurvey(survey, helper);	
			for (SurveyImage image : imageList) {
				File imageSrc  = new File (image.getFileName());
				String imageType = (image.getImageType() != null) ? image.getImageType().replaceAll("_", "") : "";
				File exportDest = new File(exportDir + "/" + Util.join("_", sampleNumber, imageType, imageSrc.getName()));
				try {
					Util.copy(imageSrc, exportDest);
				} catch (IOException e) {
					Log.e(this.getClass().getName(), "Error exporting image "+imageSrc+" to "+exportDest, e);
				}
			}
		}
		
	    private String toComment(String line) {
			return "#" + line;
		}
	    
	    @Override
	    protected void onPostExecute(Integer exportCount) {
	    	
	    	if (activity != null) {
		    	if (error != null) {
		    		activity.onSampleExportError(directory);
		    	} else {
		    		activity.onPostSampleExport(exportCount, samplesNotExported.size(), exportDir);
					
				} 
	    	}
    	}
	    
	}
}
