package org.bouncycastle.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAValidationParameters;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;
import org.bouncycastle.crypto.prng.drbg.HMacSP800DRBG;
import org.bouncycastle.crypto.prng.drbg.SP80090DRBG;
import org.bouncycastle.util.Pack;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Basic registrar class for providing defaults for cryptography services in this module.
 */
public final class CryptoServicesRegistrar
{
    private static final Logger LOG = Logger.getLogger(CryptoServicesRegistrar.class.getName());

    private static final String infoString = "BouncyCastle APIs (LTS edition) v2.73.8";

    private static final Permission CanSetDefaultProperty =
            new CryptoServicesPermission(CryptoServicesPermission.GLOBAL_CONFIG);
    private static final Permission CanSetThreadProperty =
            new CryptoServicesPermission(CryptoServicesPermission.THREAD_LOCAL_CONFIG);
    private static final Permission CanSetDefaultRandom =
            new CryptoServicesPermission(CryptoServicesPermission.DEFAULT_RANDOM);
    private static final Permission CanSetConstraints =
            new CryptoServicesPermission(CryptoServicesPermission.CONSTRAINTS);

    private static final ThreadLocal<Map<String, Object[]>> threadProperties = new ThreadLocal<Map<String, Object[]>>();
    private static final Map<String, Object[]> globalProperties = Collections.synchronizedMap(new HashMap<String,
            Object[]>());
    private static final SecureRandomProvider defaultRandomProviderImpl = new ThreadLocalSecureRandomProvider();

    private static final CryptoServicesConstraints noConstraintsImpl = new CryptoServicesConstraints()
    {
        public void check(CryptoServiceProperties service)
        {
            // anything goes.
        }
    };

    private static final AtomicReference<SecureRandomProvider> defaultSecureRandomProvider =
            new AtomicReference<SecureRandomProvider>();
    private static final boolean preconfiguredConstraints;
    private static final AtomicReference<CryptoServicesConstraints> servicesConstraints =
            new AtomicReference<CryptoServicesConstraints>();

    private static final NativeServices nativeServices;




