package org.gotson.komga.infrastructure.web

import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.pathString
import kotlin.io.path.toPath

import java.lang.Exception
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT

fun URL.toFilePath(): String =
  this.toURI().toPath().pathString

fun filePathToUrl(filePath: String): URL =
  Paths.get(filePath).toUri().toURL()

fun ResponseEntity.BodyBuilder.setCachePrivate() =
  this.cacheControl(cachePrivate)

val cachePrivate = CacheControl
  .maxAge(0, TimeUnit.SECONDS)
  .noTransform()
  .cachePrivate()
  .mustRevalidate()

fun getMediaTypeOrDefault(mediaTypeString: String?): MediaType {
  mediaTypeString?.let {
    try {
      return MediaType.parseMediaType(mediaTypeString)
    } catch (_: Exception) {
    }
  }
  return MediaType.APPLICATION_OCTET_STREAM
}

fun getPublicKey(keystr: String): RSAPublicKey? {
  val pubKey: PublicKey
  try {
      val keystrs = keystr.replace("-----BEGIN PUBLIC KEY-----", "").replace("\n", "").replace("-----END PUBLIC KEY-----","").replace(" ","")
      val pubkeyBytes = Base64.getDecoder().decode(keystrs)
      val keySpec = X509EncodedKeySpec(pubkeyBytes)
      val keyFactory = KeyFactory.getInstance("RSA")
      pubKey = keyFactory.generatePublic(keySpec)
      return pubKey as RSAPublicKey
  } catch (e: Exception) {
      return null
  }
}
fun getPrivateKey(keystr:String) : RSAPrivateKey? {
  try {
      val keystrs = keystr.replace("-----BEGIN PRIVATE KEY-----", "").replace("\n", "").replace("-----END PRIVATE KEY-----","").replace(" ","")
      val  privateBytes = Base64.getDecoder().decode(keystrs)
      val keySpec = PKCS8EncodedKeySpec(privateBytes)
      val keyFactory = KeyFactory.getInstance("RSA");
      val prvKey = keyFactory.generatePrivate(keySpec);
      return prvKey as RSAPrivateKey
  } catch (e:Exception) {
      return null
  }
}
fun getPSPDFKitJwt(bookId:String,pubKey:String,privKey:String) : String{
  val token=JWT.create().withHeader(mapOf<String,Any>("alg" to "RS256","typ" to "JWT")) // header
  .withClaim("admin", true) // payload
  .withClaim("document_id",bookId)
  .withClaim("name", "houchen")
  .withClaim("permissions", "all")
  .withClaim("sub", "1234567890")
  .withClaim("iat", 1658781737)
  .withClaim("exp", 1790317736)
  .sign(Algorithm.RSA256(getPublicKey(pubKey),getPrivateKey(privKey)))
  return token
}
