/*******************************************************************************
 * Copyright 2016 Esri
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
package com.esri.squadleader.model;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.RasterLayer;
import com.esri.core.geodatabase.Geopackage;
import com.esri.core.geodatabase.GeopackageFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.raster.RasterSource;
import com.esri.core.renderer.RGBRenderer;
import com.esri.core.renderer.RasterRenderer;
import com.esri.core.renderer.Renderer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Opens OGC GeoPackages and gets their data and layers.
 */
public class GeoPackageReader {

    private static final RGBRenderer RGB_RENDERER = new RGBRenderer();

    private static GeoPackageReader instance = new GeoPackageReader();

    public static GeoPackageReader getInstance() {
        return instance;
    }

    private final HashSet<Geopackage> geopackages = new HashSet<Geopackage>();
    private final HashSet<RasterSource> rasterSources = new HashSet<RasterSource>();

    private GeoPackageReader() {

    }

    @Override
    protected void finalize() throws Throwable {
        for (Geopackage gpkg : geopackages) {
            gpkg.dispose();
        }
        geopackages.clear();
        for (RasterSource src : rasterSources) {
            src.dispose();
        }
        rasterSources.clear();

        super.finalize();
    }

    /**
     * Reads the tables in a GeoPackage, makes a layer from each table, and returns a list containing
     * those layers.
     * @param gpkgPath the full path to the .gpkg file.
     * @param sr the spatial reference to which any raster layers should be projected, typically the
     *           spatial reference of your map.
     * @param rasterRenderer the renderer to be used for raster layers. One simple option is an RGBRenderer.
     * @param markerRenderer the renderer to be used for point layers.
     * @param lineRenderer the renderer to be used for polyline layers.
     * @param fillRenderer the renderer to be used for polygon layers.
     * @return a list of the layers created for all tables in the GeoPackage.
     * @throws FileNotFoundException if gpkgPath points to a file that does not exist <b>or cannot be
     * seen because the app has not requested READ_EXTERNAL_STORAGE or WRITE_EXTERNAL_STORAGE permission</b>.
     */
    public List<Layer> readGeoPackageToLayerList(String gpkgPath,
                                                 SpatialReference sr,
                                                 RasterRenderer rasterRenderer,
                                                 Renderer markerRenderer,
                                                 Renderer lineRenderer,
                                                 Renderer fillRenderer) throws FileNotFoundException {
        List<Layer> layers = new ArrayList<Layer>();

        // Raster layers
        FileRasterSource src = new FileRasterSource(gpkgPath);
        rasterSources.add(src);
        if (null != sr) {
            src.project(sr);
        }
        RasterLayer rasterLayer = new RasterLayer(src);
        rasterLayer.setRenderer(RGB_RENDERER);
        rasterLayer.setName((gpkgPath.contains("/") ? gpkgPath.substring(gpkgPath.lastIndexOf("/") + 1) : gpkgPath) + " (raster)");
        layers.add(rasterLayer);

        // Vector layers
        Geopackage gpkg = new Geopackage(gpkgPath);
        geopackages.add(gpkg);
        List<GeopackageFeatureTable> tables = gpkg.getGeopackageFeatureTables();

        //First pass: polygons and unknowns
        HashSet<Geometry.Type> types = new HashSet<Geometry.Type>();
        types.add(Geometry.Type.ENVELOPE);
        types.add(Geometry.Type.POLYGON);
        types.add(Geometry.Type.UNKNOWN);
        layers.addAll(getTablesAsLayers(tables, types, fillRenderer));

        //Second pass: lines
        types.clear();
        types.add(Geometry.Type.LINE);
        types.add(Geometry.Type.POLYLINE);
        layers.addAll(getTablesAsLayers(tables, types, lineRenderer));

        //Third pass: points
        types.clear();
        types.add(Geometry.Type.MULTIPOINT);
        types.add(Geometry.Type.POINT);
        layers.addAll(getTablesAsLayers(tables, types, markerRenderer));

        return layers;
    }

    private static List<Layer> getTablesAsLayers(List<GeopackageFeatureTable> tables, Set<Geometry.Type> types, Renderer renderer) {
        List<Layer> layers = new ArrayList<Layer>(tables.size());
        for (GeopackageFeatureTable table : tables) {
            if (types.contains(table.getGeometryType())) {
                final FeatureLayer layer = new FeatureLayer(table);
                layer.setRenderer(renderer);
                layer.setName(table.getTableName());
                layers.add(layer);
            }
        }
        return layers;
    }

}
