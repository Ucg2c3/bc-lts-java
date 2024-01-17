# The Bouncy Castle Crypto Package For Java LTS

## Environmental variables (IMPORTANT):

JAVA_HOME needs to point to a Java 17 installation, this is required for the build tools.
The java code will be complied to be compatible with Java 8 (Version 52) and above.

This build uses Java 8, Java 11 and Java 15, so the following environmental variables need to be defined:

LTS_JDK8 -- Java 8 Home

LTS_JDK11 -- Java 11 Home

LTS_JDK15 -- Java 15 Home

Variables must point to the equivalent of JAVA_HOME for each of those Java JDKs.

The gradle (7.6.xx) build script does a sanity test on the presence of those variables, it does not validate
they are set to a valid value, and you may experience other errors if they are set incorrectly.

Running gradle build without setting a variable, will result in an error similar to:

```
* What went wrong:
A problem occurred evaluating root project 'bc-fips-java'.
> Looking for JDK ENV 'LTS_JDK8' but found null

```

When the variables are set:

```
Looking for JDK ENV 'LTS_JDK8' found  /Users/auser/openjdk/zulu8.64.0.19-ca-jdk8.0.345-macosx_aarch64
Looking for JDK ENV 'LTS_JDK11' found  /Users/auser/openjdk/zulu11.58.23-ca-jdk11.0.16.1-macosx_aarch64
Looking for JDK ENV 'LTS_JDK15' found  /Users/auser/openjdk/zulu15.42.15-ca-jdk15.0.8-macosx_aarch64
```

# Native support

## Create jar with native libraries

Before creating the jar the native libraries will need to be built. This will need to be done on each target platform
specifically.

### Building on linux

Before building the native code components, you will need to have the following installed:

Linux:

- build essentials
- cmake
- nasm
- gcc

Windows:

- TBD

#### Process:

##### Step 1 Create native headers

The native headers need to be created first before the native library can be built,
these are generated by the JVM so that JNI calls can be implemented with the correct
signatures on the native side.

Change into the ```<base_dir>/``` directory and run:

```
./gradlew clean compileJava -x test

# Run the tests later
```


This will compile the java code and build the headers, the tests will be run during Step 3.

##### Step 2 Build native lib

Change into the ```<base_dir>/native``` directory.


```
# For linux Intel & ARM
./build_linux.sh
```

```
# For darwin (OSX), ARM only
./build_osx.sh
```

Remember: To ```cd ..``` and step back to the root of the project.

This will create the native libraries and install them into the correct location for them to be
bundled into the LTS jar.

##### Step 3 Build LTS jar

###### Java only version

Change into the ```<base_dir>/``` directory.

```
./gradlew clean cleanNative build copyJars
```

This will build a java only variation and copy all the build jars into ```../bc-lts-java-jars/``` directory. The name of the jar is defined with
the ```version=2.73.4-SNAPSHOT``` version property in ```gradle.properties```.

Running the DumpInfo class from the jar will report something similar to:

```
java -cp ../bc-lts-java-jars/2.73.4-SNAPSHOT/bcprov-lts8on-2.73.4-SNAPSHOT.jar org.bouncycastle.util.DumpInfo
        
BouncyCastle APIs (LTS edition) v2.73.4-SNAPSHOT
Native Features: None

```


###### Java with native capabilities build

Before doing this, please make sure you have built the native capabilities, see Step 2, above.

Building a jar with native capabilities exercising only the java core tests, running the core tests against native
capabilities is covered next.

```
 ./gradlew clean cleanNative withNative build copyJars
```

After building a jar with the native libraries bundled in you can verify they are getting loaded by
doing the following on an ARM based Mac:

