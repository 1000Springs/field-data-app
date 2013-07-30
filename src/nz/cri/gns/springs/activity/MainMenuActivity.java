package nz.cri.gns.springs.activity;

import nz.cri.gns.springs.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main_menu);
        
        findViewById(R.id.new_bio_sample_button)
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuActivity.this, BioSampleActivity.class);
                startActivity(intent);
            }
        });
    }

}