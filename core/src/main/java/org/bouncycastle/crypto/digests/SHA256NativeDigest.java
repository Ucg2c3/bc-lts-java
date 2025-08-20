package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.CryptoServiceProperties;
import org.bouncycastle.crypto.CryptoServicePurpose;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.SavableDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.dispose.NativeDisposer;
import org.bouncycastle.util.dispose.NativeReference;

/**
 * SHA256 implementation.
 */
class SHA256NativeDigest
        implements SavableDigest
{
    private final CryptoServicePurpose purpose;

    private DigestRefWrapper nativeRef = null;

    SHA256NativeDigest(CryptoServicePurpose purpose)
    {
        this.purpose = purpose;
        nativeRef = new DigestRefWrapper(makeNative());
        reset();
        CryptoServicesRegistrar.checkConstraints(cryptoServiceProperties());
    }

    SHA256NativeDigest()
    {
        this(CryptoServicePurpose.ANY);
    }

    SHA256NativeDigest(SHA256NativeDigest src)
    {

        this(CryptoServicePurpose.ANY);

        byte[] state = src.getEncodedState();

        restoreFullState(nativeRef.getReference(), state, 0);
    }

    //
    // From BC-LTS, used for testing in FIPS api only.
    // ----------------------- Start Testing only methods.

    SHA256NativeDigest restoreState(byte[] state, int offset)
    {
        synchronized (this)
        {
            restoreFullState(nativeRef.getReference(), state, offset);
            return this;
        }
    }

    //
    // ----------------------- End Testing only methods.
    //

    @Override
    public String getAlgorithmName()
    {
        return "SHA-256";
    }

    @Override
    public int getDigestSize()
    {
        synchronized (this)
        {
            return getDigestSize(nativeRef.getReference());
        }
    }


    @Override
    public void update(byte in)
    {
        synchronized (this)
        {
            update(nativeRef.getReference(), in);
        }
    }


    @Override
    public void update(byte[] input, int inOff, int len)
    {
        synchronized (this)
        {
            update(nativeRef.getReference(), input, inOff, len);
        }
    }


    @Override
    public int doFinal(byte[] output, int outOff)
    {
        synchronized (this)
        {
            return doFinal(nativeRef.getReference(), output, outOff);
        }
    }


    @Override
    public void reset()
    {
        synchronized (this)
        {
            reset(nativeRef.getReference());
        }
    }


    @Override
    public int getByteLength()
    {
        synchronized (this)
        {
            return getByteLength(nativeRef.getReference());
        }
    }


    @Override
    public Memoable copy()
    {
        synchronized (this)
        {
            return new SHA256NativeDigest(this);
        }
    }

    @Override
    public void reset(Memoable other)
    {
        synchronized (this)
        {
            SHA256NativeDigest dig = (SHA256NativeDigest) other;
            restoreFullState(nativeRef.getReference(), dig.getEncodedState(), 0);
        }
    }


    public byte[] getEncodedState()
    {
        synchronized (this)
        {
            int l = encodeFullState(nativeRef.getReference(), null, 0);
            byte[] state = new byte[l];
            encodeFullState(nativeRef.getReference(), state, 0);
            return state;
        }
    }


    void restoreFullState(byte[] encoded, int offset)
    {
        synchronized (this)
        {
            restoreFullState(nativeRef.getReference(), encoded, offset);
        }
    }


    @Override
    public String toString()
    {
        return "SHA256[Native]()";
    }

    static native long makeNative();

    static native void destroy(long nativeRef);

    static native int getDigestSize(long nativeRef);

    static native void update(long nativeRef, byte in);

    static native void update(long nativeRef, byte[] in, int inOff, int len);

    static native int doFinal(long nativeRef, byte[] out, int outOff);

    static native void reset(long nativeRef);

    static native int getByteLength(long nativeRef);

    static native int encodeFullState(long nativeRef, byte[] buffer, int offset);

    static native void restoreFullState(long reference, byte[] encoded, int offset);

    protected CryptoServiceProperties cryptoServiceProperties()
    {
        return Utils.getDefaultProperties(this, 256, purpose);
    }


    private static class Disposer
            extends NativeDisposer
    {

        Disposer(long ref)
        {
            super(ref);
        }

        @Override
        protected void dispose(long reference)
        {
            destroy(reference);
        }
    }

    private static class DigestRefWrapper
            extends NativeReference
    {

        public DigestRefWrapper(long reference)
        {
            super(reference, "SHA256");
        }

        @Override
        public Runnable createAction()
        {
            return new Disposer(reference);
        }
    }
}






