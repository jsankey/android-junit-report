package com.zutubi.android.junitreport;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * Custom test listener that outputs test results to a single XML file.  The
 * file uses a similar format the to Ant JUnit task XML formatter, with a
 * couple of caveats:
 * <ul>
 *     <li>Multiple suites are all placed in a single file under a root
 *     &lt;testsuites&gt; element.</li>
 *     <li>Redundant information about the number of nested cases within a
 *     suite is omitted.</li>
 *     <li>Durations are omitted from both suites and cases.</li>
 *     <li>Neither standard output nor system properties are included.</li>
 * </ul>
 * The differences mainly revolve around making this reporting as lightweight
 * as possible.  The report is streamed as the tests run, making it impossible
 * to, e.g. include the case count in a &lt;testsuite&gt; element.  It is
 * possible that durations may be added to cases in the future, as this
 * requires minimal buffering.
 */
public class JUnitReportListener implements TestListener
{
    private static final String LOG_TAG = "JUnitReportListener";
    
    private static final String ENCODING_UTF_8 = "utf-8";

    private static final String TAG_SUITES = "testsuites";
    private static final String TAG_SUITE = "testsuite";
    private static final String TAG_CASE = "testcase";
    private static final String TAG_ERROR = "error";
    private static final String TAG_FAILURE = "failure";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_CLASS = "classname";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_MESSAGE = "message";
    
    // With thanks to org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.
    // Trimmed some entries, added others for Android.
    private static final String[] DEFAULT_TRACE_FILTERS = new String[] {
                         "junit.framework.TestCase",
                         "junit.framework.TestResult",
                         "junit.framework.TestSuite",
                         "junit.framework.Assert.", // don't filter AssertionFailure
                         "java.lang.reflect.Method.invoke(",
                         "sun.reflect.",
                         // JUnit 4 support:
                         "org.junit.",
                         "junit.framework.JUnit4TestAdapter",
                         " more",
                         // Added for Android
                         "android.test.",
                         "android.app.Instrumentation",
                         "java.lang.reflect.Method.invokeNative",
                 };

    private Context context;
    private String reportFilePath;
    private boolean filterTraces;
    private FileOutputStream os;
    private XmlSerializer serializer;
    private String currentSuite;
    
    /**
     * Creates a new listener.
     * 
     * @param context        context of the target application under test
     * @param reportFilePath path of the report file to create (under the
     *                       context using {@link Context#openFileOutput(String, int)}).
     * @param filterTraces   if true, stack traces will have common noise (e.g.
     *                       framework methods) omitted for clarity
     */
    public JUnitReportListener(Context context, String reportFilePath, boolean filterTraces)
    {
        this.context = context;
        this.reportFilePath = reportFilePath;
        this.filterTraces = filterTraces;
    }
    
    @Override
    public void startTest(Test test)
    {
        try
        {
            openIfRequired(test);

            if (test instanceof TestCase)
            {
                TestCase testCase = (TestCase) test;
                checkForNewSuite(testCase);
                serializer.startTag("", TAG_CASE);
                serializer.attribute("", ATTRIBUTE_CLASS, currentSuite);
                serializer.attribute("", ATTRIBUTE_NAME, testCase.getName());
            }
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, safeMessage(e));
        }
    }

    private void checkForNewSuite(TestCase testCase) throws Exception
    {
        String suiteName = testCase.getClass().getName();
        if (currentSuite == null || !currentSuite.equals(suiteName))
        {
            if (currentSuite != null)
            {
                serializer.endTag("", TAG_SUITE);
            }
            
            serializer.startTag("", TAG_SUITE);
            serializer.attribute("", ATTRIBUTE_NAME, suiteName);
            currentSuite = suiteName;
        }
    }

    private void openIfRequired(Test test) throws FileNotFoundException, IOException
    {
        if (os == null)
        {
            os = context.openFileOutput(reportFilePath, 0);
            serializer = Xml.newSerializer();
            serializer.setOutput(os, ENCODING_UTF_8);
            serializer.startDocument(ENCODING_UTF_8, true);
            serializer.startTag("", TAG_SUITES);
        }
    }

    @Override
    public void addError(Test test, Throwable error)
    {
        addProblem(TAG_ERROR, error);
    }

    @Override
    public void addFailure(Test test, AssertionFailedError error)
    {
        addProblem(TAG_FAILURE, error);
    }

    private void addProblem(String tag, Throwable error)
    {
        try
        {
            serializer.startTag("", tag);
            serializer.attribute("", ATTRIBUTE_MESSAGE, safeMessage(error));
            serializer.attribute("", ATTRIBUTE_TYPE, error.getClass().getName());
            StringWriter w = new StringWriter(); 
            error.printStackTrace(filterTraces ? new FilteringWriter(w) : new PrintWriter(w));
            serializer.text(w.toString());
            serializer.endTag("", tag);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, safeMessage(e));
        }
    }

    @Override
    public void endTest(Test test)
    {
        try
        {
            if (test instanceof TestCase)
            {
                serializer.endTag("", TAG_CASE);
            }
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, safeMessage(e));
        }
    }

    public void close()
    {
        if (serializer != null)
        {
            try
            {
                if (currentSuite != null)
                {
                    serializer.endTag("", TAG_SUITE);
                }

                serializer.endTag("", TAG_SUITES);
                serializer.endDocument();
                serializer = null;
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, safeMessage(e));
            }
        }
        
        if (os != null)
        {
            try
            {
                os.close();
                os = null;
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, safeMessage(e));
            }
        }
    }

    private String safeMessage(Throwable error)
    {
        String message  = error.getMessage();
        return error.getClass().getName() + ": " + (message == null ? "<null>" : message);
    }
    
    /**
     * Wrapper around a print writer that filters out common noise from stack
     * traces, making it easier to see the actual failure.
     */
    private static class FilteringWriter extends PrintWriter
    {
        public FilteringWriter(Writer out)
        {
            super(out);
        }

        @Override
        public void println(String s)
        {
            for (String filtered: DEFAULT_TRACE_FILTERS)
            {
                if (s.contains(filtered))
                {
                    return;
                }
            }
            
            super.println(s);
        }
    }
}
