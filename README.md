squad-leader-android
====================

[Building from source](#building-from-source)  
[Usage](#usage)  
[Setup](#setup)  
[Licensing](#licensing)

The Squad Leader template demonstrates best practices for building handheld military applications with ArcGIS. The Squad Leader template contains source code for a handheld application and directions for building the application from source. To download a precompiled distribution of the application, visit [ArcGIS for Defense and Intelligence](http://www.arcgis.com/home/group.html?owner=Arcgisonline_defense&title=ArcGIS%20for%20Defense%20and%20Intelligence).

## Building from source

### System requirements

To run the app:

- Android 2.3.3 or higher
  - Limited functionality when running on an Android emulator ([more info](#running-on-an-android-emulator))

To build the app from source:

- Android SDK
  - Android API 14 or higher is required to build, even though the app will run on Android 2.3.3 (API 10) or higher
- [ArcGIS Runtime SDK 10.2 for Android](https://developers.arcgis.com/en/android/install.html)
  - Even if you install the Eclipse plugin from the online update site, you'll need to download the SDK to get the native binary files (see [build steps](#build-steps)).
- Eclipse 3.6.2 or higher

### Build steps

1. Clone this repository, or fork it and clone your fork.
2. Download the ArcGIS Runtime SDK, unzip it, and copy the contents of the libs directory from the SDK to your clone's source/SquadLeader/libs directory. If app size is an issue and you know you don't need to run Squad Leader on a particular platform, you can omit one or more of the directories (armeabi, armeabi-v7a, x86).
2. In Eclipse, open the project found in your clone's source/SquadLeader directory (**File > Import > Android > Existing Android Code Into Workspace**; do not copy the project to the workspace unless you know what you're doing).
3. Add the v7 appcompat Android Support Library to the workspace ([instructions](http://developer.android.com/tools/support-library/setup.html)). That gives you a library project called android-support-v7-appcompat. Right-click the SquadLeader project and choose **Properties > Android**. The SquadLeader project's reference to android-support-v7-appcompat is probably broken (red X) at this point, in which case you should remove it and add a reference to the android-support-v7-appcompat library project you just added to the workspace.
4. The squad-leader-android repo contains the aFileChooser repo as a submodule in the source directory. The aFileChooser repo has an Eclipse Android project. Add it to the workspace using **File > Import > Android > Existing Android Code Into Workspace** (do not copy the project to the workspace unless you know what you're doing). Now right-click the SquadLeader project and choose **Properties > Android**. If the SquadLeader project's reference to aFileChooser is broken, remove it and add a reference to the aFileChooser library project you just added to the workspace.
5. If any of the three projects (SquadLeader, android-support-v7-appcompat, aFileChooser) has errors, you may need to set the Android API level. Right-click the project and choose **Properties > Android**. Choose an Android API level 14 or higher. If you don't have API 14 or higher, install one in Eclipse by choosing **Window > Android SDK Manager** and selecting **SDK Platform** for API 14 or higher.
6. If the SquadLeader project has errors, right-click the project in Eclipse and choose **ArcGIS Tools > Fix Project Properties**. (If **ArcGIS Tools** does not appear in the context menu, go back to the system requirements above and install the ArcGIS Runtime SDK 10.2 for Android.) You can also clean and build the SquadLeader, aFileChooser, and/or android-support-v7-appcompat projects.
7. To run directly from Eclipse, right-click the project and choose **Run As > Android Application**. If you wish to run in an emulator, you must follow the directions in [this blog post](http://blogs.esri.com/esri/arcgis/2012/05/02/arcgis-runtime-sdk-for-android-v1-1-supports-android-emulator/) to ensure that the emulator has proper hardware GPU support.
5. To create an installer (.apk), right-click the project and choose **Export**. Choose **Android > Export Android Application** and step through the wizard.

### A note on military-apps-library-java

Squad Leader leverages [military-apps-library-java](https://github.com/ArcGIS/military-apps-library-java) as a submodule of the squad-leader-android repository. If ever you should want to update to the latest military-apps-library-java commit instead of the commit used by the squad-leader-android commit you're using, you can open a GitHub shell in squad-leader-android and run the following:

<code>
$ cd .\source\military-apps-library-java  
$ git pull origin master
</code>

### Running unit tests

1. Follow the [build steps](#build-steps) above.
2. In the same Eclipse workspace, open the project found in your clone's source/SquadLeader/tests (**File > Import > Android > Existing Android Code Into Workspace**; do not copy the project to the workspace unless you know what you're doing). The project name is SquadLeaderTest.
3. To run the SquadLeaderTest project, right-click the project and choose **Run As > Android Application**. If you wish to run in an emulator, you must follow the directions in [this blog post](http://blogs.esri.com/esri/arcgis/2012/05/02/arcgis-runtime-sdk-for-android-v1-1-supports-android-emulator/) to ensure that the emulator has proper hardware GPU support.

## Usage

Launch the app on an Android device. An interactive map appears with several buttons and a data display that shows the current location in MGRS, time, speed, and heading.

### Menu button

The app makes use of the menu button found on many but not all Android devices. For the benefit of those devices with no menu button, the app's lower left corner has a menu button:

![Menu button](source/SquadLeader/res/drawable/ic_open_menu_normal.png)

When these instructions say to tap the menu button, you can use either the device's menu button or the app's menu button.

### Change the basemap

To change the basemap currently displayed, tap the basemap selector button in the upper left corner. A dialog appears with a list of basemaps that have been added to the app. Choose a basemap to display it. Only one basemap is visible at a time.

### Display MGRS grid

To display or hide a military grid reference system (MGRS) grid on the map, toggle the grid button:

![Grid button](source/SquadLeader/res/drawable/ic_grid_normal.png)

### Navigate the map

Drag a finger on the map to pan. To zoom in and out, either pinch open and close or use the buttons in the lower right corner.

The app displays a **Follow Me** button:

![Menu button](source/SquadLeader/res/drawable/ic_follow_me_normal.png)

When Follow Me is selected, the map follows the user's current location. To exit Follow Me mode, unselect the Follow Me button or simply pan the map. You can [change the location mode](#change-settings) if desired.

To rotate the map, touch the map with two fingers and rotate. To reset the rotation so that north is up again, tap the north arrow:

![North arrow](source/SquadLeader/res/drawable/ic_north_arrow.png)

To navigate to an MGRS location, go to **Menu** > **Go to MGRS Location**. Type or paste a valid MGRS string and tap **Go to MGRS Location**. 

### Reporting

Squad Leader sends and receives Geomessages to and from other instances of Squad Leader as well as other ArcGIS for the Military apps and services, including Vehicle Commander and GeoEvent Processor. The app has many [settings](#change-settings) that govern outgoing messages. You can [simulate messages](#simulating-messages) if desired, especially for testing and demonstrations.

#### Position reports

The app periodically sends out an automatic position report consisting of the user's location, ID, vehicle type, and other information. To disable outgoing reports, go to **Menu** > **Settings** and disable **Position reports**.

#### Emergency status

Toggle the 911 button to activate or deactivate emergency status:

![911 button](source/SquadLeader/res/drawable/ic_911_normal.png)

As soon as emergency status is activated or deactivated, the position reports internal timer is reset, and a position report is immediately sent with the appropriate emergency status.

When Squad Leader receives a position report with emergency status activated, it displays the position report on the map and highlights it.

#### Spot reports

The user can create a spot report for observed hostile activities. Spot reports follow the SALUTE format (Size, Activity, Location, Unit, Time, Equipment).

To create a spot report, tap the spot report button:

![Spot report button](source/SquadLeader/res/drawable/ic_spot_report_normal.png)

Then tap the location on the map for the spot report. A spot report form displays with the location field pre-filled with the location you tapped. Change the form's values as needed and tap the Send button in the upper left corner. The spot report is sent to listening clients, including your own device, which displays the spot report on the map.

#### Chem lights

The user can create a chem light report as a quick way to drop a dot on the map and send it to listening clients. Four colors are available; a unit should predetermine what each color means.

To create a chem light, tap one of the colored chem light buttons:

![Red report button](source/SquadLeader/res/drawable/ic_chemlights_red_normal.png)

![Yellow report button](source/SquadLeader/res/drawable/ic_chemlights_yellow_normal.png)

![Green report button](source/SquadLeader/res/drawable/ic_chemlights_green_normal.png)

![Blue report button](source/SquadLeader/res/drawable/ic_chemlights_blue_normal.png)

Then tap the location on the map for the chem light. The chem light is sent to listening clients, including your own device, which displays the chem light on the map.

### Change settings

To change application settings, tap the menu button and choose Settings. You can change various settings:

- **Angular units**: choose the units, such as degrees or mils, that the app uses for displaying headings and bearings.
- **Username**: the username displayed for outgoing reports. For semantic reasons, the username should be unique.
- **Vehicle type**: the vehicle type used in outgoing reports.
- **Unique ID**: the user's unique ID. This ID should be unique. If other users use the same unique ID, clients will treat their messages as if they had come from the same user.
- **Symbol ID Code**: the MIL-STD-2525C symbol ID code (SIC or SIDC) associated with the user in outgoing position reports.
- **Position reports**: checked to automatically send periodic position reports to listening clients.
- **Position report period**: the number of milliseconds between outgoing position reports.
- **Messaging port**: the UDP port on which messages are sent and received. The port number must be between 1024 and 65535. All apps using the same port and connected to the same router will communicate with each other.
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

#### Running on an Android emulator

Like any app using ArcGIS Runtime for Android, Squad Leader can run on an Android emulator. But you must follow the instructions in [this blog post](http://blogs.esri.com/esri/arcgis/2012/05/02/arcgis-runtime-sdk-for-android-v1-1-supports-android-emulator/) to create an Android virtual device (AVD) that will work with ArcGIS Runtime. Please note that the Android emulator runs in a firewall-restricted sandbox that cannot communicate over UDP with outside processes, meaning you cannot send or receive Geomessages (spot reports, etc.) from or to Squad Leader running on an emulator.

### Installation and configuration

In order to install the app, your device must allow the installation of apps from unknown sources. On some devices, this setting is under **Settings > Security**. On other devices, this setting is under **Settings > Manage Applications**. Still other devices might have this setting elsewhere.

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

### Simulating messages

You can run the Message Simulator application included in the [Vehicle Commander](https://github.com/Esri/vehicle-commander) template ([source](https://github.com/Esri/vehicle-commander/tree/master/source/MessageSimulator), [sample messages](https://github.com/Esri/vehicle-commander/blob/master/source/MessageSimulator/SampleMessages/AFM_Sample_MSGS-719.xml). This is especially useful for testing and demonstration purposes. Note that these simulated messages will not make it to Squad Leader running on an emulator ([more info](#running-on-an-android-emulator)).

## Resources

* Learn more about Esri's [ArcGIS for the Military solution](http://solutions.arcgis.com/military/).

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

### Known issues

- When using Go to MGRS Location, some invalid MGRS strings cause the app to crash. For example, 60CVS123456 is a valid MGRS string, but 60CVR123456 is not because zone 60C has a VS square but not a VR square. Squad Leader attempts to parse and ignore invalid MGRS strings, but some strings (such as 60CVR123456) appear valid but are not and make the app crash. The crash is a known issue in ArcGIS Runtime that will be addressed in a current release of ArcGIS Runtime and a subsequent release of Squad Leader.

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
