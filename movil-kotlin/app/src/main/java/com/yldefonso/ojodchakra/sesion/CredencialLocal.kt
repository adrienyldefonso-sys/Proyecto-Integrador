package com.yldefonso.ojodchakra.sesion

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Permite entrar sin internet. Solo se guarda la sal y el hash de la contraseña, nunca la contraseña.
 * PBKDF2 con SHA-256; en Android 7.0 y 7.1 (API 24-25) se usa SHA-1 porque SHA-256 no siempre existe.
 */
data class CredencialLocal(val algoritmo: String, val sal: String, val hash: String) {

    fun coincide(contrasena: String): Boolean {
        return try {
            val calculado = derivar(contrasena, Base64.decode(sal, Base64.NO_WRAP), algoritmo)
            calculado.contentEquals(Base64.decode(hash, Base64.NO_WRAP))
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val SHA256 = "PBKDF2WithHmacSHA256"
        private const val SHA1 = "PBKDF2WithHmacSHA1"
        private const val ITERACIONES = 120_000
        private const val BITS = 256
        private val aleatorio = SecureRandom()

        fun crear(contrasena: String): CredencialLocal {
            val sal = ByteArray(16).also { aleatorio.nextBytes(it) }
            val algoritmo = algoritmoDisponible()
            val hash = derivar(contrasena, sal, algoritmo)
            return CredencialLocal(
                algoritmo = algoritmo,
                sal = Base64.encodeToString(sal, Base64.NO_WRAP),
                hash = Base64.encodeToString(hash, Base64.NO_WRAP)
            )
        }

        private fun algoritmoDisponible(): String = try {
            SecretKeyFactory.getInstance(SHA256)
            SHA256
        } catch (e: Exception) {
            SHA1
        }

        private fun derivar(contrasena: String, sal: ByteArray, algoritmo: String): ByteArray {
            val especificacion = PBEKeySpec(contrasena.toCharArray(), sal, ITERACIONES, BITS)
            try {
                return SecretKeyFactory.getInstance(algoritmo).generateSecret(especificacion).encoded
            } finally {
                especificacion.clearPassword()
            }
        }
    }
}
