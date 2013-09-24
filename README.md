squad-leader-android
====================

[Usage](#usage)  
[Setup](#setup)  
[Licensing](#licensing)

The Squad Leader template demonstrates best practices for building handheld military applications with ArcGIS. The Squad Leader template contains source code for a handheld application and directions for building the application from source. To download a precompiled distribution of the application, visit [ArcGIS for Defense and Intelligence](http://www.arcgis.com/home/group.html?owner=Arcgisonline_defense&title=ArcGIS%20for%20Defense%20and%20Intelligence).

<a id="usage"></a>
## Usage

Launch the app on an Android device. An interactive map appears.

### Change the basemap

To change the basemap currently displayed, tap the basemap selector button in the upper left corner. A dialog appears with a list of basemaps that have been added to the app. Choose a basemap to display it. Only one basemap is visible at a time.

### Navigate the map

Drag a finger on the map to pan. To zoom in and out, either pinch open and close or use the buttons in the lower right corner.

### Add a layer from the web

To add an ArcGIS Server service to the map, press the menu button (in the lower left corner of the app, or the device's menu button) and choose Add Layer from Web. Type or paste the URL of an ArcGIS Server map service, feature service, or image service. Check the Use as Basemap checkbox if you want the added layer to be one of the app's basemaps, or leave it unchecked to add the layer on top of the current basemap. Tap Add Layer, and the layer appears on the map.

<a id="setup"></a>
## Setup

### System requirements

Squad Leader runs on Android devices version 2.3.3 and higher. This includes some but not all Gingerbread devices, as well as all devices with Honeycomb or newer. For Gingerbread, check your device's Android OS version number to verify it has 2.3.3 or higher.

### Installation and configuration

Install the app from the APK file you can download from [ArcGIS for Defense and Intelligence](http://www.arcgis.com/home/group.html?owner=Arcgisonline_defense&title=ArcGIS%20for%20Defense%20and%20Intelligence).

Optional: before running the app for the first time, if you wish to specify which layers the app initially uses, you can create a file called mapconfig.xml and put it in /mnt/sdcard/SquadLeader on the target device. Here is a simple mapconfig.xml file:

    <?xml version="1.0" encoding="UTF-8"?>
    <mapconfig name="test map">
        <layers>
            <layer name="Imagery" visible="true" type="TiledMapServiceLayer" basemap="true">
                <url>http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer</url>
            </layer>
        </layers>
        <initialextent>
            <anchor>
                <x>7842690</x>
                <y>4086500</y>
            </anchor>
            <scale>250000</scale>
        </initialextent>
    </mapconfig>

Layer type should be one of the following:

- DynamicMapServiceLayer (dynamic map service)
- TiledCacheLayer (local TPK or compact cache)
- TiledMapServiceLayer (cached map service)

For best results, be sure that one and only one layer with basemap="true" also has visible="true".

If you do not provide a mapconfig.xml file, a default list of ArcGIS Online basemap layers will be used when the app launches for the first time.

After the first launch, the app uses the bsaemap layers that it loaded previously. If you want to reset and re-read mapconfig.xml, go to the Android application settings, choose Squad Leader, and choose Clear Data. Then run the app and it will read mapconfig.xml again.

## Licensing

Copyright 2013 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt) file.

Portions of this code use other Esri libraries, also governed by the Apache License:

- military-apps-library-java

Portions of this code use third-party libraries:

- Use of the JSON Java library available at http://www.json.org/java/index.html is governed by the JSON License.

See [license-ThirdParty.txt](license-ThirdParty.txt) for the details of these licenses.

[](Esri Tags: ArcGIS Defense and Intelligence Military Defense Portal Android)
[](Esri Language: Java)
