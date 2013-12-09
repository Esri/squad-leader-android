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

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

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

    private final MessageGroupLayer groupLayer;

    /**
     * Creates a new AdvancedSymbolController.
     * @param mapController the application's MapController.
     * @param assetManager the application's AssetManager, from which the advanced symbology database
     *                     will be copied.
     * @param symbolDictionaryDirname the name of the asset directory that contains the advanced symbology
     *                                database.
     * @throws FileNotFoundException if the advanced symbology database is absent or corrupt.
     */
    public AdvancedSymbolController(
            MapController mapController,
            AssetManager assetManager,
            String symbolDictionaryDirname) throws FileNotFoundException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File symDictDir = new File(downloadsDir, symbolDictionaryDirname);
        if (!symDictDir.exists()) {
            try {
                Utilities.copyAssetToDir(assetManager, symbolDictionaryDirname, downloadsDir.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Could not copy symbol dictionary directory to " + symDictDir.getAbsolutePath(), e);
            }
        }
        
        groupLayer = new MessageGroupLayer(SymbolDictionary.DictionaryType.MIL2525C, symDictDir.getAbsolutePath());
        mapController.addLayer(groupLayer);
    }
    
    /**
     * Adds a Geomessage, displaying it on the map with which this controller was created.
     * @param geomessage the Geomessage to display.
     */
    public void addGeomessage(Geomessage geomessage) {
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
        }
        message.setProperties(geomessage.getProperties());
        groupLayer.getMessageProcessor().processMessage(message);
    }
    
}
