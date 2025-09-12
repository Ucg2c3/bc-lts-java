package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.CryptoServicePurpose;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.NativeServices;
import org.bouncycastle.crypto.SavableDigest;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

/**
 * implementation of SHA-3 based on following KeccakNISTInterface.c from https://keccak.noekeon.org/
 * <p>
 * Following the naming conventions used in the C source code to enable easy review of the implementation.
 */
public class SHA3Digest
    extends KeccakDigest
    implements SavableDigest
{
    private static int checkBitLength(int bitLength)
    {
        switch (bitLength)
        {
            case 224:
            case 256:
            case 384:
            case 512:
                return bitLength;
            default:
                throw new IllegalArgumentException("'bitLength' " + bitLength + " not supported for SHA-3");
        }
    }


    public static SavableDigest newInstance()
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3))
        {
            return new SHA3NativeDigest();
        }
        return new SHA3Digest();
    }


    public SHA3Digest()
    {
        this(256, CryptoServicePurpose.ANY);
    }


    public static SavableDigest newInstance(CryptoServicePurpose purpose)
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3))
        {
            return new SHA3NativeDigest(purpose);
        }
        return new SHA3Digest(purpose);
    }

    public SHA3Digest(CryptoServicePurpose purpose)
    {
        this(256, purpose);
    }


    public static SavableDigest newInstance(int bitlen)
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3))
        {
            return new SHA3NativeDigest(bitlen);
        }
        return new SHA3Digest(bitlen);
    }

    public SHA3Digest(int bitLength)
    {
        super(checkBitLength(bitLength), CryptoServicePurpose.ANY);
    }


    public static SavableDigest newInstance(int bitlen, CryptoServicePurpose purpose)
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3))
        {
            return new SHA3NativeDigest(bitlen, purpose);
        }
        return new SHA3Digest(bitlen, purpose);
    }

    public SHA3Digest(int bitLength, CryptoServicePurpose purpose)
    {
        super(checkBitLength(bitLength), purpose);
    }


    public static SavableDigest newInstance(Digest digest)
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3) && digest instanceof SHA3NativeDigest)
        {
            return new SHA3NativeDigest((SHA3NativeDigest) digest);
        }

        return new SHA3Digest((SHA3Digest) digest);
    }

    public SHA3Digest(SHA3Digest source)
    {
        super(source);
    }

    public SHA3Digest(byte[] encoded)
    {
        if (encoded.length != 1 + 12 + (state.length * 8))
        {
            throw new IllegalArgumentException("encoded state has incorrect length");
        }

        bitsInQueue = Pack.bigEndianToInt(encoded, 0);
        rate = Pack.bigEndianToInt(encoded, 4);
        squeezing = Integer.MAX_VALUE == Pack.bigEndianToInt(encoded, 8);
        fixedOutputLength = Pack.bigEndianToInt(encoded, 12);
        int p = 16;
        for (int t = 0; t < state.length; t++)
        {
            state[t] = Pack.bigEndianToInt(encoded, p += 8);
        }
        this.purpose = CryptoServicePurpose.values()[encoded[p]];
    }


    public static SavableDigest newInstance(byte[] encoded, CryptoServicePurpose purpose)
    {
        if (CryptoServicesRegistrar.hasEnabledService(NativeServices.SHA3))
        {
            return new SHA3NativeDigest(encoded, purpose);
        }
        return new SHA3Digest(encoded, purpose);
    }

    public SHA3Digest(byte[] encoded, CryptoServicePurpose purpose)
    {
        this(encoded);
        this.purpose = purpose;
    }


    public String getAlgorithmName()
    {
        return "SHA3-" + fixedOutputLength;
    }

    public int doFinal(byte[] out, int outOff)
    {
        absorbBits(0x02, 2);
        
        return super.doFinal(out,  outOff);
    }

    /*
     * TODO Possible API change to support partial-byte suffixes.
     */
    protected int doFinal(byte[] out, int outOff, byte partialByte, int partialBits)
    {
        if (partialBits < 0 || partialBits > 7)
        {
            throw new IllegalArgumentException("'partialBits' must be in the range [0,7]");
        }

        int finalInput = (partialByte & ((1 << partialBits) - 1)) | (0x02 << partialBits);
        int finalBits = partialBits + 2;

        if (finalBits >= 8)
        {
            absorb((byte)finalInput);
            finalBits -= 8;
            finalInput >>>= 8;
        }

        return super.doFinal(out, outOff, (byte)finalInput, finalBits);
    }

    public byte[] getEncodedState()
    {
        byte[] encState = new byte[state.length * 8 + dataQueue.length + 12 + 2];

        super.getEncodedState(encState);
        
        return encState;
    }

    public Memoable copy()
    {
        return new SHA3Digest(this);
    }

    public void reset(Memoable other)
    {
        SHA3Digest d = (SHA3Digest)other;

        copyIn(d);
    }
}
