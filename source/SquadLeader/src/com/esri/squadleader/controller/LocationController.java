/*******************************************************************************
 * Copyright 2013-2014 Esri
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
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.location.LocationListener;
import android.os.Bundle;

import com.esri.android.map.LocationService;
import com.esri.core.geometry.AngularUnit;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.squadleader.util.Utilities;

public class LocationController extends com.esri.militaryapps.controller.LocationController {
    
    private LocationService locationService = null;
    
    public LocationController(boolean startImmediately)
            throws ParserConfigurationException, SAXException, IOException {
        this(LocationMode.LOCATION_SERVICE, startImmediately);
    }

    public LocationController(LocationMode mode, boolean startImmediately)
            throws ParserConfigurationException, SAXException, IOException {
        super(mode, startImmediately);
    }
    
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    protected LocationProvider createLocationServiceProvider() {
        return new LocationProvider() {
            
            private LocationProviderState state = LocationProviderState.STOPPED;
            
            @Override
            public void start() {
                setupLocationListener();
                switch (getState()) {
                    case PAUSED: {
                        if (null != locationService) {
                            locationService.resume();
                        }
                        break;
                    }
                    case STOPPED: {
                        if (null != locationService) {
                            locationService.start();
                        }
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
                if (null != locationService) {
                    locationService.pause();
                }
                state = LocationProviderState.PAUSED;
            }

            @Override
            public void stop() {
                if (null != locationService) {
                    locationService.stop();
                }
                state = LocationProviderState.STOPPED;
            }

            @Override
            public LocationProviderState getState() {
                return state;
            }
            
            private void setupLocationListener() {
                if (null != locationService) {
                    locationService.setLocationListener(new LocationListener() {
                        
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}
                        
                        @Override
                        public void onProviderEnabled(String provider) {}
                        
                        @Override
                        public void onProviderDisabled(String provider) {}
                        
                        @Override
                        public void onLocationChanged(android.location.Location location) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(location.getTime());
                            Location theLocation = new Location(location.getLongitude(), location.getLatitude(), cal, location.getSpeed(), location.getBearing());
                            sendLocation(theLocation);
                        }
                    });
                }
            }
            
        };
    }
    
    /**
     * Creates a String representing the given heading, converted to the given AngularUnit.
     * For example headingToString(180, AngularUnit.create(AngularUnit.Code.RADIAN), 5) returns
     * "3.14159°".
     * @param headingInDegrees the heading in degrees.
     * @param toAngularUnit the AngularUnit to which the heading should be converted.
     * @param decimalPlaces the number of decimal places the output should have.
     * @return a String with the converted heading and its unit abbreviation.
     */
    public static String headingToString(double headingInDegrees, AngularUnit toAngularUnit, int decimalPlaces) {
        double convertedRoundedHeading = headingInDegrees;
        if (null != toAngularUnit && !Utilities.DEGREES.equals(toAngularUnit)) {
             convertedRoundedHeading = toAngularUnit.convertFromRadians(Utilities.DEGREES.convertToRadians(headingInDegrees));
        }
        double multiplier = Math.pow(10, decimalPlaces);
        convertedRoundedHeading = Math.round(convertedRoundedHeading * multiplier) / multiplier;
        String headingString = (0 == decimalPlaces) ? Integer.toString((int) convertedRoundedHeading) : Double.toString(convertedRoundedHeading);
        //Pad with zeroes if necessary
        int pointIndex = headingString.indexOf('.');
        if (-1 < pointIndex) {
            String decimals = headingString.substring(pointIndex + 1);
            for (int i = 0; i < decimalPlaces - decimals.length(); i++) {
                headingString += '0';
            }
        }
        return headingString +
                (null == toAngularUnit ? "" : Utilities.getAngularUnitAbbreviation(toAngularUnit.getID(), toAngularUnit.getAbbreviation()));
    }

}
