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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Grid.GridType;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.CoordinateConversion.MGRSConversionMode;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.militaryapps.controller.LocationController.LocationMode;
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
    
    private static class LocationChangeHandler extends Handler {
        
        public static final String KEY_MAPX = "mapx";
        public static final String KEY_MAPY = "mapy";
        
        private final WeakReference<MapController> mapControllerRef;
        
        LocationChangeHandler(MapController mapController) {
            mapControllerRef = new WeakReference<MapController>(mapController);
        }
        
        @Override
        public void handleMessage(Message msg) {
            final MapController mapController = mapControllerRef.get();
            Bundle bundle = msg.getData();
            final Point mapPoint = new Point(bundle.getDouble(KEY_MAPX), bundle.getDouble(KEY_MAPY));
            
            if (mapController.isAutoPan()) {
                mapController.panTo(mapPoint);
            }                  
            
            //If we're using the device's location service, we don't need to add the graphic.
            if (LocationMode.SIMULATOR == mapController.getLocationController().getMode()) {
                if (-1 == mapController.locationGraphicId) {
                    mapController.locationGraphicId = mapController.locationGraphicsLayer.addGraphic(
                            new Graphic(mapPoint, mapController.mapView.getLocationService().getSymbol()));
                } else {
                    mapController.locationGraphicsLayer.updateGraphic(mapController.locationGraphicId, mapPoint);
                }
            } else {
                mapController.locationGraphicsLayer.removeAll();
                mapController.locationGraphicId = -1;
            }
        }

    }

    private static final String TAG = MapController.class.getSimpleName();

    private final MapView mapView;
    private final AssetManager assetManager;
    private final List<BasemapLayer> basemapLayers = new ArrayList<BasemapLayer>();
    private final List<Layer> nonBasemapLayers = new ArrayList<Layer>();
    private final GraphicsLayer locationGraphicsLayer = new GraphicsLayer();
    private final LocationChangeHandler locationChangeHandler = new LocationChangeHandler(this);
    private final Object lastLocationLock = new Object(); 
    private AdvancedSymbologyController advancedSymbologyController = null;
    private boolean autoPan = false;
    private int locationGraphicId = -1;
    private Point lastLocation = null;

    /**
     * Creates a new MapController.
     * @param mapView the MapView being controlled by the new MapController.
     */
    @SuppressWarnings("serial")
    public MapController(final MapView mapView, AssetManager assetManager) {
        ((LocationController) getLocationController()).setLocationService(mapView.getLocationService());
        this.mapView = mapView;
        mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mapView && STATUS.INITIALIZED == status) {
                    fireMapReady();
                }
            }
            
        });
        
        mapView.setAllowRotationByPinch(true);
        
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
        reloadMapConfig(true);
    }
    
    private void reloadMapConfig(boolean useExistingPreferences) {
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
        if (useExistingPreferences) {
            try {
                serializedMapConfigStream = context.openFileInput(context.getString(R.string.map_config_prefname));
            } catch (FileNotFoundException e) {
                //Swallow
            }
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
            
            if (0 != mapConfig.getScale()) {
                zoomToScale(mapConfig.getScale(), mapConfig.getCenterX(), mapConfig.getCenterY());
            }
        }
        
        addLayer(locationGraphicsLayer, true);
    }
    
    /**
     * Set a listener that fires when the map is single-tapped. Set to null to remove the current listener.
     * @param listener the listener.
     */
    public void setOnSingleTapListener(OnSingleTapListener listener) {
        if (null != mapView) {
            mapView.setOnSingleTapListener(listener);
        }
    }
    
    @Override
    public void reset() throws ParserConfigurationException, SAXException,
            IOException {
        super.reset();
        removeAllLayers();
        reloadMapConfig(false);
    }
    
    public void removeAllLayers() {
        basemapLayers.clear();
        nonBasemapLayers.clear();
        mapView.removeAll();
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
        getLocationController().pause();
        mapView.pause();
    }

    /**
     * Call unpause() when the activity/application is unpaused, so that the
     * MapView gets unpaused.
     */
    public void unpause() {
        mapView.unpause();
        try {
            getLocationController().unpause();
        } catch (Exception e) {
            Log.d(TAG, "Couldn't unpause LocationController", e);
        }
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
        Log.d(TAG, "panTo " + newCenter.getX() + ", " + newCenter.getY());
        mapView.centerAt(newCenter, true);
    }

    /**
     * Pans the map to a new center point, if a valid MGRS string is provided.
     * @param newCenterMgrs the map's new center point, as an MGRS string.
     * @return if the string was valid, the point to which the map was panned; null otherwise
     */
    public Point panTo(String newCenterMgrs) {
        newCenterMgrs = Utilities.convertToValidMgrs(newCenterMgrs,
                pointToMgrs(mapView.getMapBoundaryExtent().getCenter()));
        if (null != newCenterMgrs) {
            Point pt = mgrsToPoint(newCenterMgrs);
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
     * @param points the points, in map coordinates, to convert to MGRS strings.
     * @return an array of MGRS strings corresponding to the input points.
     * @deprecated use pointsToMgrs instead.
     */
    public String[] toMilitaryGrid(Point[] points) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return toMilitaryGrid(points, sr);
    }
    
    /**
     * Converts an array of points in a known spatial reference to MGRS strings.
     * @param points the points to convert to MGRS strings.
     * @param fromSr the spatial reference of all of the points.
     * @return an array of MGRS strings corresponding to the input points.
     * @deprecated use pointsToMgrs instead.
     */
    public String[] toMilitaryGrid(Point[] points, SpatialReference fromSr) {
        List<String> mgrsStrings = pointsToMgrs(Arrays.asList(points), fromSr);
        return mgrsStrings.toArray(new String[mgrsStrings.size()]);
    }
    
    /**
     * Converts a list of map points to MGRS strings.
     * @param points the points, in map coordinates, to convert to MGRS strings.
     * @return a list of MGRS strings corresponding to the input points.
     */
    public List<String> pointsToMgrs(List<Point> points) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return pointsToMgrs(points, sr);
    }
    
    /**
     * Converts a list of map points to MGRS strings.
     * @param points the points to convert to MGRS strings.
     * @param fromSr the spatial reference of all of the points.
     * @return a list of MGRS strings corresponding to the input points.
     */
    public List<String> pointsToMgrs(List<Point> points, SpatialReference fromSr) {
        return CoordinateConversion.pointsToMgrs(points, fromSr, MGRSConversionMode.AUTO, 5, false, true);
    }
    
    /**
     * Converts a map point to an MGRS string.
     * @param point the point, in map coordinates, to convert to an MGRS string.
     * @return an MGRS string corresponding to the input point.
     */
    public String pointToMgrs(Point point) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return pointToMgrs(point, sr);
    }
    
    /**
     * Converts a map point to an MGRS string.
     * @param point the point to convert to an MGRS string.
     * @param fromSr the spatial reference of the point.
     * @return an MGRS string corresponding to the input point.
     */
    public String pointToMgrs(Point point, SpatialReference fromSr) {
        return CoordinateConversion.pointToMgrs(point, fromSr, MGRSConversionMode.AUTO, 5, false, true);
    }

    @Override
    public String pointToMgrs(double x, double y, int wkid) {
        return pointToMgrs(new Point(x, y), SpatialReference.create(wkid));
    }

    /**
     * Converts an array of MGRS strings to map points.
     * @param mgrsStrings the MGRS strings to convert to map points.
     * @return an array of map points in the coordinate system of the map.
     * @deprecated use mgrsToPoints instead.
     */
    public Point[] fromMilitaryGrid(String[] mgrsStrings) {
        List<Point> pointsList = mgrsToPoints(Arrays.asList(mgrsStrings));
        return pointsList.toArray(new Point[pointsList.size()]);
    }
    
    /**
     * Converts a list of MGRS strings to map points.
     * @param mgrsStrings the MGRS strings to convert to map points.
     * @return a list of map points in the coordinate system of the map.
     */
    public List<Point> mgrsToPoints(List<String> mgrsStrings) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return CoordinateConversion.mgrsToPoints(mgrsStrings, sr, MGRSConversionMode.AUTO);
    }
    
    /**
     * Converts an MGRS string to a map point.
     * @param mgrsString the MGRS string to convert to a map point.
     * @return a map point in the coordinate system of the map.
     */
    public Point mgrsToPoint(String mgrsString) {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return CoordinateConversion.mgrsToPoint(mgrsString, sr, MGRSConversionMode.AUTO);
    }

    @Override
    public void onLocationChanged(com.esri.militaryapps.model.Location location) {
        if (null != location) {
            final Point mapPoint = GeometryEngine.project(location.getLongitude(), location.getLatitude(), mapView.getSpatialReference());
            Bundle bundle = new Bundle();
            bundle.putDouble(LocationChangeHandler.KEY_MAPX, mapPoint.getX());
            bundle.putDouble(LocationChangeHandler.KEY_MAPY, mapPoint.getY());
            Message msg = new Message();
            msg.setData(bundle);
            locationChangeHandler.sendMessage(msg);
            new Thread() {
                public void run() {
                    synchronized (lastLocationLock) {
                        lastLocation = mapPoint;
                    }
                };
            }.start();
        }
    }

    @Override
    protected LocationController createLocationController() {
        try {
            return new LocationController(LocationMode.SIMULATOR, true);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't instantiate LocationController", e);
            return null;
        }
    }

    @Override
    public void setAutoPan(boolean autoPan) {
        if (autoPan) {
            synchronized (lastLocationLock) {
                if (null != lastLocation) {
                    panTo(lastLocation);
                }
            }
        }
        this.autoPan = autoPan;
        if (null != mapView.getLocationService()) {
            mapView.getLocationService().setAutoPan(autoPan);
        }
    }

    @Override
    public boolean isAutoPan() {
        return autoPan;
    }
    
    /**
     * Returns the spatial reference of the MapView that this controller controls.
     * @return the spatial reference of the MapView that this controller controls.
     */
    public SpatialReference getSpatialReference() {
        return mapView.getSpatialReference();
    }

}
