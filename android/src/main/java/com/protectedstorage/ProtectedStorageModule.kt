package com.protectedstorage

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStoreException;
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.content.SharedPreferences
import javax.crypto.spec.IvParameterSpec;

class ProtectedStorageModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  fun generateKey(alias: String): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
      alias,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
      .setKeySize(256)
      .build()

    keyGenerator.init(keyGenParameterSpec)
    return keyGenerator.generateKey()
  }

  fun getKey(alias: String): SecretKey? {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
    return entry?.secretKey
  }

  fun encryptValue(alias: String, value: String): String {
    val secretKey = getKey(alias) ?: throw RuntimeException("No se encontró la clave con el alias: $alias")
    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
    val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    val encryptedValue = Base64.encodeToString(encrypted, Base64.DEFAULT)

    return "$iv:$encryptedValue"
  }

  fun saveEncryptedValue(context: Context, key: String, encryptedValue: String) {
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
      putString(key, encryptedValue)
      apply()
    }
  }

  fun getEncryptedValue(context: Context, key: String): String? {
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
  }

  fun removeEncryptedValue(context: Context, key: String): String? {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    keyStore.deleteEntry(key)
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove(key)
    editor.apply()
    return "OK"
  }

  fun removeStorageValue(context: Context, key: String): String? {
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove(key)
    editor.apply()
    return "OK"
  }

  fun decryptValue(alias: String, encryptedValue: String): String {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    val secretKeyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
    val secretKey = secretKeyEntry.secretKey

    val parts = encryptedValue.split(":")
    if (parts.size != 2) {
      throw IllegalArgumentException("El formato del valor cifrado no es válido")
    }

    val iv = Base64.decode(parts[0], Base64.DEFAULT)
    val encryptedData = Base64.decode(parts[1], Base64.DEFAULT)

    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

    val decryptedBytes = cipher.doFinal(encryptedData)
    return String(decryptedBytes, Charsets.UTF_8)
  }

  fun clearSharedPreferencesAndKS(context: Context) {
    try {
      val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
      val editor = sharedPreferences.edit()
      editor.clear()
      editor.apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun clearKeyStore() {
    try {
      val keyStore = KeyStore.getInstance("AndroidKeyStore")
      keyStore.load(null)
      val aliases = keyStore.aliases()
      while (aliases.hasMoreElements()) {
        val alias = aliases.nextElement()
        keyStore.deleteEntry(alias)
      }
    } catch (e: KeyStoreException) {
      e.printStackTrace() // Manejar la excepción relacionada con el Keystore
    } catch (e: Exception) {
      e.printStackTrace() // Capturar y manejar otras excepciones generales
    }
  }

  @ReactMethod
  fun setItem(name: String, value: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    generateKey(name)
    val encryptedToken = encryptValue(name, value)
    saveEncryptedValue(appContext, name, encryptedToken)
    promise.resolve("OK")
  }

  @ReactMethod
  fun getItem(name: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    val result = getEncryptedValue(appContext, name) ?: null
    if (result !== null) {
      val getEncryptedValue = decryptValue(name, result)
      promise.resolve(getEncryptedValue)
    } else {
      promise.resolve(result)
    }
  }

  @ReactMethod
  fun removeItem(name: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    val result = removeEncryptedValue(appContext, name) ?: null
    if (result !== null) {
      promise.resolve(result)
    } else {
      promise.resolve(null)
    }
  }

  @ReactMethod
  fun clearAll(promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    try {
      clearKeyStore()
      clearSharedPreferencesAndKS(appContext)
      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject("Error", "Error try to clear: ${e.message}")
    }
  }

  @ReactMethod
  fun storageSetItem(name: String, value: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    try {
      saveEncryptedValue(appContext, name, value)
      promise.resolve(name);
    } catch (e: Exception) {
      promise.reject("Error", "Error try to save item: ${e.message}")
    }
  }

  @ReactMethod
  fun storageGetItem(name: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    try {
      val result = getEncryptedValue(appContext, name) ?: null
      promise.resolve(result)
    } catch (e: Exception) {
      promise.reject("Error", "Error try to find item: ${e.message}")
    }
  }

  @ReactMethod
  fun storageRemoveItem(name: String, promise: Promise) {
    val appContext = reactApplicationContext.applicationContext
    try {
      removeStorageValue(appContext, name)
      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject("Error", "Error try to remove item: ${e.message}")
    }
  }

  companion object {
    const val NAME = "ProtectedStorage"
  }
}
