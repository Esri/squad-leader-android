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

import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;

/**
 * A controller for the MapView object used in the application.
 */
public class MapController {

    private static final String TAG = MapController.class.getSimpleName();

    private final MapView mapView;
    private AdvancedSymbologyController advancedSymbologyController = null;

    /**
     * Creates a new MapController.
     * @param mapView the MapView being controlled by the new MapController.
     */
    public MapController(MapView mapView) {        
        this.mapView = mapView;
        
        String tpkPath = "file:" + Environment.getExternalStorageDirectory() + "/SquadLeader/data/Topographic.tpk";
        Log.i(TAG, "Loading local tiled layer from " + tpkPath);
        Layer basemapLayer = new ArcGISLocalTiledLayer(tpkPath);
        mapView.addLayer(basemapLayer);
        
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
