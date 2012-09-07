package com.zutubi.android.junitreport.example;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.zutubi.android.junitreport.example.AJRExampleActivityTest \
 * com.zutubi.android.junitreport.example.tests/android.test.InstrumentationTestRunner
 */
public class AJRExampleActivityTest extends ActivityInstrumentationTestCase2<AJRExampleActivity> {

    public AJRExampleActivityTest() {
        super("com.zutubi.android.junitreport.example", AJRExampleActivity.class);
    }

    public void testSanity() {
        assertEquals(2, 1 + 1);
    }
}
