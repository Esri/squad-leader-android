package com.esri.squadleader.controller;

import java.io.FileNotFoundException;

import com.esri.android.map.RasterLayer;
import com.esri.core.analysis.Viewshed;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.raster.FunctionRasterSource;

public class ViewshedController {
    
    private final Viewshed viewshed;
    private final RasterLayer layer;
    
    private double observerHeight = 2.0;

    /**
     * Instantiates a ViewshedController with an immutable elevation raster dataset.
     * @param elevationFilename the full path to the elevation dataset to be used for viewshed analysis.
     *        This could be a TIF file, for example. The dataset should be in the same spatial reference
     *        as the MapView.
     * @throws RuntimeException if the elevation raster could not be opened and used for viewshed analysis.
     * @throws FileNotFoundException if elevationFilename represents a file that does not exist.
     * @throws IllegalArgumentException if elevationFilename is null or an empty string.
     */
    public ViewshedController(String elevationFilename) throws IllegalArgumentException, FileNotFoundException, RuntimeException {
        viewshed = new Viewshed(elevationFilename);
        layer = new RasterLayer(viewshed.getOutputFunctionRasterSource());
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (null != viewshed) {
            viewshed.dispose();
        }
    }
    
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
    }

}
