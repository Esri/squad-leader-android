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
package com.esri.squadleader.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.MotionEvent;

import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.MapConfig;
import com.esri.militaryapps.model.MapConfigReader;
import com.esri.squadleader.R;
import com.esri.squadleader.model.BasemapLayer;

/**
 * A controller for the MapView object used in the application.
 */
public class MapController {

    private static final String TAG = MapController.class.getSimpleName();

    private final MapView mapView;
    private final List<BasemapLayer> basemapLayers = new ArrayList<BasemapLayer>();
    private final List<Layer> nonBasemapLayers = new ArrayList<Layer>();
    private AdvancedSymbologyController advancedSymbologyController = null;

    /**
     * Creates a new MapController.
     * @param mapView the MapView being controlled by the new MapController.
     */
    public MapController(MapView mapView, AssetManager assetManager) {
        this.mapView = mapView;
        
        /**
         * Load a map configuration using one of these approaches. Try the first on the list and try each
         * approach until one of them works.
         * - Check for existing user preferences and use those.
         * - Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.
         * - Use mapconfig.xml built into the app
         */
        MapConfig mapConfig = null;
        Context context = mapView.getContext();
        FileInputStream serializedMapConfigStream = null;
        try {
            serializedMapConfigStream = context.openFileInput(context.getString(R.string.map_config_prefname));
        } catch (FileNotFoundException e) {
            //Swallow
        }
        if (null != serializedMapConfigStream) {
            Log.d(TAG, "Loading mapConfig previously saved on device");
            try {
                mapConfig = (MapConfig) new ObjectInputStream(serializedMapConfigStream).readObject();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Couldn't load the class for deserialized object", e);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't deserialize object", e);
            }
        }
        if (null == mapConfig) {
            //Read mapconfig from the SD card
            InputStream mapConfigInputStream = null;
            File mapConfigFile = new File(
                    context.getString(R.string.squad_leader_home_dir),
                    context.getString(R.string.map_config_filename));
            if (mapConfigFile.exists() && mapConfigFile.isFile()) {
                Log.d(TAG, "Loading mapConfig from " + mapConfigFile.getAbsolutePath());
                try {
                    mapConfigInputStream = new FileInputStream(mapConfigFile);
                } catch (FileNotFoundException e) {
                    //Swallow and let it load built-in mapconfig.xml
                }
            }
            if (null == mapConfigInputStream) {
                Log.d(TAG, "Loading mapConfig from app's " + context.getString(R.string.map_config_filename) + " asset");
                try {
                    mapConfigInputStream = assetManager.open(context.getString(R.string.map_config_filename));
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't load any " + context.getString(R.string.map_config_filename) + ", including the one built into the app", e);
                }
            }
            try {
                mapConfig = MapConfigReader.readMapConfig(mapConfigInputStream);
                if (null != mapConfig) {
                    //Write mapConfig to preferences
                    FileOutputStream out = context.openFileOutput(context.getString(R.string.map_config_prefname), Context.MODE_PRIVATE);
                    new ObjectOutputStream(out).writeObject(mapConfig);
                    out.close();
                } else {
                    Log.e(TAG, "Read MapConfig from stream but it came back null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Couldn't read MapConfig", e);
            }
        }
        if (null != mapConfig) {
            //Load map layers from mapConfig
            for (BasemapLayerInfo layerInfo : mapConfig.getBasemapLayers()) {
                Layer layer = createLayer(layerInfo);
                if (null != layer) {
                    addLayer(layer);
                    BasemapLayer basemapLayer = new BasemapLayer(layer, layerInfo.getThumbnailUrl());
                    basemapLayers.add(basemapLayer);
                }
            }
            
            for (LayerInfo layerInfo : mapConfig.getNonBasemapLayers()) {
                Layer layer = createLayer(layerInfo);
                if (null != layer) {
                    addLayer(layer);
                    nonBasemapLayers.add(layer);
                }
            }
        }
        
        /******************************************************************************************
         * TODO this is test code
         */
        mapView.setOnTouchListener(new MapOnTouchListener(mapView.getContext(), mapView) {
            
            @Override
            public boolean onSingleTap(MotionEvent event) {
                Log.i(TAG, "Touch!");
                if (null != advancedSymbologyController) {
                    advancedSymbologyController.addMessage(MapController.this.mapView.toMapPoint(event.getX(), event.getY()));
                    return true;
                } else {
                    return false;
                }
            }
            
        });
        /*****************************************************************************************/
    }
    
    public List<BasemapLayer> getBasemapLayers() {
        return basemapLayers;
    }
    
    private Layer createLayer(LayerInfo layerInfo) {
        Layer layer = null;
        switch (layerInfo.getLayerType()) {
            case TILED_MAP_SERVICE: {
                layer = new ArcGISTiledMapServiceLayer(layerInfo.getDatasetPath());
                break;
            }
            case DYNAMIC_MAP_SERVICE: {
                layer = new ArcGISDynamicMapServiceLayer(layerInfo.getDatasetPath());
                break;
            }
            case TILED_CACHE: {
                layer = new ArcGISLocalTiledLayer(layerInfo.getDatasetPath());
                break;
            }
            case MIL2525C_MESSAGE: {
                Log.i(TAG, "MIL-STD-2525C message layers will be supported in a future version of Squad Leader (TODO implement).");
                break;
            }
            default: {
                Log.i(TAG, "Layer " + layerInfo.getName() + " is of a type not yet implemented in ArcGIS Runtime for Android.");
            }
        }
        if (null != layer) {
            layer.setName(layerInfo.getName());
            layer.setVisible(layerInfo.isVisible());
        }
        return layer;
    }
    
    /**
     * Sets the AdvancedSymbologyController that this MapController will use for advanced symbology
     * (e.g. 2525C). Setting this controller is optional, but advanced symbology will not work if you
     * do not set this controller.
     * @param controller the AdvancedSymbologyController that this MapController will use for advanced
     *                   symbology.
     */
    public void setAdvancedSymbologyController(AdvancedSymbologyController controller) {
        this.advancedSymbologyController = controller;
    }

    /**
     * Adds a layer to the map, just like calling MapView.addLayer(Layer).
     * @param layer the layer to add to the map.
     */
    public void addLayer(Layer layer) {
        mapView.addLayer(layer);
    }

    /**
     * Zooms in on the map, just like calling MapView.zoomIn().
     */
    public void zoomIn() {
        mapView.zoomin();
    }

    /**
     * Zooms out on the map, just like calling MapView.zoomOut().
     */
    public void zoomOut() {
        mapView.zoomout();
    }

    /**
     * Call pause() when the activity/application is paused, so that the MapView
     * gets paused.
     */
    public void pause() {
        mapView.pause();
    }

    /**
     * Call unpause() when the activity/application is unpaused, so that the
     * MapView gets unpaused.
     */
    public void unpause() {
        mapView.unpause();
    }

}
