/*******************************************************************************
 * Copyright 2013-2015 Esri
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

import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.Bundle;

import com.esri.android.map.LocationDisplayManager;
import com.esri.core.geometry.AngularUnit;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.LocationSimulator;
import com.esri.squadleader.util.Utilities;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

public class LocationController extends com.esri.militaryapps.controller.LocationController {

    private static final String PREF_LOCATION_MODE = "pref_locationMode";
    private static final String PREF_GPX_FILE = "pref_gpxFile";

    /**
     * The name of the preferences file used to store LocationController preferences. This file will
     * be stored in the application's private space for the current user.
     */
    public static final String PREFS_NAME = "LocationControllerPrefs";

    private SharedPreferences prefs = null;
    private LocationDisplayManager locationDisplayManager = null;

    /**
     * Instantiates a LocationController.
     * @param mode the location mode.
     * @param builtInGpxPath the built-in GPX resource path for simulated GPX. You can pass null if
     *                       you will never use the built-in GPX, or you can call setBuiltInGpxPath
     *                       later.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public LocationController(String builtInGpxPath, LocationMode mode)
            throws ParserConfigurationException, SAXException, IOException {
        super(mode);
        setBuiltInGpxPath(builtInGpxPath);
    }

    /**
     * @param prefs a preferences object that contains or will contain the user's location settings.
     */
    public void setSharedPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
        if (null != prefs) {
            String gpxFilePath = prefs.getString(PREF_GPX_FILE, null);
            setGpxFile(null == gpxFilePath ? null : new File(gpxFilePath));
        }
    }

    /**
     * Returns the LocationMode stored in the specified preferences, or LOCATION_SERVICE if a preference
     * has not been stored.
     * @param prefs the SharedPreferences where the location mode may be stored.
     * @return the LocationMode stored in the specified preferences, or LOCATION_SERVICE if a preference
     *         has not been stored.
     */
    public static LocationMode getLocationModeFromPreferences(SharedPreferences prefs) {
        return LocationMode.valueOf(prefs.getString(PREF_LOCATION_MODE, LocationMode.LOCATION_SERVICE.name()));
    }
    
    public void setLocationService(LocationDisplayManager locationService) {
        this.locationDisplayManager = locationService;
    }

    /**
     * Sets the location mode. If storePreferences is true and setSharedPreferences has been called
     * with a non-null SharedPreferences object, then this method will also store the mode in that object.
     * @param mode the location mode to use.
     * @param storePreference true if the mode should be stored as a preference.
     */
    @Override
    public void setMode(LocationMode mode, boolean storePreference) throws IOException, SAXException, ParserConfigurationException {
        super.setMode(mode, storePreference);
        if (storePreference && null != prefs) {
            prefs.edit().putString(PREF_LOCATION_MODE, mode.name()).apply();
        }
    }

    @Override
    public void setGpxFile(File gpxFile, boolean storePreference) {
        super.setGpxFile(gpxFile, storePreference);
        if (storePreference && null != prefs) {
            SharedPreferences.Editor editor = prefs.edit();
            if (null == gpxFile) {
                editor.remove(PREF_GPX_FILE);
            } else {
                editor.putString(PREF_GPX_FILE, gpxFile.getAbsolutePath());
            }
            editor.apply();
        }
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
                        if (null != locationDisplayManager) {
                            locationDisplayManager.resume();
                        }
                        break;
                    }
                    case STOPPED: {
                        if (null != locationDisplayManager) {
                            locationDisplayManager.start();
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
                if (null != locationDisplayManager) {
                    locationDisplayManager.pause();
                }
                state = LocationProviderState.PAUSED;
            }

            @Override
            public void stop() {
                if (null != locationDisplayManager) {
                    locationDisplayManager.stop();
                }
                state = LocationProviderState.STOPPED;
            }

            @Override
            public LocationProviderState getState() {
                return state;
            }
            
            private void setupLocationListener() {
                if (null != locationDisplayManager) {
                    locationDisplayManager.setLocationListener(new LocationListener() {
                        
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

    @Override
    protected LocationSimulator createLocationSimulator(InputStream gpxInputStream)
            throws ParserConfigurationException, SAXException, IOException {
        return new LocationSimulator(gpxInputStream);
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
