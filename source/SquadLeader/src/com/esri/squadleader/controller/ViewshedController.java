/*******************************************************************************
 * Copyright 2015 Esri
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

import java.io.FileNotFoundException;

import android.graphics.Color;

import com.esri.android.map.RasterLayer;
import com.esri.core.analysis.Viewshed;
import com.esri.core.geometry.Point;
import com.esri.core.renderer.Colormap;
import com.esri.core.renderer.Colormap.UniqueValue;
import com.esri.core.renderer.ColormapRenderer;

/**
 * A controller that calculates viewsheds based on an elevation raster.
 */
public class ViewshedController {
    
    private static final String TAG = ViewshedController.class.getSimpleName();
    
    private final MapController mapController;
    private final String elevationFilename;
    
    private Viewshed viewshed;
    private RasterLayer layer;    
    private double observerHeight = 2.0;
    private boolean started = false;

    /**
     * Instantiates a ViewshedController with an immutable elevation raster dataset. The caller does not need to call
     * start() in this case.
     * @param elevationFilename the full path to the elevation dataset to be used for viewshed analysis.
     *        This could be a TIF file, for example. The dataset should be in the same spatial reference
     *        as the MapView.
     * @param mapController the MapController to which the viewshed layer will be added. Note that this class does not
     *        add the layer to the map, but this class removes the layer from the map when stop() is called.
     * @throws RuntimeException if the elevation raster could not be opened and used for viewshed analysis.
     * @throws FileNotFoundException if elevationFilename represents a file that does not exist.
     * @throws IllegalArgumentException if elevationFilename is null or an empty string.
     */
    public ViewshedController(String elevationFilename, MapController mapController) throws IllegalArgumentException, FileNotFoundException, RuntimeException {
        this.elevationFilename = elevationFilename;
        this.mapController = mapController;
        start();
    }
    
    /**
     * Gets the viewshed analysis and layer ready but does not add the layer to the map. The caller should add the
     * layer to the map after this method returns.
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     * @throws RuntimeException
     */
    public void start() throws IllegalArgumentException, FileNotFoundException, RuntimeException {
        if (!started) {
            viewshed = new Viewshed(elevationFilename);
            layer = new RasterLayer(viewshed.getOutputFunctionRasterSource());
            layer.setVisible(false);
            Colormap colormap = new Colormap();
            colormap.addUniqueValue(new UniqueValue(1, Color.rgb(76, 230, 0), "Visible"));
            ColormapRenderer renderer = new ColormapRenderer(colormap);
            layer.setRenderer(renderer);
            started = true;
        }
    }
    
    /**
     * Finalizes the controller by disposing the private Viewshed object and removing the viewshed layer from the map.
     */
    public void stop() {
        started = false;
        if (null != layer) {
            mapController.removeLayer(layer);
        }
        if (null != viewshed) {
            viewshed.dispose();
        }        
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }
    
    /**
     * Returns the layer that displays viewshed results.
     * @return the layer that displays viewshed results.
     */
    public RasterLayer getLayer() {
        return layer;
    }
    
    /**
     * Returns the observer height used for viewshed analysis.
     * @return the observer height used for viewshed analysis.
     */
    public double getObserverHeight() {
        return observerHeight;
    }
    
    /**
     * Sets the observer height used for viewshed analysis.
     * @param observerHeight the observer height used for viewshed analysis.
     */
    public void setObserverHeight(double observerHeight) {
        this.observerHeight = observerHeight;
        if (null != viewshed) {
            viewshed.setObserverZOffset(observerHeight);
        }
    }
    
    /**
     * Calculates the viewshed based on an observer point.
     * @param observer the observer from which the viewshed will be calculated. This point must be
     *        in the spatial reference of the underlying elevation dataset, which should be in the
     *        same spatial reference as the MapView.
     */
    public void calculateViewshed(Point observer) {
        viewshed.setObserver(observer);
        layer.setVisible(true);
    }

}
