package nz.cri.gns.springs;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GpsLocation implements LocationListener {

	private long lastUpdateTimeMillis = 0;
	private Location lastLocation = null;
	
	private boolean gpsDisabled = false;
	
	public GpsLocation(Activity activity) {
        
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		
		if (location.getTime() > lastUpdateTimeMillis) {
			lastUpdateTimeMillis = location.getTime();
			lastLocation = location;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			gpsDisabled = true;
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			gpsDisabled = false;
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
	
	public Location getLastLocation() {
		
		return lastLocation;
	}
	
	public boolean isGpsDisabled() {
		return gpsDisabled;
	}

}