```
#Running:

java -cp ../bc-lts-java-jars/2.73.4-SNAPSHOT/bcprov-lts8on-2.73.4-SNAPSHOT.jar org.bouncycastle.util.DumpInfo -a
 
BouncyCastle APIs (LTS edition) v2.73.4-SNAPSHOT
Native Build Date: 2024-01-15T15:33:43
Native Status: READY
Native Variant: neon-le
Native Features: AES/CBC AES/CCM AES/CFB AES/CTR AES/ECB AES/GCM MULACC SHA2 SHA224 SHA256 SHA3 SHA384 SHA512 SHAKE


CPU Features and Variant availability.
--------------------------------------------------------------------------------
Variant   CPU features + or -:                              Supported           
--------------------------------------------------------------------------------
neon-le   +aes +sha256 +sha512 +sha3 +neon                  Variant Supported

```

On an Intel machine:

```
BouncyCastle APIs (LTS edition) v2.73.4-SNAPSHOT
Native Build Date: 2024-01-11T19:24:03
Native Status: READY
Native Variant: vaesf
Native Features: AES/CBC AES/CBC-PC AES/CCM AES/CCM-PC AES/CFB AES/CFB-PC AES/CTR AES/CTR-PC AES/ECB AES/GCM AES/GCM-PC AES/GCM-SIV AES/GCMSIV-PC DRBG NRBG SHA2 SHA224 SHA256


CPU Features and Variant availability.
--------------------------------------------------------------------------------
Variant   CPU features + or -:                              Supported           
--------------------------------------------------------------------------------
VAESF     +vaes +avx512f +avx512bw +vpclmulqdq              Variant supported
VAES      +vaes                                             Variant supported
AVX       +avx                                              Variant supported

```



###### Building and or testing native capabilities

Use the following test targets to exercise the tests while using specific sets of native components
compiled for certain CPU features.

Table of variants:

| Variant Name | Family | Requirements                                            | Test Target |
|--------------|--------|---------------------------------------------------------|-------------|
| avx          | x86_64 | avx sha aes pclmul rdrnd -rdseed lzcnt                  | testAVX     |
| vaes         | x86_64 | avx sha aes pclmul rdrnd rdseed lzcnt vaes avx2         | testVAES    |
| vaesf        | x86_64 | avx sha aes pclmul rdrnd rdseed lzcnt vaes avx2 avx512f | testVAESF   |
| neon-le      | ARM64  | aes sha256 sha512 sha3 neon                             | testNEON_LE |

NB: For the moment only Little Endian ARM is supported. 

Attempting to test a variant on a CPU without matching hardware features will cause a fault.

```

# Using testXXX will run the java tests but with the native componnents swapped in
# where appropriate. In this case we are skipping the java only tests, hence the '-x test'
# but that can be removed and it will test both java and native.

# Intel
./gradlew clean cleanNative withNative build testSSE -x test
./gradlew clean cleanNative withNative build testAVX -x test
./gradlew clean cleanNative withNative build testVAES -x test
./gradlew clean cleanNative withNative build testVAESF -x test

# Intel + java (assuming you have a CPU with the features)
./gradlew clean cleanNative withNative build testAVX testVAES testVAESF


# ARM
./gradlew clean cleanNative withNative build testNEON_LE -x test

# ARM with java
./gradlew clean cleanNative withNative build testNEON_LE
```

## Using the module

The module can be used like any other jar file, it will manage the installation of native libraries into
a temporary directory created in the same path as ```File.createTempFile()```.

### Selecting a specific variant

The library includes a probe library that examines CPU features and advises the module which
native variation is the appropriate version to load.

To override this use the ```-Dorg.bouncycastle.native.cpu_variant``` at the time of invocation or
this value can be set within the security policy as well.

For example, using -Dorg.bouncycastle.native.cpu_variant=avx:

