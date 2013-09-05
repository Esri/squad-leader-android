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
package com.esri.squadleader.view.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.ImageButton;

import com.esri.android.map.MapView;
import com.esri.squadleader.view.SquadLeaderActivity;

public class SquadLeaderActivityTest extends ActivityInstrumentationTestCase2<SquadLeaderActivity> {
    
    private SquadLeaderActivity activity;
    private MapView mapView;
    private ImageButton zoomInButton;
            
    public SquadLeaderActivityTest() {
        super(SquadLeaderActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        mapView = (MapView) activity.findViewById(com.esri.squadleader.R.id.map);
        zoomInButton = (ImageButton) activity.findViewById(com.esri.squadleader.R.id.imageButton_zoomIn);
    }
    
    @UiThreadTest
    public void testZoomIn() throws InterruptedException {
        Thread.sleep(500);
        final double oldScale = mapView.getScale();
        Log.d(getClass().getName(), "oldScale is " + oldScale);
        zoomInButton.performClick();
        Thread.sleep(500);
        double newScale = mapView.getScale();
        Log.d(getClass().getName(), "newScale is " + newScale);
        assertEquals(oldScale / 2.0, newScale, 0.001);
    }
    
    @Override
    protected void tearDown() throws Exception {
        
    }

}
