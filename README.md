## Description
The example Android app for the Cogs real-time message brokering system.

## Requirements
* Android 4.0.4+
* Android Studio 1.5.0
* Requires GCM enabled application. You can read more about GCM [here](https://developers.google.com/cloud-messaging/)

## Installation
### Manual
* You will need to build the [cogs-android-client-sdk](https://github.com/cogswell-io/cogs-android-client-sdk) first, and install it into your local maven cache.
* Follow the Android Studio installation instructions appropriate for your platform. http://developer.android.com/sdk/index.html (If you are on Ubuntu 15.04 or later, the only dependencies you need are lib32stdc++6 and lib32z1. Attempting to install some of the others will result in errors)
* Once you have Android Studio installed, you will need to add the ANDROID_HOME environment variable to you profile, giving it the full path to your Android Studio intallation.
* Now you can run either your locally installed gradle or the Gradle Wrapper script (gradlew on Linux and OS X; gradlew.bat on Windows) in order to assemble the app: `./gradlew assemble`


## License

Copyright 2016 Aviata Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
