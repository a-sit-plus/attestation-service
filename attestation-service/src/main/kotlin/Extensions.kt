package at.asitplus.attestation

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.JCEECPublicKey
import org.bouncycastle.util.encoders.Base64
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.ZoneId
import java.util.*


private val ecKeyFactory = KeyFactory.getInstance("EC")
private val rsaKeyFactory = KeyFactory.getInstance("RSA")

//copied from AppAttest Library
private val certificateFactory = CertificateFactory.getInstance("X.509")
fun ByteArray.parseToCertificate(): X509Certificate? = kotlin.runCatching {
    certificateFactory.generateCertificate(this.inputStream()) as X509Certificate
}.getOrNull()

data class AttestationObject(
    val fmt: String,
    val attStmt: AttestationStatement,
    val authData: ByteArray
) {
    data class AttestationStatement(
        val x5c: List<ByteArray>,
        val receipt: ByteArray
    )
}

internal fun String.decodeBase64ToArray() = Base64.decode(this)

internal fun ByteArray.encodeBase64() = Base64.toBase64String(this)

internal fun Clock.toJavaClock(): java.time.Clock =
    object : java.time.Clock() {
        override fun getZone(): ZoneId = systemDefaultZone().zone


        override fun withZone(zone: ZoneId?): java.time.Clock {
            TODO("Not yet implemented")
        }

        override fun instant(): Instant = now().toJavaInstant()

    }

internal fun kotlinx.datetime.Instant.toJavaDate() = Date.from(toJavaInstant())

fun ECPublicKey.toAnsi() = let {
    val xFromBc = it.w.affineX.toByteArray().ensureSize(32)
    val yFromBc = it.w.affineY.toByteArray().ensureSize(32)
    byteArrayOf(0x04) + xFromBc + yFromBc
}

fun ByteArray.parseToPublicKey(): PublicKey =
    try {
        (if (size < 1024) ecKeyFactory else rsaKeyFactory).generatePublic(X509EncodedKeySpec(this))
    } catch (e: Throwable) {
        if (first() != 0x04.toByte()) throw InvalidKeySpecException("Encoded public key does not start with 0x04")

        val parameterSpec = ECNamedCurveTable.getParameterSpec("P-256")
        val ecPoint = parameterSpec.curve.createPoint(
            BigInteger(1, sliceArray(1..<33)),
            BigInteger(1, takeLast(32).toByteArray())
        )
        val ecPublicKeySpec = org.bouncycastle.jce.spec.ECPublicKeySpec(ecPoint, parameterSpec)
        JCEECPublicKey("EC", ecPublicKeySpec)
    }

/**
 * Drops or adds zero bytes at the start until the [size] is reached
 */
private fun ByteArray.ensureSize(size: Int): ByteArray = when {
    this.size > size -> this.drop(1).toByteArray().ensureSize(size)
    this.size < size -> (byteArrayOf(0) + this).ensureSize(size)
    else -> this
}

// taken from https://github.com/Kotlin/kotlinx-datetime/pull/249/
fun java.time.Clock.toKotlinClock(): Clock = let {
    object : Clock {
        override fun now() = it.instant().toKotlinInstant()
    }
}