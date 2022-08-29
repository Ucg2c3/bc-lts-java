package org.bouncycastle.pqc.crypto.falcon;

class SHAKE256
{
    long[] A;
    byte[] dbuf;
    long dptr;

    SHAKE256()
    {
        this.A = new long[25];
        this.dbuf = new byte[200];
        this.dptr = 0;
    }

    /*
     * Round constants.
     */
    private long RC[] = {
        0x0000000000000001l, 0x0000000000008082l,
        0x800000000000808Al, 0x8000000080008000l,
        0x000000000000808Bl, 0x0000000080000001l,
        0x8000000080008081l, 0x8000000000008009l,
        0x000000000000008Al, 0x0000000000000088l,
        0x0000000080008009l, 0x000000008000000Al,
        0x000000008000808Bl, 0x800000000000008Bl,
        0x8000000000008089l, 0x8000000000008003l,
        0x8000000000008002l, 0x8000000000000080l,
        0x000000000000800Al, 0x800000008000000Al,
        0x8000000080008081l, 0x8000000000008080l,
        0x0000000080000001l, 0x8000000080008008l
    };

    /*
     * Process the provided state.
     */
    void process_block(long[] A)
    {
        long t0, t1, t2, t3, t4;
        long tt0, tt1, tt2, tt3;
        long t, kt;
        long c0, c1, c2, c3, c4, bnn;
        int j;

        /*
         * Invert some words (alternate internal representation, which
         * saves some operations).
         */
        A[1] = ~A[1];
        A[2] = ~A[2];
        A[8] = ~A[8];
        A[12] = ~A[12];
        A[17] = ~A[17];
        A[20] = ~A[20];

        /*
         * Compute the 24 rounds. This loop is partially unrolled (each
         * iteration computes two rounds).
         */
        for (j = 0; j < 24; j += 2)
        {

            tt0 = A[1] ^ A[6];
            tt1 = A[11] ^ A[16];
            tt0 ^= A[21] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[4] ^ A[9];
            tt3 = A[14] ^ A[19];
            tt0 ^= A[24];
            tt2 ^= tt3;
            t0 = tt0 ^ tt2;

            tt0 = A[2] ^ A[7];
            tt1 = A[12] ^ A[17];
            tt0 ^= A[22] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[0] ^ A[5];
            tt3 = A[10] ^ A[15];
            tt0 ^= A[20];
            tt2 ^= tt3;
            t1 = tt0 ^ tt2;

            tt0 = A[3] ^ A[8];
            tt1 = A[13] ^ A[18];
            tt0 ^= A[23] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[1] ^ A[6];
            tt3 = A[11] ^ A[16];
            tt0 ^= A[21];
            tt2 ^= tt3;
            t2 = tt0 ^ tt2;

            tt0 = A[4] ^ A[9];
            tt1 = A[14] ^ A[19];
            tt0 ^= A[24] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[2] ^ A[7];
            tt3 = A[12] ^ A[17];
            tt0 ^= A[22];
            tt2 ^= tt3;
            t3 = tt0 ^ tt2;

            tt0 = A[0] ^ A[5];
            tt1 = A[10] ^ A[15];
            tt0 ^= A[20] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[3] ^ A[8];
            tt3 = A[13] ^ A[18];
            tt0 ^= A[23];
            tt2 ^= tt3;
            t4 = tt0 ^ tt2;

            A[0] = A[0] ^ t0;
            A[5] = A[5] ^ t0;
            A[10] = A[10] ^ t0;
            A[15] = A[15] ^ t0;
            A[20] = A[20] ^ t0;
            A[1] = A[1] ^ t1;
            A[6] = A[6] ^ t1;
            A[11] = A[11] ^ t1;
            A[16] = A[16] ^ t1;
            A[21] = A[21] ^ t1;
            A[2] = A[2] ^ t2;
            A[7] = A[7] ^ t2;
            A[12] = A[12] ^ t2;
            A[17] = A[17] ^ t2;
            A[22] = A[22] ^ t2;
            A[3] = A[3] ^ t3;
            A[8] = A[8] ^ t3;
            A[13] = A[13] ^ t3;
            A[18] = A[18] ^ t3;
            A[23] = A[23] ^ t3;
            A[4] = A[4] ^ t4;
            A[9] = A[9] ^ t4;
            A[14] = A[14] ^ t4;
            A[19] = A[19] ^ t4;
            A[24] = A[24] ^ t4;
            A[5] = (A[5] << 36) | (A[5] >>> (64 - 36));
            A[10] = (A[10] << 3) | (A[10] >>> (64 - 3));
            A[15] = (A[15] << 41) | (A[15] >>> (64 - 41));
            A[20] = (A[20] << 18) | (A[20] >>> (64 - 18));
            A[1] = (A[1] << 1) | (A[1] >>> (64 - 1));
            A[6] = (A[6] << 44) | (A[6] >>> (64 - 44));
            A[11] = (A[11] << 10) | (A[11] >>> (64 - 10));
            A[16] = (A[16] << 45) | (A[16] >>> (64 - 45));
            A[21] = (A[21] << 2) | (A[21] >>> (64 - 2));
            A[2] = (A[2] << 62) | (A[2] >>> (64 - 62));
            A[7] = (A[7] << 6) | (A[7] >>> (64 - 6));
            A[12] = (A[12] << 43) | (A[12] >>> (64 - 43));
            A[17] = (A[17] << 15) | (A[17] >>> (64 - 15));
            A[22] = (A[22] << 61) | (A[22] >>> (64 - 61));
            A[3] = (A[3] << 28) | (A[3] >>> (64 - 28));
            A[8] = (A[8] << 55) | (A[8] >>> (64 - 55));
            A[13] = (A[13] << 25) | (A[13] >>> (64 - 25));
            A[18] = (A[18] << 21) | (A[18] >>> (64 - 21));
            A[23] = (A[23] << 56) | (A[23] >>> (64 - 56));
            A[4] = (A[4] << 27) | (A[4] >>> (64 - 27));
            A[9] = (A[9] << 20) | (A[9] >>> (64 - 20));
            A[14] = (A[14] << 39) | (A[14] >>> (64 - 39));
            A[19] = (A[19] << 8) | (A[19] >>> (64 - 8));
            A[24] = (A[24] << 14) | (A[24] >>> (64 - 14));

            bnn = ~A[12];
            kt = A[6] | A[12];
            c0 = A[0] ^ kt;
            kt = bnn | A[18];
            c1 = A[6] ^ kt;
            kt = A[18] & A[24];
            c2 = A[12] ^ kt;
            kt = A[24] | A[0];
            c3 = A[18] ^ kt;
            kt = A[0] & A[6];
            c4 = A[24] ^ kt;
            A[0] = c0;
            A[6] = c1;
            A[12] = c2;
            A[18] = c3;
            A[24] = c4;
            bnn = ~A[22];
            kt = A[9] | A[10];
            c0 = A[3] ^ kt;
            kt = A[10] & A[16];
            c1 = A[9] ^ kt;
            kt = A[16] | bnn;
            c2 = A[10] ^ kt;
            kt = A[22] | A[3];
            c3 = A[16] ^ kt;
            kt = A[3] & A[9];
            c4 = A[22] ^ kt;
            A[3] = c0;
            A[9] = c1;
            A[10] = c2;
            A[16] = c3;
            A[22] = c4;
            bnn = ~A[19];
            kt = A[7] | A[13];
            c0 = A[1] ^ kt;
            kt = A[13] & A[19];
            c1 = A[7] ^ kt;
            kt = bnn & A[20];
            c2 = A[13] ^ kt;
            kt = A[20] | A[1];
            c3 = bnn ^ kt;
            kt = A[1] & A[7];
            c4 = A[20] ^ kt;
            A[1] = c0;
            A[7] = c1;
            A[13] = c2;
            A[19] = c3;
            A[20] = c4;
            bnn = ~A[17];
            kt = A[5] & A[11];
            c0 = A[4] ^ kt;
            kt = A[11] | A[17];
            c1 = A[5] ^ kt;
            kt = bnn | A[23];
            c2 = A[11] ^ kt;
            kt = A[23] & A[4];
            c3 = bnn ^ kt;
            kt = A[4] | A[5];
            c4 = A[23] ^ kt;
            A[4] = c0;
            A[5] = c1;
            A[11] = c2;
            A[17] = c3;
            A[23] = c4;
            bnn = ~A[8];
            kt = bnn & A[14];
            c0 = A[2] ^ kt;
            kt = A[14] | A[15];
            c1 = bnn ^ kt;
            kt = A[15] & A[21];
            c2 = A[14] ^ kt;
            kt = A[21] | A[2];
            c3 = A[15] ^ kt;
            kt = A[2] & A[8];
            c4 = A[21] ^ kt;
            A[2] = c0;
            A[8] = c1;
            A[14] = c2;
            A[15] = c3;
            A[21] = c4;
            A[0] = A[0] ^ RC[j + 0];

            tt0 = A[6] ^ A[9];
            tt1 = A[7] ^ A[5];
            tt0 ^= A[8] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[24] ^ A[22];
            tt3 = A[20] ^ A[23];
            tt0 ^= A[21];
            tt2 ^= tt3;
            t0 = tt0 ^ tt2;

            tt0 = A[12] ^ A[10];
            tt1 = A[13] ^ A[11];
            tt0 ^= A[14] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[0] ^ A[3];
            tt3 = A[1] ^ A[4];
            tt0 ^= A[2];
            tt2 ^= tt3;
            t1 = tt0 ^ tt2;

            tt0 = A[18] ^ A[16];
            tt1 = A[19] ^ A[17];
            tt0 ^= A[15] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[6] ^ A[9];
            tt3 = A[7] ^ A[5];
            tt0 ^= A[8];
            tt2 ^= tt3;
            t2 = tt0 ^ tt2;

            tt0 = A[24] ^ A[22];
            tt1 = A[20] ^ A[23];
            tt0 ^= A[21] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[12] ^ A[10];
            tt3 = A[13] ^ A[11];
            tt0 ^= A[14];
            tt2 ^= tt3;
            t3 = tt0 ^ tt2;

            tt0 = A[0] ^ A[3];
            tt1 = A[1] ^ A[4];
            tt0 ^= A[2] ^ tt1;
            tt0 = (tt0 << 1) | (tt0 >>> 63);
            tt2 = A[18] ^ A[16];
            tt3 = A[19] ^ A[17];
            tt0 ^= A[15];
            tt2 ^= tt3;
            t4 = tt0 ^ tt2;

            A[0] = A[0] ^ t0;
            A[3] = A[3] ^ t0;
            A[1] = A[1] ^ t0;
            A[4] = A[4] ^ t0;
            A[2] = A[2] ^ t0;
            A[6] = A[6] ^ t1;
            A[9] = A[9] ^ t1;
            A[7] = A[7] ^ t1;
            A[5] = A[5] ^ t1;
            A[8] = A[8] ^ t1;
            A[12] = A[12] ^ t2;
            A[10] = A[10] ^ t2;
            A[13] = A[13] ^ t2;
            A[11] = A[11] ^ t2;
            A[14] = A[14] ^ t2;
            A[18] = A[18] ^ t3;
            A[16] = A[16] ^ t3;
            A[19] = A[19] ^ t3;
            A[17] = A[17] ^ t3;
            A[15] = A[15] ^ t3;
            A[24] = A[24] ^ t4;
            A[22] = A[22] ^ t4;
            A[20] = A[20] ^ t4;
            A[23] = A[23] ^ t4;
            A[21] = A[21] ^ t4;
            A[3] = (A[3] << 36) | (A[3] >>> (64 - 36));
            A[1] = (A[1] << 3) | (A[1] >>> (64 - 3));
            A[4] = (A[4] << 41) | (A[4] >>> (64 - 41));
            A[2] = (A[2] << 18) | (A[2] >>> (64 - 18));
            A[6] = (A[6] << 1) | (A[6] >>> (64 - 1));
            A[9] = (A[9] << 44) | (A[9] >>> (64 - 44));
            A[7] = (A[7] << 10) | (A[7] >>> (64 - 10));
            A[5] = (A[5] << 45) | (A[5] >>> (64 - 45));
            A[8] = (A[8] << 2) | (A[8] >>> (64 - 2));
            A[12] = (A[12] << 62) | (A[12] >>> (64 - 62));
            A[10] = (A[10] << 6) | (A[10] >>> (64 - 6));
            A[13] = (A[13] << 43) | (A[13] >>> (64 - 43));
            A[11] = (A[11] << 15) | (A[11] >>> (64 - 15));
            A[14] = (A[14] << 61) | (A[14] >>> (64 - 61));
            A[18] = (A[18] << 28) | (A[18] >>> (64 - 28));
            A[16] = (A[16] << 55) | (A[16] >>> (64 - 55));
            A[19] = (A[19] << 25) | (A[19] >>> (64 - 25));
            A[17] = (A[17] << 21) | (A[17] >>> (64 - 21));
            A[15] = (A[15] << 56) | (A[15] >>> (64 - 56));
            A[24] = (A[24] << 27) | (A[24] >>> (64 - 27));
            A[22] = (A[22] << 20) | (A[22] >>> (64 - 20));
            A[20] = (A[20] << 39) | (A[20] >>> (64 - 39));
            A[23] = (A[23] << 8) | (A[23] >>> (64 - 8));
            A[21] = (A[21] << 14) | (A[21] >>> (64 - 14));

            bnn = ~A[13];
            kt = A[9] | A[13];
            c0 = A[0] ^ kt;
            kt = bnn | A[17];
            c1 = A[9] ^ kt;
            kt = A[17] & A[21];
            c2 = A[13] ^ kt;
            kt = A[21] | A[0];
            c3 = A[17] ^ kt;
            kt = A[0] & A[9];
            c4 = A[21] ^ kt;
            A[0] = c0;
            A[9] = c1;
            A[13] = c2;
            A[17] = c3;
            A[21] = c4;
            bnn = ~A[14];
            kt = A[22] | A[1];
            c0 = A[18] ^ kt;
            kt = A[1] & A[5];
            c1 = A[22] ^ kt;
            kt = A[5] | bnn;
            c2 = A[1] ^ kt;
            kt = A[14] | A[18];
            c3 = A[5] ^ kt;
            kt = A[18] & A[22];
            c4 = A[14] ^ kt;
            A[18] = c0;
            A[22] = c1;
            A[1] = c2;
            A[5] = c3;
            A[14] = c4;
            bnn = ~A[23];
            kt = A[10] | A[19];
            c0 = A[6] ^ kt;
            kt = A[19] & A[23];
            c1 = A[10] ^ kt;
            kt = bnn & A[2];
            c2 = A[19] ^ kt;
            kt = A[2] | A[6];
            c3 = bnn ^ kt;
            kt = A[6] & A[10];
            c4 = A[2] ^ kt;
            A[6] = c0;
            A[10] = c1;
            A[19] = c2;
            A[23] = c3;
            A[2] = c4;
            bnn = ~A[11];
            kt = A[3] & A[7];
            c0 = A[24] ^ kt;
            kt = A[7] | A[11];
            c1 = A[3] ^ kt;
            kt = bnn | A[15];
            c2 = A[7] ^ kt;
            kt = A[15] & A[24];
            c3 = bnn ^ kt;
            kt = A[24] | A[3];
            c4 = A[15] ^ kt;
            A[24] = c0;
            A[3] = c1;
            A[7] = c2;
            A[11] = c3;
            A[15] = c4;
            bnn = ~A[16];
            kt = bnn & A[20];
            c0 = A[12] ^ kt;
            kt = A[20] | A[4];
            c1 = bnn ^ kt;
            kt = A[4] & A[8];
            c2 = A[20] ^ kt;
            kt = A[8] | A[12];
            c3 = A[4] ^ kt;
            kt = A[12] & A[16];
            c4 = A[8] ^ kt;
            A[12] = c0;
            A[16] = c1;
            A[20] = c2;
            A[4] = c3;
            A[8] = c4;
            A[0] = A[0] ^ RC[j + 1];
            t = A[5];
            A[5] = A[18];
            A[18] = A[11];
            A[11] = A[10];
            A[10] = A[6];
            A[6] = A[22];
            A[22] = A[20];
            A[20] = A[12];
            A[12] = A[19];
            A[19] = A[15];
            A[15] = A[24];
            A[24] = A[8];
            A[8] = t;
            t = A[1];
            A[1] = A[9];
            A[9] = A[14];
            A[14] = A[2];
            A[2] = A[13];
            A[13] = A[23];
            A[23] = A[4];
            A[4] = A[21];
            A[21] = A[16];
            A[16] = A[3];
            A[3] = A[17];
            A[17] = A[7];
            A[7] = t;
        }

        /*
         * Invert some words back to normal representation.
         */
        A[1] = ~A[1];
        A[2] = ~A[2];
        A[8] = ~A[8];
        A[12] = ~A[12];
        A[17] = ~A[17];
        A[20] = ~A[20];
    }


