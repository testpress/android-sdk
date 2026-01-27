package `in`.testpress.util

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.util.UUID
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object DeviceIdentifier {
    private const val PREFS_FILE = "testpress_device_prefs"
    private const val KEY_DEVICE_UID = "device_uid"
    const val HEADER_DEVICE_UID = "X-Device-UID"
    const val HEADER_DEVICE_TYPE = "X-Device-Type"
    const val DEVICE_TYPE_MOBILE = "mobile_app"
    
    @Volatile
    private var deviceUid: String? = null

    fun get(context: Context): String {
        val existingUid = deviceUid
        if (existingUid != null) {
            return existingUid
        }

        synchronized(this) {
            val existingUidDoubleCheck = deviceUid
            if (existingUidDoubleCheck != null) {
                return existingUidDoubleCheck
            }

            val prefs = getEncryptedPrefs(context)
            val uid = prefs.getString(KEY_DEVICE_UID, null) ?: UUID.randomUUID().toString().also { newUid ->
                prefs.edit().putString(KEY_DEVICE_UID, newUid).apply()
            }
            deviceUid = uid
            return uid
        }
    }

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return try {
            createEncryptedPrefs(context, masterKeyAlias)
        } catch (e: java.security.GeneralSecurityException) {
            deleteSharedPreferences(context, PREFS_FILE)
            createEncryptedPrefs(context, masterKeyAlias)
        } catch (e: java.io.IOException) {
            deleteSharedPreferences(context, PREFS_FILE)
            createEncryptedPrefs(context, masterKeyAlias)
        }
    }

    private fun createEncryptedPrefs(context: Context, masterKeyAlias: String): SharedPreferences {
        return EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteSharedPreferences(context: Context, name: String) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.deleteSharedPreferences(name)
            } else {
                context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
                val prefsFile = File(File(context.applicationInfo.dataDir, "shared_prefs"), "$name.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DeviceIdentifier", "Failed to delete shared preferences", e)
        }
    }
}
