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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageGroupLayer;
import com.esri.core.symbol.advanced.MessageHelper;
import com.esri.core.symbol.advanced.SymbolDictionary;
import com.esri.militaryapps.model.Geomessage;
import com.esri.squadleader.util.Utilities;

/**
 * A controller for ArcGIS Runtime advanced symbology. Use this class when you want to use
 * MessageGroupLayer, MessageProcessor, SymbolDictionary, and MIL-STD-2525C symbols.
 */
public class AdvancedSymbolController {
    
    private static final String TAG = AdvancedSymbolController.class.getSimpleName();
    private static final SpatialReference WGS1984 = SpatialReference.create(4326);

    private final MapController mapController;
    private final MessageGroupLayer groupLayer;
    private final GraphicsLayer spotReportLayer;
    private final String[] messageTypesSupportedSorted;
    private final HashSet<String> highlightedIds = new HashSet<String>();
    private final HashMap<String, Integer> spotReportIdToGraphicId = new HashMap<String, Integer>();
    private final Symbol spotReportSymbol;

    /**
     * Creates a new AdvancedSymbolController.
     * @param mapController the application's MapController.
     * @param assetManager the application's AssetManager, from which the advanced symbology database
     *                     will be copied.
     * @param symbolDictionaryDirname the name of the asset directory that contains the advanced symbology
     *                                database.
     * @param spotReportIcon the Drawable for putting spot reports on the map.
     * @throws FileNotFoundException if the advanced symbology database is absent or corrupt.
     */
    public AdvancedSymbolController(
            MapController mapController,
            AssetManager assetManager,
            String symbolDictionaryDirname,
            Drawable spotReportIcon) throws FileNotFoundException {
        this.mapController = mapController;
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File symDictDir = new File(downloadsDir, symbolDictionaryDirname);
        if (!symDictDir.exists()) {
            try {
                Utilities.copyAssetToDir(assetManager, symbolDictionaryDirname, downloadsDir.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Could not copy symbol dictionary directory to " + symDictDir.getAbsolutePath(), e);
            }
        }
        
        spotReportLayer = new GraphicsLayer();
        mapController.addLayer(spotReportLayer);
        
        groupLayer = new MessageGroupLayer(SymbolDictionary.DictionaryType.MIL2525C, symDictDir.getAbsolutePath());
        mapController.addLayer(groupLayer);
        
        messageTypesSupportedSorted = groupLayer.getMessageProcessor().getMessageTypesSupported();
        Arrays.sort(messageTypesSupportedSorted);
        
        spotReportSymbol = new PictureMarkerSymbol(spotReportIcon);
    }
    
    /**
     * Adds a Geomessage, displaying it on the map with which this controller was created.
     * @param geomessage the Geomessage to display.
     */
    public void addGeomessage(Geomessage geomessage) {
        if ("spotrep".equals(geomessage.getProperty(Geomessage.TYPE_FIELD_NAME))
                || "spot_report".equals(geomessage.getProperty(Geomessage.TYPE_FIELD_NAME))) {
            //Use a single symbol for all spot reports
            String controlPointsString = (String) geomessage.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME);
            if (null != controlPointsString) {
                StringTokenizer tok = new StringTokenizer(controlPointsString, ",");
                if (2 == tok.countTokens()) {
                    try {
                        Geometry pt = new Point(Double.parseDouble(tok.nextToken()), Double.parseDouble(tok.nextToken()));
                        final int wkid = Integer.parseInt((String) geomessage.getProperty(Geomessage.WKID_FIELD_NAME));
                        if (null != mapController.getSpatialReference() && wkid != mapController.getSpatialReference().getID()) {
                            pt = GeometryEngine.project(pt, SpatialReference.create(wkid), mapController.getSpatialReference());
                        }
                        Integer graphicId = spotReportIdToGraphicId.get(geomessage.getId());
                        if (null != graphicId) {
                            spotReportLayer.updateGraphic(graphicId, pt);
                            spotReportLayer.updateGraphic(graphicId, geomessage.getProperties());
                        } else {
                            Graphic graphic = new Graphic(pt, spotReportSymbol, geomessage.getProperties());
                            graphicId = spotReportLayer.addGraphic(graphic);
                            spotReportIdToGraphicId.put(geomessage.getId(), graphicId);
                        }
                    } catch (NumberFormatException nfe) {
                        Log.e(TAG, "Could not parse spot report", nfe);
                    }
                }
            }
        } else {
            //Let the MessageProcessor handle other types of reports
            String action = (String) geomessage.getProperty(Geomessage.ACTION_FIELD_NAME);
            Message message;
            if (MessageHelper.MESSAGE_ACTION_VALUE_HIGHLIGHT.equalsIgnoreCase(action)) {
                message = MessageHelper.create2525CHighlightMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                        true);
            } else if (MessageHelper.MESSAGE_ACTION_VALUE_UNHIGHLIGHT.equalsIgnoreCase(action)) {
                message = MessageHelper.create2525CHighlightMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                        false);
            } else if (MessageHelper.MESSAGE_ACTION_VALUE_REMOVE.equalsIgnoreCase(action)) {
                message = MessageHelper.create2525CRemoveMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME));
            } else {
                message = MessageHelper.create2525CUpdateMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                        true);
                message.setProperties(geomessage.getProperties());
                message.setID(geomessage.getId());
            }
            
            //Translate from an AFM message type name to an ArcGIS Runtime for Android message type name
            String messageType = (String) message.getProperty(Geomessage.TYPE_FIELD_NAME);
            if (0 > Arrays.binarySearch(messageTypesSupportedSorted, messageType)) {
                if ("trackrep".equals(messageType)) {
                    message.setProperty(Geomessage.TYPE_FIELD_NAME, "track_report");
                }
            }
            
            //Translate from an AFM color string to an ArcGIS Runtime for Android color string
            if ("chemlight".equals(message.getProperty(Geomessage.TYPE_FIELD_NAME))) {
                String colorString = (String) message.getProperty("color");
                if (null == colorString) {
                    colorString = (String) message.getProperty("chemlight");
                }
                if ("1".equals(colorString)) {
                    colorString = "red";
                } else if ("2".equals(colorString)) {
                    colorString = "green";
                } else if ("3".equals(colorString)) {
                    colorString = "blue";
                } else if ("4".equals(colorString)) {
                    colorString = "yellow";
                }
                if (null != colorString) {
                    message.setProperty("chemlight", colorString);
                }
            }
            
            //Workaround for https://github.com/Esri/squad-leader-android/issues/63
            //TODO remove this workaround when the issue is fixed in ArcGIS Runtime
            if (message.getProperties().containsKey("datetimevalid")) {
                if (!message.getProperties().containsKey("z")) {
                    message.setProperty("z", "0");
                }
                String controlPoints = (String) message.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME);
                if (null != controlPoints) {
                    StringTokenizer tok = new StringTokenizer(controlPoints, ",; ");
                    if (2 <= tok.countTokens()) {
                        try {
                            Double x = Double.parseDouble(tok.nextToken());
                            Double y = Double.parseDouble(tok.nextToken());
                            String wkid = (String) message.getProperty(Geomessage.WKID_FIELD_NAME);
                            if (null != wkid) {
                                Point projectedPoint = (Point) GeometryEngine.project(
                                        new Point(x, y),
                                        SpatialReference.create(Integer.parseInt(wkid)),
                                        WGS1984);
                                x = projectedPoint.getX();
                                y = projectedPoint.getY();
                            }
                            message.setProperty("x", x);
                            message.setProperty("y", y);
                        } catch (NumberFormatException nfe) {
                            Log.e(TAG, "_control_points or WKID NumberFormatException", nfe);
                        }
                    }
                }
            }
            
            groupLayer.getMessageProcessor().processMessage(message);
            
            boolean needToHighlight = false;
            boolean needToUnhighlight = false;
            boolean previouslyHighlighted = highlightedIds.contains(geomessage.getId());
            boolean nowHighlighted = "1".equals(geomessage.getProperty("status911"));
            if (previouslyHighlighted) {
                needToUnhighlight = !nowHighlighted;
            } else {
                needToHighlight = nowHighlighted;
            }
            if (needToHighlight || needToUnhighlight) {
                message = MessageHelper.create2525CHighlightMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                        needToHighlight);
                groupLayer.getMessageProcessor().processMessage(message);
                if (needToHighlight) {
                    highlightedIds.add(geomessage.getId());
                } else {
                    highlightedIds.remove(geomessage.getId());
                }
            }
        }
    }
    
}