    static
    {
        // default domain parameters for DSA and Diffie-Hellman

        DSAParameters def512Params = new DSAParameters(
                new BigInteger(
                        "fca682ce8e12caba26efccf7110e526db078b05edecbcd1eb4a208f3ae1617ae01f35b91a47e6df63413c5e12ed0899bcd132acd50d99151bdc43ee737592e17", 16),
                new BigInteger("962eddcc369cba8ebb260ee6b6a126d9346e38c5", 16),
                new BigInteger(
                        "678471b27a9cf44ee91a49c5147db1a9aaf244f05a434d6486931d2d14271b9e35030b71fd73da179069b32e2935630e1c2062354d0da20a6c416e50be794ca4", 16),
                new DSAValidationParameters(Hex.decodeStrict("b869c82b35d70e1b1ff91b28e37a62ecdc34409b"), 123));

        DSAParameters def768Params = new DSAParameters(
                new BigInteger("e9e642599d355f37c97ffd3567120b8e25c9cd43e927b3a9670fbec5" +
                        "d890141922d2c3b3ad2480093799869d1e846aab49fab0ad26d2ce6a" +
                        "22219d470bce7d777d4a21fbe9c270b57f607002f3cef8393694cf45" +
                        "ee3688c11a8c56ab127a3daf", 16),
                new BigInteger("9cdbd84c9f1ac2f38d0f80f42ab952e7338bf511", 16),
                new BigInteger("30470ad5a005fb14ce2d9dcd87e38bc7d1b1c5facbaecbe95f190aa7" +
                        "a31d23c4dbbcbe06174544401a5b2c020965d8c2bd2171d366844577" +
                        "1f74ba084d2029d83c1c158547f3a9f1a2715be23d51ae4d3e5a1f6a" +
                        "7064f316933a346d3f529252", 16),
                new DSAValidationParameters(Hex.decodeStrict("77d0f8c4dad15eb8c4f2f8d6726cefd96d5bb399"), 263));

        DSAParameters def1024Params = new DSAParameters(
                new BigInteger("fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80" +
                        "b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b" +
                        "801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c6" +
                        "1bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675" +
                        "f3ae2b61d72aeff22203199dd14801c7", 16),
                new BigInteger("9760508f15230bccb292b982a2eb840bf0581cf5", 16),
                new BigInteger("f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b" +
                        "3d0782675159578ebad4594fe67107108180b449167123e84c281613" +
                        "b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f" +
                        "0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06" +
                        "928b665e807b552564014c3bfecf492a", 16),
                new DSAValidationParameters(Hex.decodeStrict("8d5155894229d5e689ee01e6018a237e2cae64cd"), 92));

        DSAParameters def2048Params = new DSAParameters(
                new BigInteger("95475cf5d93e596c3fcd1d902add02f427f5f3c7210313bb45fb4d5b" +
                        "b2e5fe1cbd678cd4bbdd84c9836be1f31c0777725aeb6c2fc38b85f4" +
                        "8076fa76bcd8146cc89a6fb2f706dd719898c2083dc8d896f84062e2" +
                        "c9c94d137b054a8d8096adb8d51952398eeca852a0af12df83e475aa" +
                        "65d4ec0c38a9560d5661186ff98b9fc9eb60eee8b030376b236bc73b" +
                        "e3acdbd74fd61c1d2475fa3077b8f080467881ff7e1ca56fee066d79" +
                        "506ade51edbb5443a563927dbc4ba520086746175c8885925ebc64c6" +
                        "147906773496990cb714ec667304e261faee33b3cbdf008e0c3fa906" +
                        "50d97d3909c9275bf4ac86ffcb3d03e6dfc8ada5934242dd6d3bcca2" +
                        "a406cb0b", 16),
                new BigInteger("f8183668ba5fc5bb06b5981e6d8b795d30b8978d43ca0ec572e37e09939a9773", 16),
                new BigInteger("42debb9da5b3d88cc956e08787ec3f3a09bba5f48b889a74aaf53174" +
                        "aa0fbe7e3c5b8fcd7a53bef563b0e98560328960a9517f4014d3325f" +
                        "c7962bf1e049370d76d1314a76137e792f3f0db859d095e4a5b93202" +
                        "4f079ecf2ef09c797452b0770e1350782ed57ddf794979dcef23cb96" +
                        "f183061965c4ebc93c9c71c56b925955a75f94cccf1449ac43d586d0" +
                        "beee43251b0b2287349d68de0d144403f13e802f4146d882e057af19" +
                        "b6f6275c6676c8fa0e3ca2713a3257fd1b27d0639f695e347d8d1cf9" +
                        "ac819a26ca9b04cb0eb9b7b035988d15bbac65212a55239cfc7e58fa" +
                        "e38d7250ab9991ffbc97134025fe8ce04c4399ad96569be91a546f49" +
                        "78693c7a", 16),
                new DSAValidationParameters(Hex.decodeStrict(
                        "b0b4417601b59cbc9d8ac8f935cadaec4f5fbb2f23785609ae466748d9b5a536"), 497));

        localSetGlobalProperty(Property.DSA_DEFAULT_PARAMS, def512Params, def768Params, def1024Params, def2048Params);
        localSetGlobalProperty(Property.DH_DEFAULT_PARAMS, toDH(def512Params), toDH(def768Params),
                toDH(def1024Params), toDH(def2048Params));

        servicesConstraints.set(getDefaultConstraints());
        preconfiguredConstraints = (servicesConstraints.get() != noConstraintsImpl);

        //
        // Load the native code.
        //
        NativeLoader.loadDriver();

        nativeServices = new DefaultNativeServices();
    }


    private CryptoServicesRegistrar()
    {

    }

    public static String getInfo()
    {
        return infoString;
    }

    public static boolean isNativeEnabled()
    {
        return NativeLoader.isNativeAvailable();
    }


    public static void setNativeEnabled(boolean enabled)
    {
        NativeLoader.setNativeEnabled(enabled);
    }


    public static NativeServices getNativeServices()
    {
        return nativeServices;
    }

    @Deprecated
    public static void setPacketCipherEnabled(boolean enabled)
    {
    }

    @Deprecated
    public static boolean isPacketCipherEnabled()
    {
        return false;
    }

