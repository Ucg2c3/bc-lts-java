#include "org_bouncycastle_crypto_modes_AESNativeGCM.h"

#include "../gcm/gcm.h"
#include "../gcm/AesGcm.h"
#include "../../jniutil/JavaByteArray.h"
#include "../../exceptions/OutputLengthException.h"
#include "../../jniutil/JavaEnvUtils.h"
#include "../../exceptions/CipherTextException.h"


//
// NOTE:
// All input validation is done on the java side, this code is not intended to exist
// away from its java counterpart.
//


/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    reset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_reset
        (JNIEnv *, jobject, jlong ref) {
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    instance->reset(false);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    initNative
 * Signature: (JZ[B[B[BI)V
 */
JNIEXPORT void JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_initNative
        (JNIEnv *env, jclass, jlong ref, jboolean direction, jbyteArray key_, jbyteArray nonce_, jbyteArray aad_,
         jint macSizeInBits) {


    jniutil::JavaByteArray key(env, key_);
    jniutil::JavaByteArray nonce(env, nonce_);
    jniutil::JavaByteArray aad(env, aad_);

    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    instance->init(direction == JNI_TRUE,
                   key.uvalue(),
                   key.length(),
                   nonce.uvalue(),
                   nonce.length(),
                   aad.uvalue(),
                   aad.length(),
                   macSizeInBits / 8);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    makeInstance
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_makeInstance
        (JNIEnv *, jclass, jint keySize) {

    // TODO add key size implementations.

    auto instance = new intel::gcm::AesGcm();
    return (jlong) instance;
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    dispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_dispose
        (JNIEnv *, jclass, jlong ref) {
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    delete instance;
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    processAADByte
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_processAADByte
        (JNIEnv *, jclass, jlong ref, jbyte b) {
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    instance->processAADByte(b);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    processAADBytes
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_processAADBytes
        (JNIEnv *env, jclass, jlong ref, jbyteArray aad_, jint offset, jint len) {

    jniutil::JavaByteArray aad(env, aad_);
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    instance->processAADBytes(aad.uvalue(), offset, len);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    processByte
 * Signature: (JB[BI)I
 */
JNIEXPORT jint JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_processByte
        (JNIEnv *env, jclass, jlong ref, jbyte in, jbyteArray out_, jint outOff) {

    jniutil::JavaByteArray out(env, out_);
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    return (jint) instance->processByte(in, out.uvalue() + outOff, out.length());
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    processBytes
 * Signature: (J[BII[BI)I
 */
JNIEXPORT jint JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_processBytes
        (JNIEnv *env, jclass, jlong ref, jbyteArray in_, jint inOff, jint len, jbyteArray out_, jint outOff) {

    jniutil::JavaByteArray out(env, out_);
    jniutil::JavaByteArray in(env, in_);

    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    return (jint) instance->processBytes(in.uvalue(), inOff, len, out.uvalue(), outOff, out.length());

}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    doFinal
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_doFinal
        (JNIEnv *env, jclass, jlong ref, jbyteArray out_, jint outOff) {

    jniutil::JavaByteArray out(env, out_);

    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    try {
        return (jint) instance->doFinal(out.uvalue(), outOff, out.length());
    } catch (const exceptions::OutputLengthException &exp) {
        jniutil::JavaEnvUtils::throwException(env,
                                              "org/bouncycastle/crypto/OutputLengthException",
                                              exp.what());
    } catch (const exceptions::CipherTextException &exp) {
        jniutil::JavaEnvUtils::throwException(env,
                                              "org/bouncycastle/crypto/InvalidCipherTextException",
                                              exp.what());
    } catch (const std::runtime_error &err) {
        jniutil::JavaEnvUtils::throwIllegalArgumentException(env, err.what());
    }

    return 0;
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    getUpdateOutputSize
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_getUpdateOutputSize
        (JNIEnv *, jclass, jlong ref, jint len) {
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    return (jint) instance->getUpdateOutputSize(len);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    getOutputSize
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_getOutputSize
        (JNIEnv *, jclass, jlong ref, jint len) {
    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);
    return (jint) instance->getOutputSize(len);
}

/*
 * Class:     org_bouncycastle_crypto_engines_AESNativeGCM
 * Method:    getMac
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL  Java_org_bouncycastle_crypto_modes_AESNativeGCM_getMac
        (JNIEnv *env, jclass, jlong ref) {

    auto instance = static_cast<intel::gcm::GCM *>((void *) ref);

    jbyteArray out = env->NewByteArray((jint) instance->getMacLen());

    // Acquire elements.
    auto elements = env->GetByteArrayElements(out, nullptr);

    // Copy in value.
    instance->getMac((unsigned char *) elements);

    // Release elements
    env->ReleaseByteArrayElements(out, elements, 0);

    return out;

}