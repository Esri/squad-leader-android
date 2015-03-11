User Guide - squad-leader-android 
====================

[Usage](#usage)  
[Setup](#setup)  

## Usage

Launch the app on an Android device. An interactive map appears with several buttons and a data display that shows the current location in MGRS, time, speed, and heading.

### Change the basemap

To change the basemap currently displayed, tap the basemap selector button in the upper left corner. A dialog appears with a list of basemaps that have been added to the app. Choose a basemap to display it. Only one basemap is visible at a time.

### Display MGRS grid

To display or hide a military grid reference system (MGRS) grid on the map, toggle the grid button:

![Grid button](../source/SquadLeader/res/drawable/ic_grid_normal.png)

### Navigate the map

Drag a finger on the map to pan. To zoom in and out, either pinch open and close or use the buttons in the lower right corner.

The app displays a **Follow Me** button:

![Menu button](../source/SquadLeader/res/drawable/ic_follow_me_normal.png)

When Follow Me is selected, the map follows the user's current location. To exit Follow Me mode, unselect the Follow Me button or simply pan the map. You can [change the location mode](#change-settings) if desired.

To rotate the map, touch the map with two fingers and rotate. To reset the rotation so that north is up again, tap the north arrow:

![North arrow](../source/SquadLeader/res/drawable/ic_north_arrow.png)

To navigate to an MGRS location, go to **Menu** > **Go to MGRS Location**. Type or paste a valid MGRS string and tap **Go to MGRS Location**. 

### Reporting

Squad Leader sends and receives Geomessages to and from other instances of Squad Leader as well as other ArcGIS for the Military apps and services, including Vehicle Commander and GeoEvent Processor. The app has many [settings](#change-settings) that govern outgoing messages. You can [simulate messages](#simulating-messages) if desired, especially for testing and demonstrations.

#### Position reports

The app periodically sends out an automatic position report consisting of the user's location, ID, vehicle type, and other information. To disable outgoing reports, go to **Menu** > **Settings** and disable **Position reports**.

#### Emergency status

Toggle the 911 button to activate or deactivate emergency status:

![911 button](../source/SquadLeader/res/drawable/ic_911_normal.png)

As soon as emergency status is activated or deactivated, the position reports internal timer is reset, and a position report is immediately sent with the appropriate emergency status.

When Squad Leader receives a position report with emergency status activated, it displays the position report on the map and highlights it.

#### Spot reports

The user can create a spot report for observed hostile activities. Spot reports follow the SALUTE format (Size, Activity, Location, Unit, Time, Equipment).

To create a spot report, tap the spot report button:

![Spot report button](../source/SquadLeader/res/drawable/ic_spot_report_normal.png)

Then tap the location on the map for the spot report. A spot report form displays with the location field pre-filled with the location you tapped. Change the form's values as needed and tap the Send button in the upper left corner. The spot report is sent to listening clients, including your own device, which displays the spot report on the map.

#### Chem lights

The user can create a chem light report as a quick way to drop a dot on the map and send it to listening clients. Four colors are available; a unit should predetermine what each color means.

To create a chem light, tap one of the colored chem light buttons:

![Red report button](../source/SquadLeader/res/drawable/ic_chemlights_red_normal.png)

![Yellow report button](../source/SquadLeader/res/drawable/ic_chemlights_yellow_normal.png)

![Green report button](../source/SquadLeader/res/drawable/ic_chemlights_green_normal.png)

![Blue report button](../source/SquadLeader/res/drawable/ic_chemlights_blue_normal.png)

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

You can run the [GeoMessage Simulator application](https://github.com/Esri/geomessage-simulator-qt) to send messages to Squad Leader. GeoMessage Simulator is especially useful for testing and demonstration purposes. Note that these simulated messages will not make it to Squad Leader running on an emulator ([more info](#running-on-an-android-emulator)). (TODO add link to compiled GeoMessage Simulator when it is posted)

Squad Leader supports the "removeall" Geomessage action to remove all messages of a certain type (e.g. position_report, chemlight, spot_report). Here's an example that removes all position reports:

    <geomessages>
        <geomessage v="1.0">
            <_type>position_report</_type>
            <_action>removeall</_action>
            <_id>{b4b3eeaa-c769-11e4-8731-1681e6b88ec1}</_id>
        </geomessage>
    </geomessages>