# Phone tracker
[ ![Download](https://api.bintray.com/packages/fooock/maven/phone-tracker/images/download.svg) ](https://bintray.com/fooock/maven/phone-tracker/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Phone%20tracker-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5476)

Phone tracker is an Android library to gather environment signals, like cell towers, wifi access points and gps locations. You can configure how to scan. Also you can make hot configuring updates, and be notified when the configuration is updated, among other things.

## Installation
For gradle based projects you need to add to your ```build.gradle```
```gradle
repositories {
    jcenter()
}
```
And in your dependencies block add this line
```gradle
compile 'com.fooock:phone-tracker:0.2.1'
```

## Getting started
You only need to create and instance of the ```PhoneTracker``` class and pass the ```Context``` to it
```java
PhoneTracker phoneTracker = new PhoneTracker(this);
```
Now you can call the ```start()``` method from the tracker instance. The default configuration is used. For default all sensors are enabled.

**Important:** Note that if you are target Android 6.0 or greater, you need to grant location permissions to the application in order to gather environment data. You can listen for missing permissions from the ```PhoneTracker``` class using the ```PhoneTracker.PermissionListener``` interface:
```java
// Listen for missing permissions
phoneTracker.addPermissionListener(new PhoneTracker.PermissionListener() {
    @Override
    public void onPermissionNotGranted(String... permission) {

    }
});
```
If the permissions are not granted, the tracker **can't start**.

To check if the tracker is running:
```java
// Check the state of the tracker
boolean running = phoneTracker.isRunning();
```
To stop the tracker:
```java
// Stop all sensors and don't receive more updates
phoneTracker.stop();
```
## Tracker configuration
To create a default ```Configuration```:
```java
// Create a default configuration
Configuration configuration = new Configuration.Builder().create();
```
Now see how you can customize the configuration:
* **Wifi**
Create a Wifi configuration
```java
// Create a new wifi configuration
Configuration.Wifi wifiConf = new Configuration.Wifi();
wifiConf.setScanDelay(3000);
```
* **Cell**
Create a cell configuration
```java
// Create a new cell configuration
Configuration.Cell cellConf = new Configuration.Cell();
cellConf.setScanDelay(1000);
```
* **GPS**
Create a GPS configuration
```java
// Create a gps configuration
Configuration.Gps gpsConf = new Configuration.Gps();
gpsConf.setMinDistanceUpdate(10);
gpsConf.setMinTimeUpdate(7000);
```
To create the new custom configuration:
```java
// Create a new custom configuration
Configuration configuration = new Configuration.Builder()
    .useCell(true).cell(cellConf)
    .useWifi(true).wifi(wifiConf)
    .useGps(true).gps(gpsConf)
    .create();
```
In order to make effective the configuration:
```java
// Set the init configuration
phoneTracker.setConfiguration(configuration);
```
Also, if you want to change the current configuration when the tracker is running:
```java
// Update the current configuration
phoneTracker.updateConfiguration(configuration);
```
This method only loads the configuration that has changed, without stopping the tracker. You can listen for configuration changes using the ```PhoneTracker.ConfigurationChangeListener``` interface:
```java
// Listen for configuration changes
phoneTracker.setConfigurationChangeListener(new PhoneTracker.ConfigurationChangeListener(){
    @Override
    public void onConfigurationChange(Configuration configuration) {

    }
});
```
## Receiving data
You can setup listeners to receive wifi, gps updates and cell tower signals. See below.
* **Wifi**
```java
// Set the listener to receive wifi scans
phoneTracker.setWifiScanListener(new PhoneTracker.WifiScanListener() {
    @Override
    public void onWifiScansReceived(long timestamp, List<ScanResult> wifiScans) {

    }
});
```
* **GPS**
```java
// Set the listener to receive location updates from gps
phoneTracker.setGpsLocationListener(new PhoneTracker.GpsLocationListener() {
    @Override
    public void onLocationReceived(long timestamp, Location location) {

    }
});
```
* **Cell**
```java
// Set the listener for cell scans
phoneTracker.setCellScanListener(new PhoneTracker.CellScanListener() {
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onCellInfoReceived(long timestamp, List<CellInfo> cells) {

    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void onNeighborCellReceived(long timestamp, List<NeighboringCellInfo> cells) {

    }
});
```
Note if your application target from API 17 to 25 you don't need override the ```onNeighborCellReceived(...)```. For this I created an adapter class ```PhoneTracker.CellScanAdapter``` to override only the method you are interested:
```java
phoneTracker.setCellScanListener(new PhoneTracker.CellScanAdapter() {
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onCellInfoReceived(long timestamp, List<CellInfo> cells) {

    }
});
```

## License
```
Copyright 2017 newhouse (nhitbh at gmail dot com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [dill]: <https://github.com/joemccann/dillinger>
   [git-repo-url]: <https://github.com/joemccann/dillinger.git>
   [john gruber]: <http://daringfireball.net>
   [df1]: <http://daringfireball.net/projects/markdown/>
   [markdown-it]: <https://github.com/markdown-it/markdown-it>
   [Ace Editor]: <http://ace.ajax.org>
   [node.js]: <http://nodejs.org>
   [Twitter Bootstrap]: <http://twitter.github.com/bootstrap/>
   [jQuery]: <http://jquery.com>
   [@tjholowaychuk]: <http://twitter.com/tjholowaychuk>
   [express]: <http://expressjs.com>
   [AngularJS]: <http://angularjs.org>
   [Gulp]: <http://gulpjs.com>

   [PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
   [PlGh]: <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
   [PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
   [PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
   [PlMe]: <https://github.com/joemccann/dillinger/tree/master/plugins/medium/README.md>
   [PlGa]: <https://github.com/RahulHP/dillinger/blob/master/plugins/googleanalytics/README.md>
