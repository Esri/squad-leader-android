package com.esri.squadleader;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;


public class SquadLeaderActivity extends Activity {
	
	private static final String TAG = SquadLeaderActivity.class.getSimpleName();
	
	MapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		mapView = (MapView) findViewById(R.id.map);
		
		String tpkPath = "file:" + Environment.getExternalStorageDirectory() + "/SquadLeader/data/Topographic.tpk";
		Log.i(TAG, "Loading local tiled layer from " + tpkPath);
		Layer basemapLayer = new ArcGISLocalTiledLayer(tpkPath);
		mapView.addLayer(basemapLayer);
    }

	@Override 
	protected void onDestroy() { 
		super.onDestroy();
 }
	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
 }
	@Override
	protected void onResume() {
		super.onResume(); 
		mapView.unpause();
	}

}