    public static boolean hasEnabledService(String feature)
    {

        return nativeServices != null &&
                nativeServices.isSupported() &&
                nativeServices.isInstalled() &&
                nativeServices.isEnabled() &&
                nativeServices.hasService(feature);
    }

    /**
     * Return the default source of randomness.
     *
     * @return the default SecureRandom
     */
    public static SecureRandom getSecureRandom()
    {
        defaultSecureRandomProvider.compareAndSet(null, defaultRandomProviderImpl);

        return defaultSecureRandomProvider.get().get();
    }

    /**
     * Return either the passed-in SecureRandom, or if it is null, then the default source of randomness.
     *
     * @param secureRandom the SecureRandom to use if it is not null.
     * @return the SecureRandom parameter if it is not null, or else the default SecureRandom
     */
    public static SecureRandom getSecureRandom(SecureRandom secureRandom)
    {
        return null == secureRandom ? getSecureRandom() : secureRandom;
    }

    /**
     * Set a default secure random to be used where none is otherwise provided.
     *
     * @param secureRandom the SecureRandom to use as the default.
     */
    public static void setSecureRandom(final SecureRandom secureRandom)
    {
        checkPermission(CanSetDefaultRandom);

        if (secureRandom == null)
        {
            defaultSecureRandomProvider.set(defaultRandomProviderImpl);
        }
        else
        {
            defaultSecureRandomProvider.set(new SecureRandomProvider()
            {
                public SecureRandom get()
                {
                    return secureRandom;
                }
            });
        }
    }

    /**
     * Set a default secure random provider to be used where none is otherwise provided.
     *
     * @param secureRandomProvider a provider SecureRandom to use when a default SecureRandom is requested.
     */
    public static void setSecureRandomProvider(SecureRandomProvider secureRandomProvider)
    {
        checkPermission(CanSetDefaultRandom);

        defaultSecureRandomProvider.set(secureRandomProvider);
    }

    /**
     * Return the default entropy source for this JVM.
     *
     * @return the default entropy source.
     */
    public static EntropySourceProvider getDefaultEntropySourceProvider()
    {
        if (NativeLoader.hasNativeService(NativeServices.DRBG) ||
                NativeLoader.hasNativeService(NativeServices.NRBG))
        {
            return new EntropySourceProvider()
            {
                public EntropySource get(int bitsRequired)
                {
                    return new NativeEntropySource(bitsRequired);
                }
            };
        }
        else if (Properties.isOverrideSet("org.bouncycastle.drbg.entropy_thread"))
        {
            synchronized (entropyDaemon)
            {
                if (entropyThread == null)
                {
                    entropyThread = new Thread(entropyDaemon, "BC Entropy Daemon");
                    entropyThread.setDaemon(true);
                    entropyThread.start();
                }
            }
            return new EntropySourceProvider()
            {
                public EntropySource get(int bitsRequired)
                {
                    return new HybridEntropySource(entropyDaemon, createBaseEntropySourceProvider(), bitsRequired);
                }
            };
        }
        else
        {
            return new EntropySourceProvider()
            {
                public EntropySource get(int bitsRequired)
                {
                    return new OneShotHybridEntropySource(createBaseEntropySourceProvider(), bitsRequired);
                }
            };
        }
    }

    /**
     * Return the current algorithm/services constraints.
     *
     * @return the algorithm/services constraints.
     */
    public static CryptoServicesConstraints getServicesConstraints()
    {
        return servicesConstraints.get();
    }

    /**
     * Check a service to make sure it meets the current constraints.
     *
     * @param cryptoService the service to be checked.
     * @throws CryptoServiceConstraintsException if the service violates the current constraints.
     */
    public static void checkConstraints(CryptoServiceProperties cryptoService)
    {
        servicesConstraints.get().check(cryptoService);
    }