    /* see inner.h */
    void inner_shake256_init()
    {
        this.dptr = 0;

        /*
         * Representation of an all-ones uint64_t is the same regardless
         * of local endianness.
         */
        for (int i = 0; i < this.A.length; i++)
        {
            this.A[i] = 0;
        }
    }

    /* see inner.h */
    void inner_shake256_inject(byte[] srcin, int in, int len)
    {
        long dptr;

        dptr = this.dptr;
        while (len > 0)
        {
            long clen, u;

            clen = 136 - dptr;
            if (clen > len)
            {
                clen = len;
            }
            for (u = 0; u < clen; u++)
            {
                long v;

                v = u + dptr;
                this.A[(int)(v >> 3)] ^= Byte.toUnsignedLong(srcin[in + (int)u]) << ((v & 7) << 3);
            }
            dptr += clen;
            in += clen;
            len -= clen;
            if (dptr == 136)
            {
                process_block(this.A);
                dptr = 0;
            }
        }
        this.dptr = dptr;
    }

    /* see falcon.h */
    void i_shake256_flip()
    {
        /*
         * We apply padding and pre-XOR the value into the state. We
         * set dptr to the end of the buffer, so that first call to
         * shake_extract() will process the block.
         */
        int v;

        v = (int)this.dptr;
        this.A[v >> 3] ^= Integer.toUnsignedLong(0x1F) << ((v & 7) << 3);
        this.A[16] ^= Integer.toUnsignedLong(0x80) << 56;
        this.dptr = 136;
    }

    /* see falcon.h */
    void inner_shake256_extract(byte[] srcout, int out, int len)
    {
        int dptr;
        int o = out;

        dptr = (int)this.dptr;
        while (len > 0)
        {
            int clen;

            if (dptr == 136)
            {
                process_block(this.A);
                dptr = 0;
            }
            clen = 136 - dptr;
            if (clen > len)
            {
                clen = len;
            }
            len -= clen;
            while (clen-- > 0)
            {
                srcout[o++] = (byte)(this.A[dptr >> 3] >>> ((dptr & 7) << 3));
                dptr++;
            }
        }
        this.dptr = dptr;
    }

}
