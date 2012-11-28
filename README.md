Android JUnit Report Test Runner
================================

Introduction
------------

The Android JUnit report test runner is a custom instrumentation test
runner for Android that creates XML test reports.  These reports are
in a similar format to those created by the Ant JUnit task's XML
formatter, allowing them to be integrated with tools that support that
format (e.g. continuous integration servers).

Home Page
---------

Android JUnit report has home on the web at:

http://zutubi.com/source/projects/android-junit-report/

License
-------

This code is licensed under the Apache License, Version 2.0.  See the
LICENSE file for details.

Quick Start
-----------

This is a quick overview of how to integrate the runner with Ant
builds. Note all modifications are made to your test project, i.e. the
project which implements the JUnit tests:

  * Grab the latest jar from:
      http://github.com/jsankey/android-junit-report/downloads
    and add it to your `libs/` directory.
  * Edit `AndroidManifest.xml` to set `android:name` in the
    `instrumentation` tag to:
    ```com.zutubi.android.junitreport.JUnitReportTestRunner```
  * Edit ant.properties to add the line:
    ```test.runner=com.zutubi.android.junitreport.JUnitReportTestRunner```
  * Run your tests as you would normally:
    ```$ ant debug install test```
  * Pull the resulting XML report from the device (from the application
    under test's internal storage directory):
    ```$ adb pull /data/data/main app package/files/junit-report.xml```
  * Integrate the XML with your chosen build tool.
  
Customising Via Arguments
-------------------------

The runner supports the following arguments:

  * `multiFile: if set to true, a new report file is generated for each
    test suite.  Defaults to false (a single file contains all suites).
  * reportFile: the name of the report file to generate (single file
    mode) or a pattern for the name of the files to generate (multiple
    file mode).  In the latter case the string `__suite__` will be
    substituted with the test suite name.  Defaults to
    `junit-report.xml` in single file mode,
    `junit-report-__suite__.xml` in multiple file mode.
  * `reportDiri`: path to a directory in which to write the report
    file(s).  May start with `__external__` which will be replaced with
    the external storage directory for the application under test.
    This requires external storage to be available and
    `WRITE_EXTERNAL_STORAGE` permission in the application under test.
    Defaults to unspecified, in which case the internal storage
    directory of the application under test is used.
  * `filterTraces`: if true, stack traces in the report will be filtered
    to remove common noise (e.g. framework methods).  Defaults to true.

To specify arguments, use the -e flag to adb shell am instrument, for
example:

```adb shell am instrument -w -e reportFile my-report.xml \
  com.example.test/com.zutubi.android.junitreport.JUnitReportTestRunner```

See the example and/or full documentation for how to set arguments in
you Ant build.

More Information
----------------

Check out the following resources for more details:

  * The project home page (with full documentation):
      http://zutubi.com/source/projects/android-junit-report/
  * The example project in the example/ subdirectory.
  * The GitHub project page:
      https://github.com/jsankey/android-junit-report/

Building From Source
--------------------

If you would like to modify the runner, or build it yourself for any
other reason, you will need:

  * A JDK, version 1.5 or later.
  * The Android SDK (or at least a stub android.jar as provided in the
    SDK).
  * Apache Ant version 1.7 or later.

To run a build:

  * Create a file local.properties in the directory containing this
    README.  In this file, define the location of an android.jar to
    build against, for example:

    ```android.jar=/opt/android/platforms/android-14/android.jar```

    where /opt/android is the root of an Android SDK.

  * Run ant in this same directory:

    ```$ ant```

The jar will be created at `build/android-junit-report-dev.jar`.

Feedback
-------

If you have any thoughts, questions etc about the runner, you can
contact me at:

  jason@zutubi.com

All feedback is welcome.
