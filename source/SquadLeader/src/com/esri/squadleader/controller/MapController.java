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
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
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
    private final AssetManager assetManager;
    private final List<BasemapLayer> basemapLayers = new ArrayList<BasemapLayer>();
    private final List<Layer> nonBasemapLayers = new ArrayList<Layer>();
    private AdvancedSymbologyController advancedSymbologyController = null;

    /**
     * Creates a new MapController.
     * @param mapView the MapView being controlled by the new MapController.
     */
    public MapController(MapView mapView, AssetManager assetManager) {
        this.mapView = mapView;
        this.assetManager = assetManager;
        reloadMapConfig();
    }
    
    /**
     * Loads a map configuration using one of the following approaches, trying each approach in order
     * until one of them works.
     * <ol>
     * <li>Check for existing user preferences and use those.</li>
     * <li>Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.</li>
     * <li>Use mapconfig.xml built into the app.</li>
     * </ol>
     */
    public void reloadMapConfig() {
        mapView.removeAll();
        
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
                    addBasemapLayer(new BasemapLayer(layer, layerInfo.getThumbnailUrl()));
                }
            }
            
            for (LayerInfo layerInfo : mapConfig.getNonBasemapLayers()) {
                Layer layer = createLayer(layerInfo);
                if (null != layer) {
                    addLayer(layer);
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
    
    public int getVisibleBasemapLayerIndex() {
        for (int i = 0; i < basemapLayers.size(); i++) {
            if (basemapLayers.get(i).getLayer().isVisible()) {
                return i;
            }
        }
        return -1;
    }
    
    public void setVisibleBasemapLayerIndex(final int index) {
        int oldIndex = getVisibleBasemapLayerIndex();
        if (index != oldIndex) {
            basemapLayers.get(index).getLayer().setVisible(true);
            basemapLayers.get(oldIndex).getLayer().setVisible(false);
        }
    }
    
    public List<Layer> getNonBasemapLayers() {
        return nonBasemapLayers;
    }
    
    public Context getContext() {
        return mapView.getContext();
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
            case FEATURE_SERVICE: {
                layer = new ArcGISFeatureLayer(layerInfo.getDatasetPath(), MODE.ONDEMAND);
                break;
            }
            case IMAGE_SERVICE: {
                layer = new ArcGISImageServiceLayer(layerInfo.getDatasetPath(), null);
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
     * Adds a non-basemap layer to the map.
     * @param layer the layer to add to the map.
     */
    public void addLayer(Layer layer) {
        mapView.addLayer(layer);
        nonBasemapLayers.add(layer);
    }
    
    /**
     * Adds a basemap layer to the map.
     * @param basemapLayer the layer to add to the map.
     */
    public void addBasemapLayer(BasemapLayer basemapLayer) {
        mapView.addLayer(basemapLayer.getLayer(), basemapLayers.size());
        basemapLayers.add(basemapLayer);
        if (basemapLayer.getLayer().isVisible()) {
            setVisibleBasemapLayerIndex(basemapLayers.size() - 1);
        }
    }
    
    /**
     * Adds a layer to the map based on a LayerInfo object.
     * @param layerInfo the LayerInfo object that specifies the layer to add to the map.
     *        Whether the layer will be a basemap layer or not depends on whether
     *        the LayerInfo object is also of type BasemapLayerInfo.
     */
    public void addLayer(LayerInfo layerInfo) {
        Layer layer = createLayer(layerInfo);
        if (null != layer) {
            if (layerInfo instanceof BasemapLayerInfo) {
                BasemapLayer basemapLayer = new BasemapLayer(layer, ((BasemapLayerInfo) layerInfo).getThumbnailUrl());
                addBasemapLayer(basemapLayer);
            } else {
                addLayer(layer);
            }
        }
        //TODO emit an error if layer was null (Exception, Toast, something)
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
