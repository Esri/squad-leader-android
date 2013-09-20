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
package com.esri.squadleader.view;

import java.io.FileNotFoundException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.esri.android.map.MapView;
import com.esri.squadleader.R;
import com.esri.squadleader.controller.AdvancedSymbologyController;
import com.esri.squadleader.controller.MapController;
import com.esri.squadleader.model.BasemapLayer;


/**
 * The main activity for the Squad Leader application. Typically this displays a map with various other
 * controls.
 */
public class SquadLeaderActivity extends Activity {
    
    private static final String TAG = SquadLeaderActivity.class.getSimpleName();
    
    private MapController mapController = null;
    private AdvancedSymbologyController mil2525cController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MapView mapView = (MapView) findViewById(R.id.map);
        mapController = new MapController(mapView, getAssets());
        try {
            mil2525cController = new AdvancedSymbologyController(
                    mapController,
                    getAssets(),
                    getString(R.string.sym_dict_dirname));
            mapController.setAdvancedSymbologyController(mil2525cController);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find file while loading AdvancedSymbologyController", e);
        }
    }
    
    public MapController getMapController() {
        return mapController;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapController.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume(); 
        mapController.unpause();
    }
    
    public void imageButton_zoomIn_clicked(View view) {
	mapController.zoomIn();
    }
    
    public void imageButton_zoomOut_clicked(View view) {
	mapController.zoomOut();
    }
    
    public void imageButton_openBasemapPanel_clicked(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_basemap)
                .setNegativeButton(R.string.cancel, null);
        List<BasemapLayer> basemapLayers = mapController.getBasemapLayers();
        String[] basemapLayerNames = new String[basemapLayers.size()];
        for (int i = 0; i < basemapLayers.size(); i++) {
            basemapLayerNames[i] = basemapLayers.get(i).getLayer().getName();
        }
        builder.setSingleChoiceItems(
                basemapLayerNames,
                mapController.getVisibleBasemapLayerIndex(),
                new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                mapController.setVisibleBasemapLayerIndex(which);
                dialog.dismiss();
            }
            
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}