    /**
     * Set the current algorithm constraints.
     */
    public static void setServicesConstraints(CryptoServicesConstraints constraints)
    {
        checkPermission(CanSetConstraints);

        CryptoServicesConstraints newConstraints = (constraints == null) ? noConstraintsImpl : constraints;

        if (preconfiguredConstraints)
        {
            if (Properties.isOverrideSet("org.bouncycastle.constraints.allow_override"))
            {
                servicesConstraints.set(newConstraints);
            }
            else
            {
                LOG.warning("attempt to override pre-configured constraints ignored");
            }
        }
        else
        {
            // TODO: should this only be allowed once?
            servicesConstraints.set(newConstraints);
        }
    }

    /**
     * Return the default value for a particular property if one exists. The look up is done on the thread's local
     * configuration first and then on the global configuration in no local configuration exists.
     *
     * @param property the property to look up.
     * @param <T>      the type to be returned
     * @return null if the property is not set, the default value otherwise,
     */
    public static <T> T getProperty(Property property)
    {
        Object[] values = lookupProperty(property);

        if (values != null)
        {
            return (T) values[0];
        }

        return null;
    }

    private static Object[] lookupProperty(Property property)
    {
        Map<String, Object[]> properties = threadProperties.get();
        Object[] values;

        if (properties == null || !properties.containsKey(property.name))
        {
            values = globalProperties.get(property.name);
        }
        else
        {
            values = properties.get(property.name);
        }
        return values;
    }

    /**
     * Return an array representing the current values for a sized property such as DH_DEFAULT_PARAMS or
     * DSA_DEFAULT_PARAMS.
     *
     * @param property the name of the property to look up.
     * @param <T>      the base type of the array to be returned.
     * @return null if the property is not set, an array of the current values otherwise.
     */
    public static <T> T[] getSizedProperty(Property property)
    {
        Object[] values = lookupProperty(property);

        if (values == null)
        {
            return null;
        }

        return (T[]) values.clone();
    }

    /**
     * Return the value for a specific size for a sized property such as DH_DEFAULT_PARAMS or
     * DSA_DEFAULT_PARAMS.
     *
     * @param property the name of the property to look up.
     * @param size     the size (in bits) of the defining value in the property type.
     * @param <T>      the type of the value to be returned.
     * @return the current value for the size, null if there is no value set,
     */
    public static <T> T getSizedProperty(Property property, int size)
    {
        Object[] values = lookupProperty(property);

        if (values == null)
        {
            return null;
        }

        if (property.type.isAssignableFrom(DHParameters.class))
        {
            for (int i = 0; i != values.length; i++)
            {
                DHParameters params = (DHParameters) values[i];

                if (params.getP().bitLength() == size)
                {
                    return (T) params;
                }
            }
        }
        else if (property.type.isAssignableFrom(DSAParameters.class))
        {
            for (int i = 0; i != values.length; i++)
            {
                DSAParameters params = (DSAParameters) values[i];

                if (params.getP().bitLength() == size)
                {
                    return (T) params;
                }
            }
        }

        return null;
    }

    /**
     * Set the value of the the passed in property on the current thread only. More than
     * one value can be passed in for a sized property. If more than one value is provided the
     * first value in the argument list becomes the default value.
     *
     * @param property      the name of the property to set.
     * @param propertyValue the values to assign to the property.
     * @param <T>           the base type of the property value.
     */
    public static <T> void setThreadProperty(Property property, T... propertyValue)
    {
        checkPermission(CanSetThreadProperty);

        if (!property.type.isAssignableFrom(propertyValue[0].getClass()))
        {
            throw new IllegalArgumentException("Bad property value passed");
        }

        localSetThread(property, propertyValue.clone());
    }

    /**
     * Set the value of the the passed in property globally in the JVM. More than
     * one value can be passed in for a sized property. If more than one value is provided the
     * first value in the argument list becomes the default value.
     *
     * @param property      the name of the property to set.
     * @param propertyValue the values to assign to the property.
     * @param <T>           the base type of the property value.
     */
    public static <T> void setGlobalProperty(Property property, T... propertyValue)
    {
        checkPermission(CanSetDefaultProperty);

        localSetGlobalProperty(property, propertyValue.clone());
    }

    private static <T> void localSetThread(Property property, T[] propertyValue)
    {
        Map<String, Object[]> properties = threadProperties.get();

        if (properties == null)
        {
            properties = new HashMap<String, Object[]>();
            threadProperties.set(properties);
        }

        properties.put(property.name, propertyValue);
    }

