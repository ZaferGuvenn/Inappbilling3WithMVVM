package com.lafimsize.inappbilling.util

import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

class Security {

    private val KEY_FACTORY_ALGORITHM = "RSA"
    private val SIGNATURE_ALGORITHM = "SHA1withRSA"

    @Throws(IOException::class)
    fun verifyPurchase(
        base64PublicKey: String?, signedData: String,
        signature: String?
    ): Boolean {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)
            || TextUtils.isEmpty(signature)
        ) {
            //Purchase verification failed: missing data
            return false
        }
        val key = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)
    }

    @Throws(IOException::class)
    fun generatePublicKey(encodedPublicKey: String?): PublicKey {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            val msg = "Invalid key specification: $e"
            throw IOException(msg)
        }
    }

    fun verify(publicKey: PublicKey?, signedData: String, signature: String?): Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            //Base64 decoding failed
            return false
        }
        try {
            val signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            return signatureAlgorithm.verify(signatureBytes)
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            //Invalid key specification
        } catch (e: SignatureException) {
            //Signature exception
        }
        return false
    }
}