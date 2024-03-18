package com.android.quemeful_qr;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.sql.DriverManager.println;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

/**
 * This is an activity class used to enable location/map functions, where the event takes place for the users.
 * Reference URLs:
 * https://github.com/osmdroid/osmdroid/issues/1304#issuecomment-477920497
 * Author- (osmdroid) neoacevedo, License- Apache 2.0, Published Date- 28 Mar, 2019
 * https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Java)
 * Author- osmdroid, License- Apache 2.0, Published Date- 19 May, 2022
 * https://stackoverflow.com/questions/34139048/cannot-resolve-manifest-permission-access-fine-location
 * Author- anoop ghildiyal, License- CC BY-SA 3.0, Published Date- 17 Mar, 2017
 * https://stackoverflow.com/a/63456832
 * Author- rilott, License- CC BY-SA 4.0, Published Date- 17 Aug, 2020
 * https://stackoverflow.com/a/71698834
 * Author- REInVent, License- CC BY-SA 4.0, Published Date- 31 Mar, 2022
 */
public class MapActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private ImageView backButton;

    private MapView map = null;
    private IMapController mapController;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;

    /**
     * This onCreate method is used to create the map view on the app.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here


        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_map);

        map = (MapView) findViewById(R.id.map);
        backButton = (ImageView) findViewById(R.id.backArrow);


        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        String[] strArray = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE};

//        requestPermissionsIfNecessary(strArray);


        String[] strArray = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        // Request Location permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            println("Location Permission GRANTED");
        } else {
            println("Location Permission DENIED");
            ActivityCompat.requestPermissions(
                    this,
                    strArray,
                    1
            );
        }


        // Create MapView
        map = findViewById(R.id.map);
        // Set tile source + display settings
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        // Create MapController and set starting location
        mapController = map.getController();

        GpsMyLocationProvider prov = new GpsMyLocationProvider(ctx);
        prov.addLocationSource(LocationManager.NETWORK_PROVIDER);
        // Create location overlay
        this.mLocationOverlay = new MyLocationNewOverlay(prov,map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        this.mLocationOverlay.setDrawAccuracyEnabled(true);

        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapController.animateTo(mLocationOverlay.getMyLocation());
                        mapController.setZoom(15.5);
                    }
                });
            }
        });

        map.getOverlays().add(this.mLocationOverlay);

        // Set user agent
        Configuration.getInstance().setUserAgentValue("RossMaps");

        println(String.valueOf(mLocationOverlay.getMyLocation()));
        println("Create done");

        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();

        this.mLocationOverlay = new MyLocationNewOverlay(prov,map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        this.mLocationOverlay.setDrawAccuracyEnabled(true);

        mapController.setCenter(mLocationOverlay.getMyLocation());
        mapController.animateTo(mLocationOverlay.getMyLocation());

        map.getOverlays().add(this.mLocationOverlay);

        this.mCompassOverlay = new CompassOverlay(getApplicationContext(), new InternalCompassOrientationProvider(getApplicationContext()), map);
        this.mCompassOverlay.enableCompass();
        map.getOverlays().add(this.mCompassOverlay);

        LatLonGridlineOverlay2 overlay = new LatLonGridlineOverlay2();
        map.getOverlays().add(overlay);


        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        map.setMultiTouchControls(true);


        mapController.setZoom(9.5);
//        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
//        mapController.setCenter(startPoint);


    }

    /**
     * This method is used to change map directions depending on the location on resuming.
     */
    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * This method is used to handle the map/location when paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * This method is used to handle the result from the permission requested by the user.
     * @param requestCode The request code passed in.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }


    /**
     * This method is used to ask user permission to enable location on app.
     * @param permissions permission requested to user.
     */
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }



    }

} // activity class closing