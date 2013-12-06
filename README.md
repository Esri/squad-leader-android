squad-leader-android
====================

[Building](#building)  
[Usage](#usage)  
[Setup](#setup)  
[Licensing](#licensing)

The Squad Leader template demonstrates best practices for building handheld military applications with ArcGIS. The Squad Leader template contains source code for a handheld application and directions for building the application from source. To download a precompiled distribution of the application, visit [ArcGIS for Defense and Intelligence](http://www.arcgis.com/home/group.html?owner=Arcgisonline_defense&title=ArcGIS%20for%20Defense%20and%20Intelligence).

## Building from source

### System requirements

- Android 2.3.3 or higher
- Android SDK
- [ArcGIS Runtime SDK 10.2 for Android](https://developers.arcgis.com/en/android/install.html)
- Eclipse 3.6.2 or higher

### Build steps

1. Clone this repository, or fork it and clone your fork.
2. In Eclipse, open the project found in your clone's source/SquadLeader directory.
3. Add the v7 appcompat Android Support Library to the SquadLeader project ([instructions](http://developer.android.com/tools/support-library/setup.html)). The SquadLeader project's reference to android-support-v7-appcompat is probably broken (red X) at this point, in which case you should remove it and add a reference to the one that [the instructions](http://developer.android.com/tools/support-library/setup.html) tell you to create.
3. If the project has errors, right-click the project in Eclipse and choose **ArcGIS Tools > Fix Project Properties**. (If **ArcGIS Tools** does not appear in the context menu, go back to the system requirements above and install the ArcGIS Runtime SDK 10.2 for Android.)
4. To run directly from Eclipse, right-click the project and choose **Run As > Android Application**.
5. To create an installer (.apk), right-click the project and choose **Export**. Choose **Android > Export Android Application** and step through the wizard.

## Usage

Launch the app on an Android device. An interactive map appears with several buttons and a data display that shows the current location in MGRS, time, speed, and heading.

### Menu button

The app makes use of the menu button found on many but not all Android devices. For the benefit of those devices with no menu button, the app's lower left corner has a menu button:

![Menu button](source/SquadLeader/res/drawable/ic_open_menu_normal.png)

When these instructions say to tap the menu button, you can use either the device's menu button or the app's menu button.

### Change the basemap

To change the basemap currently displayed, tap the basemap selector button in the upper left corner. A dialog appears with a list of basemaps that have been added to the app. Choose a basemap to display it. Only one basemap is visible at a time.

### Navigate the map

Drag a finger on the map to pan. To zoom in and out, either pinch open and close or use the buttons in the lower right corner.

The app displays a **Follow Me** button:

![Menu button](source/SquadLeader/res/drawable/ic_follow_me_normal.png)

When Follow Me is selected, the map follows the user's current location. To exit Follow Me mode, unselect the Follow Me button or simply pan the map. You can [change the location mode](#change-settings) if desired.

### Change settings

To change application settings, tap the menu button and choose Settings. You can change various settings:

- **Angular units**: choose the units, such as degrees or mils, that the app uses for displaying headings and bearings.
- [**Reset map**](#reset-the-map)

To change the location mode, tap the menu button and choose Set Location Mode. A dialog appears with various location mode choices:

- **Hardware (GPS)**: the app uses the device's location capabilities, including GPS if available, to obtain the user's location, speed, and heading.
- **Simulation (Built-in)**: the app uses GPS points in Jalalabad, Afghanistan, to simulate the user's location, speed, and heading.
- **Simulation (GPX File)**: the app uses points from a GPX file to simulate the user's location, speed, and heading. After choosing this option, select a GPX file on your device.

### Add a layer from the web

To add an ArcGIS Server service to the map, tap the menu button and choose Add Layer from Web. Type or paste the URL of an ArcGIS Server map service, feature service, or image service. Check the Use as Basemap checkbox if you want the added layer to be one of the app's basemaps, or leave it unchecked to add the layer on top of the current basemap. Tap Add Layer, and the layer appears on the map.

### Reset the map

You can clear any layers you have added and go back to the original map configuration. To reset the map, tap the menu button and choose **Reset map**. Tap **OK** if you want to reset the map. This will reload the map configuration from one of two locations:

1. If /mnt/sdcard/SquadLeader/mapconfig.xml exists on the device, it will be used for resetting the map.
2. Otherwise, Squad Leader's built-in default map configuration will be used.

## Setup

### System requirements

Squad Leader runs on Android devices version 2.3.3 and higher. This includes all Honeycomb devices and higher, as well as some but not all Gingerbread devices.

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
- TiledCacheLayer (file:/// URL to a local TPK or compact cache)
- TiledMapServiceLayer (cached map service)
- FeatureServiceLayer (feature service; either an entire feature service like ".../FeatureServer" or a single layer like ".../FeatureServer/42")
- ImageServiceLayer (image service)

For best results, be sure that one and only one layer with basemap="true" also has visible="true".

If you do not provide a mapconfig.xml file, a default list of ArcGIS Online basemap layers will be used when the app launches for the first time.

After the first launch, the app uses the bsaemap layers that it loaded previously. If you want to reset and re-read mapconfig.xml, you can [reset the map](#reset-the-map). Alternatively, you can manually go to the Android application settings, choose Squad Leader, and choose Clear Data. Then run the app and it will read mapconfig.xml again.

## Resources

* Learn more about Esri's [ArcGIS for the Military solution](http://solutions.arcgis.com/military/).

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing

Copyright 2013 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt) file.

Portions of this code use other Esri libraries, also governed by the Apache License:

- military-apps-library-java

Portions of this code use third-party libraries:

- Use of aFileChooser is governed by the Apache License.
- Use of the JSON Java library available at http://www.json.org/java/index.html is governed by the JSON License.

See [license-ThirdParty.txt](license-ThirdParty.txt) for the details of these licenses.

[](Esri Tags: ArcGIS Defense and Intelligence Military Defense Portal Android)
[](Esri Language: Java)
