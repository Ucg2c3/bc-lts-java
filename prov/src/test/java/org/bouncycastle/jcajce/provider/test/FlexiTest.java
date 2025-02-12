package org.bouncycastle.jcajce.provider.test;

import junit.framework.TestCase;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

public abstract class FlexiTest
    extends TestCase
{

    /**
     * Source of randomness
     */
    protected Random rand;

    /**
     * Secure source of randomness
     */
    protected SecureRandom sr;

    protected void setUp()
    {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
        {
            Security.addProvider(new BouncyCastleProvider());
        }
        // initialize sources of randomness
        rand = new Random();
        sr = new SecureRandom();
        // TODO need it?
        sr.setSeed(sr.generateSeed(20));
    }

    protected static final void assertEquals(byte[] expected, byte[] actual)
    {
        assertTrue(Arrays.equals(expected, actual));
    }

    protected static final void assertEquals(String message, byte[] expected,
                                             byte[] actual)
    {
        assertTrue(message, Arrays.equals(expected, actual));
    }

    protected static final void assertEquals(int[] expected, int[] actual)
    {
        assertTrue(Arrays.equals(expected, actual));
    }

    protected static final void assertEquals(String message, int[] expected,
                                             int[] actual)
    {
        assertTrue(message, Arrays.equals(expected, actual));
    }

    /**
     * Method used to report test failure when in exception is thrown.
     *
     * @param e the exception
     */
    protected static final void fail(Exception e)
    {
        fail("Exception thrown: " + e.getClass().getName() + ":\n"
            + e.getMessage());
    }

}
