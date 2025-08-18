package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.modes.CFBModeCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.dispose.NativeDisposer;
import org.bouncycastle.util.dispose.NativeReference;

class AESNativeCFB
        implements CFBModeCipher
{
    private final int bitSize;
    private CFBRefWrapper referenceWrapper;

    private byte[] oldIv;
    private boolean encrypting;

    public AESNativeCFB()
    {
        this(128);
    }

    public AESNativeCFB(int bitSize)
    {
        this.bitSize = bitSize;
        switch (bitSize)
        {
            case 128:
                break;
            default:
                throw new IllegalArgumentException("native feedback bit size can only be 128");
        }
    }

    @Override
    public void init(boolean forEncryption, CipherParameters params)
            throws IllegalArgumentException
    {

        boolean oldEncrypting = this.encrypting;

        this.encrypting = forEncryption;

        byte[] key = null;
        byte[] iv = null;

        if (params instanceof ParametersWithIV)
        {
            ParametersWithIV ivParam = (ParametersWithIV) params;
            iv = ivParam.getIV();

            if (iv.length > getBlockSize() || iv.length < 1)
            {
                throw new IllegalArgumentException("initialisation vector must be between one and block size length");
            }

            if (iv.length < getBlockSize())
            {
                byte[] newIv = new byte[getBlockSize()];
                System.arraycopy(iv, 0, newIv, newIv.length - iv.length, iv.length);
                iv = newIv;
            }

            oldIv = Arrays.clone(iv);

            if (ivParam.getParameters() != null)
            {
                key = Arrays.clone(((KeyParameter) ivParam.getParameters()).getKey());
            }

            if (key != null)
            {
                oldEncrypting = encrypting; // Can change because key is supplied.
                key = Arrays.clone(key);
            }
            else
            {
                // Use old key, it may be null but that is tested later.
                key =  referenceWrapper!=null? referenceWrapper.getKey():null;
            }
        }
        else
        {
            //
            // Change of key.
            //

            if (params instanceof KeyParameter)
            {
                key = Arrays.clone(((KeyParameter) params).getKey());
                iv = oldIv;
            }

        }

        if (key == null && oldEncrypting != encrypting)
        {
            throw new IllegalArgumentException("cannot change encrypting state without providing key.");
        }

        if (iv == null)
        {
            throw new IllegalArgumentException("iv is null");
        }


        referenceWrapper = new CFBRefWrapper(makeNative(encrypting, key.length), key);
        init(referenceWrapper.getReference(), key, iv);

    }


    @Override
    public String getAlgorithmName()
    {
        return "AES/CFB";
    }

    @Override
    public byte returnByte(byte in)
    {
        return processByte(referenceWrapper.getReference(), in);
    }

    @Override
    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff)
            throws DataLengthException
    {

        if (referenceWrapper == null)
        {
            throw new IllegalStateException("not initialized");
        }


        return processBytes(referenceWrapper.getReference(), in, inOff, len, out, outOff);
    }

    @Override
    public int getBlockSize()
    {
        return bitSize / 8;
    }


    @Override
    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
            throws DataLengthException, IllegalStateException
    {

        if (referenceWrapper == null)
        {
            throw new IllegalStateException("not initialized");
        }

        return processBytes(referenceWrapper.getReference(), in, inOff, getBlockSize(), out, outOff);
    }

    @Override
    public void reset()
    {
        // skip over spurious resets that may occur before init is called.
        if (referenceWrapper == null)
        {
            return;
        }

        reset(referenceWrapper.getReference());

    }


    @Override
    public int getMultiBlockSize()
    {
        return getNativeMultiBlockSize();
    }

    @Override
    public int processBlocks(byte[] in, int inOff, int blockCount, byte[] out, int outOff)
            throws DataLengthException, IllegalStateException
    {


        if (referenceWrapper == null)
        {
            throw new IllegalStateException("CFB engine not initialized");
        }

        return processBytes(in, inOff, blockCount * getBlockSize(), out, outOff);
    }


    private static native byte processByte(long ref, byte in);

    private static native int processBytes(long ref, byte[] in, int inOff, int len, byte[] out, int outOff)
            throws DataLengthException;

    static native long makeNative(boolean encrypting, int keyLen);

    native void init(long nativeRef, byte[] key, byte[] iv);

    static native void dispose(long ref);

    static native int getNativeMultiBlockSize();

    private static native void reset(long nativeRef);


    private static class Disposer
            extends NativeDisposer
    {
        private final byte[] key;

        Disposer(long ref, byte[] key)
        {
            super(ref);
            this.key = key;
        }

        @Override
        protected void dispose(long reference)
        {
            Arrays.clear(key);
            AESNativeCFB.dispose(reference);
        }
    }

    private static class CFBRefWrapper
            extends NativeReference
    {

        private final byte[] key;

        public CFBRefWrapper(long reference, byte[] key)
        {
            super(reference, "CFB");
            this.key = key;
        }

        public byte[] getKey()
        {
            return key;
        }

        @Override
        public Runnable createAction()
        {
            return new Disposer(reference, key);
        }
    }

    public String toString()
    {
        if (referenceWrapper != null && referenceWrapper.getKey() != null)
        {
            return "CFB[Native](AES[Native](" + (referenceWrapper.getKey().length * 8) + "))";
        }
        return "CFB[Native](AES[Native](not initialized))";
    }

}
