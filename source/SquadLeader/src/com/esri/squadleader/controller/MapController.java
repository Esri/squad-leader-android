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

import com.esri.android.map.Grid.GridType;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.MgrsConversionMode;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.MapConfig;
import com.esri.militaryapps.model.MapConfigReader;
import com.esri.squadleader.R;
import com.esri.squadleader.model.BasemapLayer;
import com.esri.squadleader.util.Utilities;

/**
 * A controller for the MapView object used in the application.
 */
public class MapController extends com.esri.militaryapps.controller.MapController {

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
    @SuppressWarnings("serial")
    public MapController(final MapView mapView, AssetManager assetManager) {
        this.mapView = mapView;
        mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mapView && STATUS.INITIALIZED == status) {
                    fireMapReady();
                }
            }
            
        });
        mapView.getGrid().setType(GridType.MGRS);
        mapView.getGrid().setVisibility(false);
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
        addLayer(layer, false);
    }
    
    /**
     * Adds a non-basemap layer to the map.
     * TODO make this method public when overlay layers are implemented
     * @param layer the layer to add to the map.
     * @param isOverlay true if the layer is an overlay that can be turned on and
     *                  off, and false otherwise.
     */
    private void addLayer(Layer layer, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        mapView.addLayer(layer);
        nonBasemapLayers.add(layer);
        fireLayersChanged(isOverlay);
    }
    
    /**
     * Adds a basemap layer to the map.
     * @param basemapLayer the layer to add to the map.
     */
    public void addBasemapLayer(BasemapLayer basemapLayer) {
        addBasemapLayer(basemapLayer, false);
    }
    
    /**
     * Adds a basemap layer to the map.
     * TODO make this method public when overlay layers are implemented
     * @param basemapLayer the layer to add to the map.
     * @param isOverlay true if the layer is an overlay that can be turned on and
     *                  off, and false otherwise.
     */
    private void addBasemapLayer(BasemapLayer basemapLayer, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        mapView.addLayer(basemapLayer.getLayer(), basemapLayers.size());
        basemapLayers.add(basemapLayer);
        if (basemapLayer.getLayer().isVisible()) {
            setVisibleBasemapLayerIndex(basemapLayers.size() - 1);
        }
        fireLayersChanged(isOverlay);
    }
    
    /**
     * Adds a layer to the map based on a LayerInfo object.
     * @param layerInfo the LayerInfo object that specifies the layer to add to the map.
     *        Whether the layer will be a basemap layer or not depends on whether
     *        the LayerInfo object is also of type BasemapLayerInfo.
     */
    public void addLayer(LayerInfo layerInfo) {
        addLayer(layerInfo, false);
    }
    
    /**
     * Adds a layer to the map based on a LayerInfo object.
     * TODO make this method public when overlay layers are implemented
     * @param layerInfo the LayerInfo object that specifies the layer to add to the map.
     *        Whether the layer will be a basemap layer or not depends on whether
     *        the LayerInfo object is also of type BasemapLayerInfo.
     * @param isOverlay true if the layer is an overlay that can be turned on and
     *                  off, and false otherwise.
     */
    private void addLayer(LayerInfo layerInfo, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        Layer layer = createLayer(layerInfo);
        if (null != layer) {
            if (layerInfo instanceof BasemapLayerInfo) {
                BasemapLayer basemapLayer = new BasemapLayer(layer, ((BasemapLayerInfo) layerInfo).getThumbnailUrl());
                addBasemapLayer(basemapLayer, isOverlay);
            } else {
                addLayer(layer, isOverlay);
            }
        }
        //TODO emit an error if layer was null (Exception, Toast, something)
    }

    /**
     * Zooms in on the map, just like calling MapView.zoomIn().
     */
    @Override
    public void zoomIn() {
        mapView.zoomin();
    }

    /**
     * Zooms out on the map, just like calling MapView.zoomOut().
     */
    @Override
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

    @Override
    public void zoom(double factor) {
        mapView.setScale(factor);
    }

    @Override
    public void setRotation(double degrees) {
        mapView.setRotationAngle(degrees);
    }

    @Override
    public double getRotation() {
        return mapView.getRotationAngle();
    }

    @Override
    protected void _zoomToScale(double scale, double centerPointX, double centerPointY) {
        mapView.zoomToScale(new Point(centerPointX, centerPointY), scale);
    }

    @Override
    public int getWidth() {
        return mapView.getWidth();
    }

    @Override
    public int getHeight() {
        return mapView.getHeight();
    }

    @Override
    public void panTo(double centerX, double centerY) {
        panTo(new Point(centerX, centerY));
    }
    
    public void panTo(Point newCenter) {
        mapView.centerAt(newCenter, true);
    }

    /**
     * Pans the map to a new center point, if a valid MGRS string is provided.
     * @param newCenterMgrs the map's new center point, as an MGRS string.
     * @return if the string was valid, the point to which the map was panned; null otherwise
     */
    public Point panTo(String newCenterMgrs) {
        newCenterMgrs = Utilities.convertToValidMgrs(newCenterMgrs,
                toMilitaryGrid(new Point[] {mapView.getMapBoundaryExtent().getCenter()})[0]);
        if (null != newCenterMgrs) {
            Point pt = fromMilitaryGrid(new String[] {newCenterMgrs})[0];
            if (null != pt) {
                panTo(pt);
                return pt;
            } else {
                Log.w(TAG, "MGRS string " + newCenterMgrs + " could not be converted to a point");
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public double[] toMapPoint(int screenX, int screenY) {
        Point pt = mapView.toMapPoint(screenX, screenY);
        return new double[] { pt.getX(), pt.getY() };
    }

    @Override
    public void setGridVisible(boolean visible) {
        mapView.getGrid().setVisibility(visible);
    }

    @Override
    public boolean isGridVisible() {
        return mapView.getGrid().getVisibility();
    }

    /**
     * Converts an array of map points to MGRS strings.
     * @param points the points to convert to MGRS strings.
     * @return an array of MGRS strings corresponding to the input points.
     */
    public String[] toMilitaryGrid(Point[] points) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }

        return sr.toMilitaryGrid(MgrsConversionMode.mgrsAutomatic, 5, false, true, points);
    }
    
    /**
     * Converts an array of MGRS points to map points.
     * @param mgrsStrings the MGRS strings to convert to map points.
     * @return an array of map points in the coordinate system of the map.
     */
    public Point[] fromMilitaryGrid(String[] mgrsStrings) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return sr.fromMilitaryGrid(mgrsStrings, MgrsConversionMode.mgrsAutomatic);
    }

}
