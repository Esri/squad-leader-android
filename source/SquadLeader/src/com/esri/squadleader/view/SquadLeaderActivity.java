/*******************************************************************************
 * Copyright 2013 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnPanListener;
import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.militaryapps.controller.LocationController.LocationMode;
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.Location;
import com.esri.squadleader.R;
import com.esri.squadleader.controller.AdvancedSymbologyController;
import com.esri.squadleader.controller.LocationController;
import com.esri.squadleader.controller.MapController;
import com.esri.squadleader.model.BasemapLayer;
import com.esri.squadleader.util.Utilities;
import com.esri.squadleader.view.AddLayerFromWebDialogFragment.AddLayerListener;
import com.esri.squadleader.view.GoToMgrsDialogFragment.GoToMgrsHelper;
import com.ipaulpro.afilechooser.utils.FileUtils;

/**
 * The main activity for the Squad Leader application. Typically this displays a map with various other
 * controls.
 */
public class SquadLeaderActivity extends FragmentActivity
        implements AddLayerListener, GoToMgrsHelper {
    
    private static final String TAG = SquadLeaderActivity.class.getSimpleName();
    private static final double MILLISECONDS_PER_HOUR = 1000 * 60 * 60;
    
    /**
     * A unique ID for the GPX file chooser.
     */
    private static final int REQUEST_CHOOSER = 30046;
    
    /**
     * A unique ID for getting a result from the settings activity.
     */
    private static final int SETTINGS_ACTIVITY = 5862;
    
    private final Handler locationChangeHandler = new Handler() {
        
        private final SpatialReference SR = SpatialReference.create(4326);
        
        private Location previousLocation = null;
        
        @Override
        public void handleMessage(Message msg) {
            if (null != msg) {
                Location location = (Location) msg.obj;
                try {
                    TextView locationView = (TextView) findViewById(R.id.textView_displayLocation);
                    String mgrs = mapController.pointToMgrs(new Point(location.getLongitude(), location.getLatitude()), SR);
                    locationView.setText(getString(R.string.display_location) + mgrs);
                } catch (Throwable t) {
                    Log.i(TAG, "Couldn't set location text", t);
                }
                try {
                    double speedMph = location.getSpeedMph();
                    if (0 == Double.compare(speedMph, 0.0)
                            && null != previousLocation
                            && !mapController.getLocationController().getMode().equals(LocationMode.LOCATION_SERVICE)) {
                        //Calculate speed
                        double distanceInMiles = Utilities.calculateDistanceInMeters(previousLocation, location) / Utilities.METERS_PER_MILE;
                        double timeInHours = (location.getTimestamp().getTimeInMillis() - previousLocation.getTimestamp().getTimeInMillis()) /  MILLISECONDS_PER_HOUR;
                        speedMph = distanceInMiles / timeInHours;
                    }
                    ((TextView) findViewById(R.id.textView_displaySpeed)).setText(
                            getString(R.string.display_speed) + Double.toString(Math.round(10.0 * speedMph) / 10.0) + " mph");
                } catch (Throwable t) {
                    Log.i(TAG, "Couldn't set speed text", t);
                }
                try {
                    if (null == angularUnitPreference) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SquadLeaderActivity.this);
                        int wkid = Integer.parseInt(sp.getString(getString(R.string.pref_angularUnits), Integer.toString(AngularUnit.Code.DEGREE)));
                        angularUnitPreference = (AngularUnit) AngularUnit.create(wkid);
                    }
                    String headingString = LocationController.headingToString(location.getHeading(), angularUnitPreference, 0);
                    ((TextView) findViewById(R.id.textView_displayHeading)).setText(getString(R.string.display_heading) + headingString);
                } catch (Throwable t) {
                    Log.i(TAG, "Couldn't set heading text", t);
                }
                previousLocation = location;
            }
        };
    };
    
    private final OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref_angularUnits))) {
                try {
                    int angularUnitWkid = Integer.parseInt(sharedPreferences.getString(key, "0"));
                    angularUnitPreference = (AngularUnit) AngularUnit.create(angularUnitWkid);
                } catch (Throwable t) {
                    Log.i(TAG, "Couldn't get " + getString(R.string.pref_angularUnits) + " value", t);
                }
            }
        }
    };
    
    private MapController mapController = null;
    private AdvancedSymbologyController mil2525cController = null;
    private AddLayerFromWebDialogFragment addLayerFromWebDialogFragment = null;
    private GoToMgrsDialogFragment goToMgrsDialogFragment = null;
    private boolean wasFollowMeBeforeMgrs = false;
    private final Timer clockTimer = new Timer(true);
    private TimerTask clockTimerTask = null;
    private AngularUnit angularUnitPreference = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        
        setContentView(R.layout.main);
        adjustLayoutForOrientation(getResources().getConfiguration().orientation);

        final MapView mapView = (MapView) findViewById(R.id.map);
        
        mapView.setOnPanListener(new OnPanListener() {
            
            private static final long serialVersionUID = 0x58d30af8d168f63aL;

            @Override
            public void prePointerUp(float fromx, float fromy, float tox, float toy) {}
            
            @Override
            public void prePointerMove(float fromx, float fromy, float tox, float toy) {
                setFollowMe(false);
            }
            
            @Override
            public void postPointerUp(float fromx, float fromy, float tox, float toy) {}
            
            @Override
            public void postPointerMove(float fromx, float fromy, float tox, float toy) {}
            
        });

        mapController = new MapController(mapView, getAssets());
        ((NorthArrowView) findViewById(R.id.northArrowView)).setMapController(mapController);
        try {
            mil2525cController = new AdvancedSymbologyController(
                    mapController,
                    getAssets(),
                    getString(R.string.sym_dict_dirname));
            mapController.setAdvancedSymbologyController(mil2525cController);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find file while loading AdvancedSymbologyController", e);
        }
        
        mapController.getLocationController().addListener(new LocationListener() {
            
            @Override
            public void onLocationChanged(final Location location) {
                if (null != location) {
                    //Do this in a thread in case we need to calculate the speed
                    new Thread() {
                        public void run() {
                            Message msg = new Message();
                            msg.obj = location;
                            locationChangeHandler.sendMessage(msg);
                        }
                    }.start();
                }
            }
        });

        clockTimerTask = new TimerTask() {
            
            private final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        if (null != msg.obj) {
                            ((TextView) findViewById(R.id.textView_displayTime)).setText(getString(R.string.display_time) + msg.obj);
                        }
                    } catch (Throwable t) {
                        Log.i(TAG, "Couldn't update time", t);
                    }
                }
            };
            
            @Override
            public void run() {                
                if (null != mapController) {
                    Message msg = new Message();
                    msg.obj = Utilities.DATE_FORMAT_MILITARY_ZULU.format(new Date());
                    handler.sendMessage(msg);
                }
            }
            
        };
        clockTimer.schedule(clockTimerTask, 0, Utilities.ANIMATION_PERIOD_MS);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutForOrientation(newConfig.orientation);
    }
    
    private void adjustLayoutForOrientation(int orientation) {
        View displayView = findViewById(R.id.tableLayout_display);
        if (displayView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) displayView.getLayoutParams();
            switch (orientation) {
                case Configuration.ORIENTATION_LANDSCAPE: {
                    params.addRule(RelativeLayout.RIGHT_OF, R.id.toggleButton_grid);
                    params.addRule(RelativeLayout.LEFT_OF, R.id.toggleButton_followMe);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imageButton_zoomOut);
                    params.addRule(RelativeLayout.ABOVE, -1);
                    break;
                }
                case Configuration.ORIENTATION_PORTRAIT:
                default: {
                    params.addRule(RelativeLayout.RIGHT_OF, -1);
                    params.addRule(RelativeLayout.LEFT_OF, R.id.imageButton_zoomIn);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imageButton_zoomIn);
                    params.addRule(RelativeLayout.ABOVE, R.id.imageButton_openMapMenu);
                }
            }
        }
    }
    
    private boolean isFollowMe() {
        ToggleButton followMeButton = (ToggleButton) findViewById(R.id.toggleButton_followMe);
        if (null != followMeButton) {
            return followMeButton.isChecked();
        } else {
            return false;
        }
    }
    
    private void setFollowMe(boolean isFollowMe) {
        ToggleButton followMeButton = (ToggleButton) findViewById(R.id.toggleButton_followMe);
        if (null != followMeButton) {
            if (isFollowMe != followMeButton.isChecked()) {
                followMeButton.performClick();
            }
        }
    }
    
    public MapController getMapController() {
        return mapController;
    }

    @Override
    public void beforePanToMgrs(String mgrs) {
        wasFollowMeBeforeMgrs = isFollowMe();
        setFollowMe(false);
    }

    @Override
    public void onPanToMgrsError(String mgrs) {
        if (wasFollowMeBeforeMgrs) {
            setFollowMe(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mapController.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mapController.unpause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_layer_from_web:
                //Present Add Layer from Web dialog
                if (null == addLayerFromWebDialogFragment) {
                    addLayerFromWebDialogFragment = new AddLayerFromWebDialogFragment();
                }
                addLayerFromWebDialogFragment.show(getSupportFragmentManager(), getString(R.string.add_layer_from_web_fragment_tag));
                return true;
            case R.id.go_to_mgrs:
                //Present Go to MGRS dialog
                if (null == goToMgrsDialogFragment) {
                    goToMgrsDialogFragment = new GoToMgrsDialogFragment();
                }
                goToMgrsDialogFragment.show(getSupportFragmentManager(), getString(R.string.go_to_mgrs_fragment_tag));
                return true;
            case R.id.set_location_mode:
                //Present Set Location Mode dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.set_location_mode)
                        .setNegativeButton(R.string.cancel, null)
                        .setSingleChoiceItems(
                                new String[] {
                                        getString(R.string.option_location_service),
                                        getString(R.string.option_simulation_builtin),
                                        getString(R.string.option_simulation_file)},
                                mapController.getLocationController().getMode() == LocationMode.LOCATION_SERVICE ? 0 : 
                                    null == mapController.getLocationController().getGpxFile() ? 1 : 2,
                                new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (2 == which) {
                                        //Present file chooser
                                        Intent getContentIntent = FileUtils.createGetContentIntent();
                                        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                                        startActivityForResult(intent, REQUEST_CHOOSER);
                                    } else {
                                        mapController.getLocationController().setGpxFile(null);
                                        mapController.getLocationController().setMode(
                                                0 == which ? LocationMode.LOCATION_SERVICE : LocationMode.SIMULATOR,
                                                true);
                                    }
                                } catch (Exception e) {
                                    Log.d(TAG, "Couldn't set location mode", e);
                                } finally {
                                    dialog.dismiss();
                                }
                            }
                            
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Called when an activity called by this activity returns a result. This method was initially
     * added to handle the result of choosing a GPX file for the LocationSimulator.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER: {
                if (resultCode == RESULT_OK) {  
                    final Uri uri = data.getData();
                    File file = FileUtils.getFile(uri);
                    mapController.getLocationController().setGpxFile(file);
                    try {
                        mapController.getLocationController().setMode(LocationMode.SIMULATOR, true);
                    } catch (Exception e) {
                        Log.d(TAG, "Could not start simulator", e);
                    }
                }
                break;
            }
            case SETTINGS_ACTIVITY: {
                if (null != data && data.getBooleanExtra(getString(R.string.pref_resetApp), false)) {
                    try {
                        mapController.reset();
                    } catch (Throwable t) {
                        Log.e(TAG, "Could not reset map", t);
                    }
                }
                break;
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
    
    public void imageButton_zoomIn_clicked(View view) {
	mapController.zoomIn();
    }
    
    public void imageButton_zoomOut_clicked(View view) {
	mapController.zoomOut();
    }
    
    public void imageButton_openBasemapPanel_clicked(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_basemap)
                .setNegativeButton(R.string.cancel, null);
        List<BasemapLayer> basemapLayers = mapController.getBasemapLayers();
        String[] basemapLayerNames = new String[basemapLayers.size()];
        for (int i = 0; i < basemapLayers.size(); i++) {
            basemapLayerNames[i] = basemapLayers.get(i).getLayer().getName();
        }
        builder.setSingleChoiceItems(
                basemapLayerNames,
                mapController.getVisibleBasemapLayerIndex(),
                new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                mapController.setVisibleBasemapLayerIndex(which);
                dialog.dismiss();
            }
            
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    public void imageButton_openMapMenu_clicked(final View view) {
        openOptionsMenu();
    }

    public void onValidLayerInfos(LayerInfo[] layerInfos) {
        for (int i = layerInfos.length - 1; i >= 0; i--) {
            mapController.addLayer(layerInfos[i]);
        }
    }
    
    public void toggleButton_grid_clicked(final View view) {
        mapController.setGridVisible(((ToggleButton) view).isChecked());
    }
    
    public void northArrowView_clicked(View view) {
        mapController.setRotation(0);
    }

    public void toggleButton_followMe_clicked(final View view) {
        mapController.setAutoPan(((ToggleButton) view).isChecked());
    }

}