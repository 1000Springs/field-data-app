package nz.cri.gns.springs.db.test;

import java.util.List;

import nz.cri.gns.springs.db.Feature;
import nz.cri.gns.springs.db.FeatureDb;
import nz.cri.gns.springs.db.SpringsDbHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class FeatureDbTest extends AndroidTestCase {
	
	private static final String TEST_FILE_PREFIX = "test_";
	private SpringsDbHelper dbHelper;
	
	@Override
	protected void setUp() throws Exception {
	    super.setUp();

	    RenamingDelegatingContext context 
	        = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);

	   dbHelper = new SpringsDbHelper(context);
	}
	
	public void testCreateRead() {

		Feature feature = getTestFeatureOne();
		FeatureDb featureDb = new FeatureDb(dbHelper);
		featureDb.create(feature);
		assertTrue(feature.getId() > 0);
		
		Feature retrieved = featureDb.getByName(feature.getFeatureName());
		assertReflectionEquals(feature, retrieved);
	}
	
	public void testReadAll() {

		Feature featureB = getTestFeatureOne();
		featureB.setFeatureName("B");
		Feature featureA = getTestFeatureTwo();
		featureA.setFeatureName("A");
		FeatureDb featureDb = new FeatureDb(dbHelper);
		featureDb.create(featureB);
		featureDb.create(featureA);
		
		List<Feature> features = featureDb.readAll();
		assertEquals(2, features.size());
		
		// Features should be sorted alphabetically by name
		assertReflectionEquals(features.get(0), featureA);
		assertReflectionEquals(features.get(1), featureB);
	}	
	
	public void testUpdate() throws InterruptedException {
		
		Feature feature = getTestFeatureOne();
		FeatureDb featureDb = new FeatureDb(dbHelper);
		featureDb.create(feature);
		assertEquals("NEW", feature.getStatus());
		
		Feature featureUpdate = getTestFeatureTwo();
		featureUpdate.setId(feature.getId());
		
		// Sleep to ensure UPDATED_DATE has a new value
		Thread.sleep(1000);
		featureDb.update(featureUpdate);

		List<Feature> features = featureDb.readAll();
		assertEquals(1, features.size());
		
		assertReflectionEquals(featureUpdate, features.get(0));
		assertEquals("UPDATED", featureUpdate.getStatus());
		assertNotSame(featureUpdate.getUpdatedDate(), feature.getUpdatedDate());
	}
	
	public static Feature getTestFeatureOne() {
		
		Feature feature = new Feature();
		feature.setFeatureName("Feature name");
		feature.setHistoricName("Historic name");
		feature.setGeothermalField("Geothermal field");
		feature.setFeatureType("Feature type");
		feature.setDescription("Description");
		feature.setCoordNztmEast(123.45);
		feature.setCoordNztmNorth(456.89);
		feature.setCoordErrorEst(1.3);
		feature.setCoordFeatureRel("Relationship to feature");
		
		return feature;
	}
	
	public static Feature getTestFeatureTwo() {
		
		Feature feature = new Feature();
		feature.setFeatureName("Feature name 2");
		feature.setHistoricName("Historic name 2");
		feature.setGeothermalField("Geothermal field 2");
		feature.setFeatureType("Feature type 2");
		feature.setDescription("Description 2");
		feature.setCoordNztmEast(-98736.21);
		feature.setCoordNztmNorth(56875.0);
		feature.setCoordErrorEst(12.2);
		feature.setCoordFeatureRel("Relationship to feature 2");
		
		return feature;
	}

}
