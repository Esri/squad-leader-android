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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.esri.android.map.LocationService;
import com.esri.militaryapps.model.LocationProvider;

public class LocationController extends com.esri.militaryapps.controller.LocationController {
    
    private LocationService locationService = null;
    
    public LocationController(boolean startImmediately, LocationService locationService)
            throws ParserConfigurationException, SAXException, IOException {
        this(LocationMode.LOCATION_SERVICE, startImmediately, locationService);
    }

    public LocationController(LocationMode mode, boolean startImmediately, LocationService locationService)
            throws ParserConfigurationException, SAXException, IOException {
        super(mode, startImmediately);
        this.locationService = locationService;
    }

    @Override
    protected LocationProvider createLocationServiceProvider() {
        return new LocationProvider() {
            
            private LocationProviderState state = LocationProviderState.STOPPED;

            @Override
            public void start() {
                switch (getState()) {
                    case PAUSED: {
                        locationService.resume();
                        break;
                    }
                    case STOPPED: {
                        locationService.start();
                        break;
                    }
                    case STARTED:
                    default: {
                        
                    }
                }
                state = LocationProviderState.STARTED;
            }

            @Override
            public void pause() {
                locationService.pause();
                state = LocationProviderState.PAUSED;
            }

            @Override
            public void stop() {
                locationService.stop();
                state = LocationProviderState.STOPPED;
            }

            @Override
            public LocationProviderState getState() {
                return state;
            }
            
        };
    }

}