```
# Intel

BouncyCastle APIs (LTS edition) v2.73.4-SNAPSHOT
Native Build Date: 2024-01-11T19:24:03
Native Status: READY
Native Variant: avx [<==== VARIANT!!!]
Native Features: AES/CBC AES/CBC-PC AES/CCM AES/CCM-PC AES/CFB AES/CFB-PC AES/CTR AES/CTR-PC AES/ECB AES/GCM AES/GCM-PC AES/GCM-SIV AES/GCMSIV-PC DRBG NRBG SHA2 SHA224 SHA256


CPU Features and Variant availability.
--------------------------------------------------------------------------------
Variant   CPU features + or -:                              Supported           
--------------------------------------------------------------------------------
VAESF     +vaes +avx512f +avx512bw +vpclmulqdq              Variant supported
VAES      +vaes                                             Variant supported
AVX       +avx                                              Variant supported

```

NB: Forcing a variant that is compiled to use instructions not supported by the host CPU will
case a fault and terminate the program. And you won't know until the JVM either errors or causes
a segfault.

### Properties

| Property                               | Values                      | Description                                                                  |
|----------------------------------------|-----------------------------|------------------------------------------------------------------------------|
| org.bouncycastle.native.cpu_variant    | avx, vaes, vaesf or neon-le | Specify a variant to use  see "Selecting a specific variant" for warnings.   |
| org.bouncycastle.packet_cipher_enabled | true or false               | False by default, enable or disable use of packet ciphers where appropriate. |


# Things to watch out for

## Notes on buffering

The native implementations may try to buffer anywhere from zero bytes to 16 blocks. The amount of buffering
or any buffering at all depends on the transformation and the underlying hardware. This is one difference between
the usual Java only api and the JNI based LTS - due to the use of underlying hardware the LTS api will behave more
like an HSM does. 

What this means is that users may need to pay more attention to the amount of data returned from a read call,
for example:

```
CipherInputStream cin = ... 
byte[] dataIExpect = new byte[...];
cin.read(dataIExpect);

```

May not return any data or may return the partial or different amount of data to what you might have observed 
previously using the java non-native implementation. This behavior is consistent with the specification for 
the read methods on InputStream, for example:

```
 public int read(byte[] b, int off, int len) throws IOException
 
 Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read
 as many as len bytes, but a smaller number may be read. The number of bytes actually read is returned 
 as an integer.
 
 https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read-byte:A-
 
 ```

It is important that users ensure they are not making assumptions concerning the amount of data returned
and check the number of bytes returned as they go, for example:

```
CipherInputStream cin = ... 
byte[] dataIExpect = new byte[...];
int l = cin.read(dataIExpect);
// Does `l` equal what I expect it to? Am I checking this?
```

Reading fully in java has always been a special case with DataInputStream for example supplying a special
```readFully(byte[] b)``` method to do it. The BC ```org.bouncycastle.util.io.Streams``` class
also has utility methods for reading fully to things like byte arrays and may be suitable in your use case.

Lightweight API users should also pay attention to the returned lengths as well, please note
the variable ```j``` in the following example:

```
  GCMModeCipher gcm = ...
  gcm.init( ... );
  
  byte[] out = new byte[gcm.getOutputSize(...)];
  int j = gcm.processBytes(message, 0, message.length, out, 0);
  gcm.doFinal(out, j);

```

## Optimisation status

HW accelerated features may vary between CPUs for example SHA3 support is available for ARM
but not Intel.

# Bouncy Castle

