//
// Created  on 7/6/2022.
//

#ifndef BCFIPS_0_0_AESCBC_H
#define BCFIPS_0_0_AESCBC_H


#include <emmintrin.h>
#include <wmmintrin.h>
#include <jni_md.h>
#include "CBCNarrow.h"


namespace intel {
    namespace cbc {


        class AesCBC128Enc : public CBCNarrow {
        private:



        public:
            AesCBC128Enc();

            ~AesCBC128Enc() override;

            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;

        };

        class AesCBC128Dec : public CBCNarrow {
        private:



        public:
            AesCBC128Dec();

            ~AesCBC128Dec() override;

            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;
        };


        class AesCBC192Enc : public CBCNarrow {
        private:


        public:
            AesCBC192Enc();

            ~AesCBC192Enc() override;
            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;

        };

        class AesCBC192Dec : public CBCNarrow {
        private:


        public:
            AesCBC192Dec();

            ~AesCBC192Dec() override;

            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;


        };

        class AesCBC256Enc : public CBCNarrow {
        private:



        public:
            AesCBC256Enc();

            ~AesCBC256Enc() override;

            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;

        };

        class AesCBC256Dec : public CBCNarrow {
        private:




        public:
            AesCBC256Dec();

            ~AesCBC256Dec() override;

            size_t processBlock(unsigned char *in, uint32_t blocks, unsigned char *out) override;

        };

    }
}

#endif //BCFIPS_0_0_AESCBC_H
