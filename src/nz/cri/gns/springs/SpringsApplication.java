package nz.cri.gns.springs;

import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class SpringsApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        SpringsApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SpringsApplication.context;
    }
    
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
