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
package com.esri.squadleader.controller.test;

import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;

import com.esri.core.geometry.AngularUnit;
import com.esri.squadleader.controller.LocationController;
import com.esri.squadleader.view.SquadLeaderActivity;

public class LocationControllerTest extends ActivityInstrumentationTestCase2<SquadLeaderActivity> {
    
    private SquadLeaderActivity activity;
    private LocationController locationController;
    
    public LocationControllerTest() {
        super(SquadLeaderActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        locationController = (LocationController) activity.getMapController().getLocationController();
    }
    
    @Test
    public void test001HeadingToString() {
        String expected = "180";
        String actual = LocationController.headingToString(180.0, (AngularUnit) AngularUnit.create(4326), 0);
        assertEquals(expected, actual);
        
        expected = "180.0";
        actual = LocationController.headingToString(180.0, (AngularUnit) AngularUnit.create(4326), 1);
        assertEquals(expected, actual);
        
        expected = "180.00";
        actual = LocationController.headingToString(180.0, (AngularUnit) AngularUnit.create(4326), 2);
        assertEquals(expected, actual);
    }

}