    private static <T> void localSetGlobalProperty(Property property, T... propertyValue)
    {
        if (!property.type.isAssignableFrom(propertyValue[0].getClass()))
        {
            throw new IllegalArgumentException("Bad property value passed");
        }

        // set the property for the current thread as well to avoid mass confusion
        localSetThread(property, propertyValue);

        globalProperties.put(property.name, propertyValue);
    }

    /**
     * Clear the global value for the passed in property.
     *
     * @param property the property to be cleared.
     * @param <T>      the base type of the property value
     * @return an array of T if a value was previously set, null otherwise.
     */
    public static <T> T[] clearGlobalProperty(Property property)
    {
        checkPermission(CanSetDefaultProperty);

        // clear the property for the current thread as well to avoid confusion
        localClearThreadProperty(property);

        return (T[]) globalProperties.remove(property.name);
    }

    /**
     * Clear the thread local value for the passed in property.
     *
     * @param property the property to be cleared.
     * @param <T>      the base type of the property value
     * @return an array of T if a value was previously set, null otherwise.
     */
    public static <T> T[] clearThreadProperty(Property property)
    {
        checkPermission(CanSetThreadProperty);

        return (T[]) localClearThreadProperty(property);
    }

    private static Object[] localClearThreadProperty(Property property)
    {
        Map<String, Object[]> properties = threadProperties.get();

        if (properties == null)
        {
            properties = new HashMap<String, Object[]>();
            threadProperties.set(properties);
        }

        return properties.remove(property.name);
    }

    private static void checkPermission(final Permission permission)
    {
        final SecurityManager securityManager = System.getSecurityManager();

        if (securityManager != null)
        {
            AccessController.doPrivileged(new PrivilegedAction<Object>()
            {
                public Object run()
                {
                    securityManager.checkPermission(permission);

                    return null;
                }
            });
        }
    }

    private static DHParameters toDH(DSAParameters dsaParams)
    {
        int pSize = dsaParams.getP().bitLength();
        int m = chooseLowerBound(pSize);
        return new DHParameters(dsaParams.getP(), dsaParams.getG(), dsaParams.getQ(), m, 0, null,
                new DHValidationParameters(dsaParams.getValidationParameters().getSeed(),
                        dsaParams.getValidationParameters().getCounter()));
    }

    // based on lower limit of at least 2^{2 * bits_of_security}
    private static int chooseLowerBound(int pSize)
    {
        int m = 160;
        if (pSize > 1024)
        {
            if (pSize <= 2048)
            {
                m = 224;
            }
            else if (pSize <= 3072)
            {
                m = 256;
            }
            else if (pSize <= 7680)
            {
                m = 384;
            }
            else
            {
                m = 512;
            }
        }
        return m;
    }

    private static CryptoServicesConstraints getDefaultConstraints()
    {
        // TODO: return one based on system/security properties if set.

        return noConstraintsImpl;
    }

    /**
     * Available properties that can be set.
     */
    public static final class Property
    {
        /**
         * The parameters to be used for processing implicitlyCA X9.62 parameters
         */
        public static final Property EC_IMPLICITLY_CA = new Property("ecImplicitlyCA", X9ECParameters.class);
        /**
         * The default parameters for a particular size of Diffie-Hellman key.This is a sized property.
         */
        public static final Property DH_DEFAULT_PARAMS = new Property("dhDefaultParams", DHParameters.class);
        /**
         * The default parameters for a particular size of DSA key. This is a sized property.
         */
        public static final Property DSA_DEFAULT_PARAMS = new Property("dsaDefaultParams", DSAParameters.class);
        private final String name;
        private final Class type;

        private Property(String name, Class type)
        {
            this.name = name;
            this.type = type;
        }
    }

    // Entropy source selection for Java 7 and before.

