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
package com.esri.squadleader.controller.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.esri.android.map.MapView;
import com.esri.squadleader.controller.MapController;
import com.esri.squadleader.util.Utilities;
import com.esri.squadleader.view.SquadLeaderActivity;

public class MapControllerTest extends ActivityInstrumentationTestCase2<SquadLeaderActivity> {
    
    private static final String TAG = MapControllerTest.class.getSimpleName();
    
    private SquadLeaderActivity activity;
    private MapController mapController;
            
    public MapControllerTest() {
        super(SquadLeaderActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        mapController = activity.getMapController();
    }
    
    /**
     * MapConfig loading tests
     * 
     * Here's the priority that Squad Leader should use:
     * 1. Check for existing user preferences and use those.
     * 2. Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.
     * 3. Use mapconfig.xml built into the app
     * 
     * To make this easier, test #2 first. That should write preferences. Then delete
     * /mnt/sdcard/SquadLeader/mapconfig.xml and test #1. Then delete
     * user preferences and test #3.
     * @throws IOException 
     */
        
    @Test
    public void test001LoadMapConfigFromSdCardFile() throws IOException {
        clearExistingPreferences(mapController.getContext());
        
        //Create mapconfig.xml on SD card
        File originalMapConfigOnSdCard = new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename));
        if (originalMapConfigOnSdCard.exists()) {
            //Back it up
            originalMapConfigOnSdCard.renameTo(new File(originalMapConfigOnSdCard.getAbsolutePath() + ".bak"));
        }
        Utilities.copyAssetToDir(getInstrumentation().getContext().getAssets(),
                activity.getString(com.esri.squadleader.R.string.map_config_filename),
                activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir));
        
        //Load it
        reloadMapController();
        runTestsAgainstTestMapConfig();
    }
    
    @Test
    public void test002LoadMapConfigFromProfile() {
        //Delete mapconfig.xml on SD card
        new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename)).delete();
        
        //Load it from preferences file
        reloadMapController();
        runTestsAgainstTestMapConfig();
    }
    
    @Test
    public void test003LoadMapConfigFromAppAsset() {
        clearExistingPreferences(mapController.getContext());
        
        reloadMapController();
        assertEquals(0, mapController.getNonBasemapLayers().size());
        assertEquals(1, mapController.getBasemapLayers().size());
        assertEquals("Scanned", mapController.getBasemapLayers().get(0).getLayer().getName());
        assertEquals("http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer",
                mapController.getBasemapLayers().get(0).getLayer().getUrl());
        
        //Now that tests are done, restore original MapConfig if any
        File originalMapConfigOnSdCard = new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename) + ".bak");
        if (originalMapConfigOnSdCard.exists()) {
            String filename = originalMapConfigOnSdCard.getAbsolutePath();
            if (filename.endsWith(".bak")) {
                originalMapConfigOnSdCard.renameTo(new File(filename.substring(0, filename.length() - ".bak".length())));
            }
        }
    }
    
    private void reloadMapController() {
        mapController = new MapController((MapView) activity.findViewById(com.esri.squadleader.R.id.map), activity.getAssets());
    }
    
    private void clearExistingPreferences(Context context) {
        context.deleteFile(context.getString(com.esri.squadleader.R.string.map_config_prefname));
    }
    
    private void runTestsAgainstTestMapConfig() {
        assertEquals(0, mapController.getNonBasemapLayers().size());
        assertEquals(1, mapController.getBasemapLayers().size());
        assertEquals("Test Layer", mapController.getBasemapLayers().get(0).getLayer().getName());
        assertEquals("http://my/fake/URL", mapController.getBasemapLayers().get(0).getLayer().getUrl());
    }
    
    @Override
    protected void tearDown() throws Exception {
        activity.finish();
    }

}
