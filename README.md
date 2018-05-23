# geoSpatialConversionWrapper
Creates a java wrapper which interfaces with native GeoTrans libraries to provide coordinate conversion and datum transformation.

## setup

### build environment
- Make sure you have the latest stable Java SE JDK installed (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Make sure you have the latest version of gradle loaded on your system, and it's bin directory available in your path. (https://gradle.org/install/)
- Make sure you have the GeoTrans jar file available for build integration.
- Edit the `build.gradle` file to update the repositories{} section to include the location(s) of the GeoTrans jar file.
- Open a Terminal window at the git root location (i.e. the same level as the `build.gradle` above).
- Make sure you have latest code `git pull` (This requires a git client to be installed, e.g. https://git-scm.com/)
- If the gradle wrapper hasn't already been created, you can create one for the project by calling. `gradle wrapper`
- For the build unit tests to run, make sure you have the GeoTrans program installed locally. Both the 64-bit Linux or 64-bit Windows build are supported. (http://earth-info.nga.mil/GandG/update/index.php?dir=coordsys&action=coordsys) (Note: *The GeoTrans download and documentation can be found under the Apps part of the Apps & Services section*)
- For the build unit tests to run, the GeoTrans libraries must be available in the `PATH` or `LD_LIBRARY_PATH`.
- For the build unit tests to run, The environment variable `MSPCCS_DATA` must be set to the full path to the data folder of the GeoTrans install.


### execution environment
- Make sure you have the latest stable Java SE Runtime installed (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Make sure you have the GeoTrans program installed locally. Both the 64-bit Linux or 64-bit Windows build are supported. (http://earth-info.nga.mil/GandG/update/index.php?dir=coordsys&action=coordsys) (Note: *The GeoTrans download and documentation can be found under the Apps part of the Apps & Services section*)
- The GeoTrans libraries must be available in the `PATH` or `LD_LIBRARY_PATH`.
- The environment variable `MSPCCS_DATA` must be set to the full path to the data folder of the GeoTrans install.

## build
- Open a Terminal to the base location of the project (where gradlew is located)
- Run the build `.\gradlew build`
- Resulting output will be located in `build/libs/` directory.
- Resulting test report will be located at `build/reports/tests/index.html`

## execution
- The resulting jar is meant to be interfaced via a web service wrapper.  The unit tests provide a way to test the wrapper to verify that it is successfully talking to the GeoTrans application.

## publicly exposed methods

| Method | Input | Result |
| ------ | ------ | ------ |
| retrieveAvailableCoordinateTypes() | void | Returns JSON containing the available coordinate types |
| retrieveAvailableDatums() | void | Returns JSON containing the available datums |
| doConversion() | String JSON | Returns JSON object containing the converted coordinates |
| doCoordinateTranslation() | String JSON | Returns JSON object containing the translated coordinates |
| retrieveAvailableEllipsoids() | void | Returns JSON object containing the available ellipsoids |
| retrieveSourceCoordinateInputByType() | void | Returns JSON object containing the required source coordinate fields by coordinate type. |

## development notes
- Git is rooted at the same level as this README.md file. To perform git commands properly against this repo you should execute those commands from that level e.g. `user/path/geoSpatialConversionWrapper/:-> git pull`
- If you have any pending changes on your local machine and want to pull latest, you must stash or discard these changes before pulling. The easiest command in git to use is `git stash`. There are a variety of optional arguments to this command depending on what you want to do.
- Must you can reach either the MavenCenral() repository or you define a path to your own repo which contains the required dependencies listed in the build.gradle for this project.

## stack
- GeoTrans (http://earth-info.nga.mil/GandG/update/index.php?dir=coordsys&action=coordsys): The NGA and DOD approved coordinate converter and datum translator. Both 64-bit Linux and Windows versions of GeoTrans are supported.