    // {"Provider class name","SecureRandomSpi class name"}
    private static final String[][] initialEntropySourceNames = new String[][]
            {
                    // Normal JVM
                    {"sun.security.provider.Sun", "sun.security.provider.SecureRandom"},
                    // Apache harmony
                    {"org.apache.harmony.security.provider.crypto.CryptoProvider", "org.apache.harmony.security" +
                            ".provider.crypto.SHA1PRNG_SecureRandomImpl"},
                    // Android.
                    {"com.android.org.conscrypt.OpenSSLProvider", "com.android.org.conscrypt.OpenSSLRandom"},
                    {"org.conscrypt.OpenSSLProvider", "org.conscrypt.OpenSSLRandom"},
            };

    // Cascade through providers looking for match.
    private final static Object[] findSource()
    {
        for (int t = 0; t < initialEntropySourceNames.length; t++)
        {
            String[] pair = initialEntropySourceNames[t];
            try
            {
                Object[] r = new Object[]{Class.forName(pair[0]).newInstance(), Class.forName(pair[1]).newInstance()};

                return r;
            }
            catch (Throwable ex)
            {
                continue;
            }
        }

        return null;
    }

    // unfortunately new SecureRandom() can cause a regress and it's the only reliable way of getting access
    // to the JVM's seed generator.
    private static EntropySourceProvider createBaseEntropySourceProvider()
    {
        String source = AccessController.doPrivileged(new PrivilegedAction<String>()
        {
            public String run()
            {
                return Security.getProperty("securerandom.source");
            }
        });

        if (source == null)
        {
            return createInternalEntropySourceProvider();
        }
        else
        {
            try
            {
                return new URLSeededEntropySourceProvider(new URL(source));
            }
            catch (Exception e)
            {
                return createInternalEntropySourceProvider();
            }
        }
    }

