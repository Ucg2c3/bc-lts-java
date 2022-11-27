package org.bouncycastle.util.utiltest;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.bouncycastle.core.PrintResults;

public class AllTests
    extends TestCase
{
    public static void main (String[] args)
    {
       PrintResults.printResult( junit.textui.TestRunner.run (suite()));
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("util tests");
        suite.addTestSuite(IPTest.class);
        suite.addTestSuite(BigIntegersTest.class);
        suite.addTestSuite(ArraysTest.class);
        return new BCTestSetup(suite);
    }

    static class BCTestSetup
        extends TestSetup
    {
        public BCTestSetup(Test test)
        {
            super(test);
        }

        protected void setUp()
        {

        }

        protected void tearDown()
        {

        }
    }
}
