package nz.cri.gns.springs.db;

import android.provider.BaseColumns;

public class SpringsContract {

    
    public static abstract class Survey implements BaseColumns {
        public static final String TABLE_NAME = "SURVEY";
        
        public static final String COLUMN_NAME_FEATURE_ID = "FEATURE_ID";
        
        // Date and time the feature was surveyed
        public static final String COLUMN_NAME_SURVEY_DATE = "SURVEY_DATE";
        
        // Size of the feature at the time of the survey
        public static final String COLUMN_NAME_SIZE = "SIZE";
        public static final String COLUMN_NAME_COLOUR = "COLOUR";
        public static final String COLUMN_NAME_CLARITY = "CLARITY";
        
        // Temperature of feature upon first arriving at the site
        public static final String COLUMN_NAME_TEMPERATURE = "TEMPERATURE";
        public static final String COLUMN_NAME_OBSERVER_1 = "OBSERVER_1";
        public static final String COLUMN_NAME_OBSERVER_2 = "OBSERVER_2";
    }
    
    public static abstract class BiologicalSample implements BaseColumns {
        public static final String TABLE_NAME = "BIOLOGICAL_SAMPLE";
        
        public static final String COLUMN_NAME_SURVEY_ID = "SURVEY_ID";
        
        // Temperature of sample
        public static final String COLUMN_NAME_TEMPERATURE = "TEMPERATURE";
        
        // Size of the feature at the time of the survey
        public static final String COLUMN_NAME_PH = "SIZE";
        public static final String COLUMN_NAME_COLOUR = "COLOUR";
        public static final String COLUMN_NAME_CLARITY = "CLARITY";
        
        // Full name of the observers
        public static final String COLUMN_NAME_OBSERVER_1 = "OBSERVER_1";
        public static final String COLUMN_NAME_OBSERVER_2 = "OBSERVER_2";
    }    
}