The Bouncy Castle Crypto package is a Java implementation of cryptographic algorithms, it was developed by the Legion of
the Bouncy Castle, a registered Australian Charity, with a little help! The Legion, and the latest goings-on with this
package, can be found at [https://www.bouncycastle.org](https://www.bouncycastle.org).

The Legion also gratefully acknowledges the contributions made to this package by others (
see [here](https://www.bouncycastle.org/contributors.html) for the current list). If you would like to contribute to our
efforts please feel free to get in touch with us or visit our [donations page](https://www.bouncycastle.org/donate),
sponsor some specific work, or purchase a support contract through [Crypto Workshop](https://www.cryptoworkshop.com).

The package is organised so that it contains a light-weight API suitable for use in any environment (including the newly
released J2ME) with the additional infrastructure to conform the algorithms to the JCE framework.

Except where otherwise stated, this software is distributed under a license based on the MIT X Consortium license. To
view the license, [see here](https://www.bouncycastle.org/licence.html). The OpenPGP library also includes a modified
BZIP2 library which is licensed under the [Apache Software License, Version 2.0](https://www.apache.org/licenses/).

**Note**: this source tree is not the FIPS version of the APIs - if you are interested in our FIPS version please
contact us directly at  [office@bouncycastle.org](mailto:office@bouncycastle.org).

## Code Organisation

The clean room JCE, for use with JDK 1.1 to JDK 1.3 is in the jce/src/main/java directory. From JDK 1.4 and later the
JCE ships with the JVM, the source for later JDKs follows the progress that was made in the later versions of the JCE.
If you are using a later version of the JDK which comes with a JCE install please **do not** include the jce directory
as a source file as it will clash with the JCE API installed with your JDK.

The **core** module provides all the functionality in the ligthweight APIs.

The **prov** module provides all the JCA/JCE provider functionality.

The **util** module is the home for code which is used by other modules that does not need to be in prov. At the moment
this is largely ASN.1 classes for the PKIX module.

The **pkix** module is the home for code for X.509 certificate generation and the APIs for standards that rely on ASN.1
such
as CMS, TSP, PKCS#12, OCSP, CRMF, and CMP.

The **mail** module provides an S/MIME API built on top of CMS.

The **pg** module is the home for code used to support OpenPGP.

The **tls** module is the home for code used to a general TLS API and JSSE Provider.

The build scripts that come with the full distribution allow creation of the different releases by using the different
source trees while excluding classes that are not appropriate and copying in the required compatibility classes from the
directories containing compatibility classes appropriate for the distribution.

If you want to try create a build for yourself, using your own environment, the best way to do it is to start with the
build for the distribution you are interested in, make sure that builds, and then modify your build scripts to do the
required exclusions and file copies for your setup, otherwise you are likely to get class not found exceptions. The
final caveat to this is that as the j2me distribution includes some compatibility classes starting in the java package,
you need to use an obfuscator to change the package names before attempting to import a midlet using the BC API.

## Examples and Tests

To view some examples, look at the test programs in the packages:

* **org.bouncycastle.crypto.test**

* **org.bouncycastle.jce.provider.test**

* **org.bouncycastle.cms.test**

* **org.bouncycastle.mail.smime.test**

* **org.bouncycastle.openpgp.test**

* **org.bouncycastle.tsp.test**

There are also some specific example programs for dealing with SMIME and OpenPGP. They can be found in:

* **org.bouncycastle.mail.smime.examples**

* **org.bouncycastle.openpgp.examples**

## Mailing Lists

For those who are interested, there are 2 mailing lists for participation in this project. To subscribe use the links
below and include the word subscribe in the message body. (To unsubscribe, replace **subscribe** with **unsubscribe** in
the message body)

* [announce-crypto-request@bouncycastle.org](mailto:announce-crypto-request@bouncycastle.org)  
  This mailing list is for new release announcements only, general subscribers cannot post to it.
* [dev-crypto-request@bouncycastle.org](mailto:dev-crypto-request@bouncycastle.org)  
  This mailing list is for discussion of development of the package. This includes bugs, comments, requests for
  enhancements, questions about use or operation.

**NOTE:** You need to be subscribed to send mail to the above mailing list.

## Feedback and Contributions

If you want to provide feedback directly to the members of **The Legion** then please
use [feedback-crypto@bouncycastle.org](mailto:feedback-crypto@bouncycastle.org), if you want to help this project
survive please consider [donating](https://www.bouncycastle.org/donate).

For bug reporting/requests you can report issues here on github, or via feedback-crypto if required. We will accept pull
requests based on this repository as well, but only on the basis that any code included may be distributed under
the [Bouncy Castle License](https://www.bouncycastle.org/licence.html).

## Finally

Enjoy!
