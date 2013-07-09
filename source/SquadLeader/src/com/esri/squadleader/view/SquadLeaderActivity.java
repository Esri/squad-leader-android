package com.esri.squadleader.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.esri.android.map.MapView;
import com.esri.squadleader.R;

import controller.MapController;


public class SquadLeaderActivity extends Activity {
    
    private MapController mapController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MapView mapView = (MapView) findViewById(R.id.map);
        mapController = new MapController(mapView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapController.getMapView().pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume(); 
        mapController.getMapView().unpause();
    }
    
    public void imageButton_zoomIn_clicked(View view) {
	mapController.zoomIn();
    }
    
    public void imageButton_zoomOut_clicked(View view) {
	mapController.zoomOut();
    }

}