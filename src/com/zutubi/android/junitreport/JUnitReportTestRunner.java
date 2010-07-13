package com.zutubi.android.junitreport;

import android.os.Bundle;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;

/**
 * Custom test runner that adds a {@link JUnitReportListener} to the underlying
 * test runner in order to capture test results in an XML report.  You may use
 * this class in place of {@link InstrumentationTestRunner} in your test
 * project's manifest, and/or specify it to your Ant build using the
 * test.runner property.
 * <p/>
 * This runner behaves identically to the default, with the added side-effect
 * of producing a JUnit XML report.  The report format is similar to that
 * produced by the Ant JUnit task's XML formatter, making it compatible with
 * existing tools that can process that format.  See
 * {@link JUnitReportListener} for further details.
 * <p/>
 * This runner accepts the following arguments:
 * <ul>
 *     <li>reportFilePath: path of the file to write the XML report to, in the
 *     target application's data area (default: junit-report.xml).</li>
 *     <li>filterTraces: if true, stack traces in test failure reports will be
 *     filtered to remove noise such as framework methods (default: true)</li>
 * </ul>
 * These arguments may be specified as follows:
 * <pre>{@code adb shell am instrument -w -e reportFile my-report-file.xml}</pre>
 */
public class JUnitReportTestRunner extends InstrumentationTestRunner
{
    private static final String ARG_REPORT_FILE_PATH = "reportFilePath";
    private static final String ARG_FILTER_TRACES    = "filterTraces";

    private static final String DEFAULT_REPORT_FILE = "junit-report.xml";
    
    private JUnitReportListener listener;
    private String reportFilePath;
    private boolean filterTraces = true;
    
    @Override
    public void onCreate(Bundle arguments)
    {
        if (arguments != null)
        {
            reportFilePath = arguments.getString(ARG_REPORT_FILE_PATH);
            filterTraces = arguments.getBoolean(ARG_FILTER_TRACES, true);
        }
        
        if (reportFilePath == null)
        {
            reportFilePath = DEFAULT_REPORT_FILE;
        }

        super.onCreate(arguments);
    }
    
    @Override
    protected AndroidTestRunner getAndroidTestRunner()
    {
        AndroidTestRunner runner = new AndroidTestRunner();
        listener = new JUnitReportListener(getTargetContext(), reportFilePath, filterTraces);
        runner.addTestListener(listener);
        return runner;
    }

    @Override
    public void finish(int resultCode, Bundle results)
    {
        if (listener != null)
        {
            listener.close();
        }
        
        super.finish(resultCode, results);
    }
    
    
}