    private static IncrementalEntropySourceProvider createInternalEntropySourceProvider()
    {
        boolean hasGetInstanceStrong = AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {
            public Boolean run()
            {
                try
                {
                    Class def = SecureRandom.class;

                    return def.getMethod("getInstanceStrong") != null;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        });

        if (hasGetInstanceStrong)
        {
            SecureRandom strong = AccessController.doPrivileged(new PrivilegedAction<SecureRandom>()
            {
                public SecureRandom run()
                {
                    try
                    {
                        return (SecureRandom) SecureRandom.class.getMethod("getInstanceStrong").invoke(null);
                    }
                    catch (Exception e)
                    {
                        return new CoreSecureRandom(findSource());
                    }
                }
            });

            return new IncrementalEntropySourceProvider(strong, true);
        }
        else
        {
            return new IncrementalEntropySourceProvider(AccessController.doPrivileged(new PrivilegedAction<SecureRandom>()
            {
                public SecureRandom run()
                {
                    return new CoreSecureRandom(findSource());
                }
            }), true);
        }
    }

    private static class CoreSecureRandom
            extends SecureRandom
    {
        CoreSecureRandom(Object[] initialEntropySourceAndSpi)
        {
            super((SecureRandomSpi) initialEntropySourceAndSpi[1], (Provider) initialEntropySourceAndSpi[0]);
        }
    }

    private static EntropyDaemon entropyDaemon = null;
    private static Thread entropyThread = null;

    static
    {
        entropyDaemon = new EntropyDaemon();
        entropyThread = new Thread(entropyDaemon, "BC Entropy Daemon");
        entropyThread.setDaemon(true);
        entropyThread.start();
    }

    private static class HybridEntropySource
            implements EntropySource
    {
        private final AtomicBoolean seedAvailable = new AtomicBoolean(false);
        private final AtomicInteger samples = new AtomicInteger(0);

        private final SP80090DRBG drbg;
        private final SignallingEntropySource entropySource;
        private final int bytesRequired;
        private final byte[] additionalInput = Pack.longToBigEndian(System.currentTimeMillis());

        HybridEntropySource(final EntropyDaemon entropyDaemon, EntropySourceProvider entropyProvider,
                            final int bitsRequired)
        {
            bytesRequired = (bitsRequired + 7) / 8;
            // remember for the seed generator we need the correct security strength for SHA-512
            entropySource = new SignallingEntropySource(entropyDaemon, seedAvailable, entropyProvider, 256);
            drbg = new HMacSP800DRBG(new HMac(SHA512Digest.newInstance()), 256, entropySource, Strings.toByteArray("Bouncy Castle Hybrid Entropy Source"), entropySource.getEntropy());
        }

        @Override
        public boolean isPredictionResistant()
        {
            return true;
        }

        @Override
        public byte[] getEntropy()
        {
            byte[] entropy = new byte[bytesRequired];

            // after 20 samples we'll start to check if there is new seed material.
            if (samples.getAndIncrement() > 20)
            {
                if (seedAvailable.getAndSet(false))
                {
                    samples.set(0);
                    drbg.reseed(additionalInput);
                }
                else
                {
                    entropySource.schedule();
                }
            }

            // hard to imagine happening, can't afford it to though!
            if (drbg.generate(entropy, null, false) < 0)
            {
                drbg.reseed(additionalInput);
                drbg.generate(entropy, null, false);
            }

            return entropy;
        }

        @Override
        public int entropySize()
        {
            return bytesRequired * 8;
        }

        private class SignallingEntropySource
                implements EntropySource
        {
            private final EntropyDaemon entropyDaemon;
            private final AtomicBoolean seedAvailable;
            private final EntropySource entropySource;
            private final int byteLength;
            private final AtomicReference entropy = new AtomicReference();
            private final AtomicBoolean scheduled = new AtomicBoolean(false);

            SignallingEntropySource(EntropyDaemon entropyDaemon, AtomicBoolean seedAvailable,
                                    EntropySourceProvider baseRandom, int bitsRequired)
            {
                this.entropyDaemon = entropyDaemon;
                this.seedAvailable = seedAvailable;
                this.entropySource = baseRandom.get(bitsRequired);
                this.byteLength = (bitsRequired + 7) / 8;
            }

            public boolean isPredictionResistant()
            {
                return true;
            }

            public byte[] getEntropy()
            {
                byte[] seed = (byte[]) entropy.getAndSet(null);

                if (seed == null || seed.length != byteLength)
                {
                    seed = entropySource.getEntropy();
                }
                else
                {
                    scheduled.set(false);
                }

                schedule();

                return seed;
            }

            void schedule()
            {
                if (!scheduled.getAndSet(true))
                {
                    entropyDaemon.addTask(new EntropyGatherer(entropySource, seedAvailable, entropy));
                }
            }

            public int entropySize()
            {
                return byteLength * 8;
            }
        }
    }

    private static class URLSeededEntropySourceProvider
            implements EntropySourceProvider
    {
        private final InputStream seedStream;

        URLSeededEntropySourceProvider(final URL url)
        {
            this.seedStream = AccessController.doPrivileged(new PrivilegedAction<InputStream>()
            {
                public InputStream run()
                {
                    try
                    {
                        return url.openStream();
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException("unable to open random source");
                    }
                }
            });
        }

        private int privilegedRead(final byte[] data, final int off, final int len)
        {
            return AccessController.doPrivileged(new PrivilegedAction<Integer>()
            {
                public Integer run()
                {
                    try
                    {
                        return seedStream.read(data, off, len);
                    }
                    catch (IOException e)
                    {
                        throw new InternalError("unable to read random source");
                    }
                }
            });
        }

        public EntropySource get(final int bitsRequired)
        {
            return new IncrementalEntropySource()
            {
                public byte[] getEntropy(long pause)
                        throws InterruptedException
                {
                    byte[] seed = new byte[numBytes];
                    for (int i = 0; i < numBytes / 8; i++)
                    {
                        // we need to be mindful that we may not be the only thread/process looking for entropy
                        sleep(pause);
                        fetchEntropy(seed, i * 8, 8);
                    }

                    int extra = numBytes - ((numBytes / 8) * 8);
                    if (extra != 0)
                    {
                        sleep(pause);
                        fetchEntropy(seed, seed.length - extra, extra);
                    }

                    return seed;
                }

                private final int numBytes = (bitsRequired + 7) / 8;

                public boolean isPredictionResistant()
                {
                    return true;
                }

                public byte[] getEntropy()
                {
                    try
                    {
                        return getEntropy(0);
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("initial entropy fetch interrupted"); // should never happen
                    }
                }

                private void fetchEntropy(byte[] data, int dataOff, int length)
                {
                    int off = 0;
                    int len;

                    while (off != length && (len = privilegedRead(data, dataOff + off, length - off)) > -1)
                    {
                        off += len;
                    }

                    if (off != length)
                    {
                        throw new InternalError("unable to fully read random source");
                    }
                }

                public int entropySize()
                {
                    return bitsRequired;
                }
            };
        }
    }

    private static void sleep(long ms)
            throws InterruptedException
    {
        if (ms != 0)
        {
            Thread.sleep(ms);
        }
    }

    private static class OneShotHybridEntropySource
            implements EntropySource
    {
        private final AtomicBoolean seedAvailable = new AtomicBoolean(false);
        private final AtomicInteger samples = new AtomicInteger(0);

        private final SP80090DRBG drbg;
        private final SignallingEntropySource entropySource;
        private final int bytesRequired;
        private final byte[] additionalInput = Pack.longToBigEndian(System.currentTimeMillis());

        OneShotHybridEntropySource(EntropySourceProvider entropyProvider, final int bitsRequired)
        {
            bytesRequired = (bitsRequired + 7) / 8;
            // remember for the seed generator we need the correct security strength for SHA-512
            entropySource = new SignallingEntropySource(seedAvailable, entropyProvider, 256);
            drbg = new HMacSP800DRBG(new HMac(SHA512Digest.newInstance()), 256, entropySource, Strings.toByteArray("Bouncy Castle One Shot Entropy Source"), entropySource.getEntropy());
        }

        public boolean isPredictionResistant()
        {
            return true;
        }

        public byte[] getEntropy()
        {
            byte[] entropy = new byte[bytesRequired];

            // after 1024 samples we'll start to check if there is new seed material,
            // we do this less often than with the daemon based one due to the overheads.
            if (samples.getAndIncrement() > 1024)
            {
                if (seedAvailable.getAndSet(false))
                {
                    samples.set(0);
                    drbg.reseed(additionalInput);
                }
                else
                {
                    entropySource.schedule();
                }
            }

            // hard to imagine happening, can't afford it to though!
            if (drbg.generate(entropy, null, false) < 0)
            {
                drbg.reseed(additionalInput);
                drbg.generate(entropy, null, false);
            }

            return entropy;
        }

        public int entropySize()
        {
            return bytesRequired * 8;
        }

        private class SignallingEntropySource
                implements IncrementalEntropySource
        {
            private final AtomicBoolean seedAvailable;
            private final IncrementalEntropySource entropySource;
            private final int byteLength;
            private final AtomicReference entropy = new AtomicReference();
            private final AtomicBoolean scheduled = new AtomicBoolean(false);

            SignallingEntropySource(AtomicBoolean seedAvailable, EntropySourceProvider baseRandom, int bitsRequired)
            {
                this.seedAvailable = seedAvailable;
                this.entropySource = (IncrementalEntropySource) baseRandom.get(bitsRequired);
                this.byteLength = (bitsRequired + 7) / 8;
            }

            public boolean isPredictionResistant()
            {
                return true;
            }

            public byte[] getEntropy()
            {
                try
                {
                    return getEntropy(0);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("initial entropy fetch interrupted"); // should never happen
                }
            }

            public byte[] getEntropy(long pause)
                    throws InterruptedException
            {
                byte[] seed = (byte[]) entropy.getAndSet(null);

                if (seed == null || seed.length != byteLength)
                {
                    seed = entropySource.getEntropy(pause);
                }
                else
                {
                    scheduled.set(false);
                }

                return seed;
            }

            void schedule()
            {
                if (!scheduled.getAndSet(true))
                {
                    Thread thread = new Thread(new EntropyGatherer(entropySource, seedAvailable, entropy));
                    thread.setDaemon(true);
                    thread.start();
                }
            }

            public int entropySize()
            {
                return byteLength * 8;
            }
        }
    }

    private static class ThreadLocalSecureRandomProvider
            implements SecureRandomProvider
    {
        final ThreadLocal<SecureRandom> defaultRandoms = new ThreadLocal<SecureRandom>();

        public SecureRandom get()
        {
            if (defaultRandoms.get() == null)
            {
                defaultRandoms.set(new SecureRandom());
            }

            return defaultRandoms.get();
        }
    }
}
