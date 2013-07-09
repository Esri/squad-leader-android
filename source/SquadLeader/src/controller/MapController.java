package controller;

import android.os.Environment;
import android.util.Log;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;

public class MapController {
    
    private static final String TAG = MapController.class.getSimpleName();
    
    private final MapView mapView;
    
    public MapController(MapView mapView) {        
        this.mapView = mapView;
        
        String tpkPath = "file:" + Environment.getExternalStorageDirectory() + "/SquadLeader/data/Topographic.tpk";
        Log.i(TAG, "Loading local tiled layer from " + tpkPath);
        Layer basemapLayer = new ArcGISLocalTiledLayer(tpkPath);
        mapView.addLayer(basemapLayer);
    }
    
    public MapView getMapView() {
	return mapView;
    }
    
    public void zoomIn() {
	mapView.zoomin();
    }
    
    public void zoomOut() {
	mapView.zoomout();
    }

}
