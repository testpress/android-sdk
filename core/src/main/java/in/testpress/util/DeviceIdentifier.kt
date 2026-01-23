package `in`.testpress.util

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.util.UUID
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object DeviceIdentifier {
    private const val PREFS_FILE = "testpress_device_prefs"
    private const val KEY_DEVICE_UID = "device_uid"
    
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
            var uid = prefs.getString(KEY_DEVICE_UID, null)

            if (uid == null) {
                uid = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_UID, uid).apply()
            }

            deviceUid = uid
            return uid!!
        }
    }

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            createEncryptedPrefs(context, masterKey)
        } catch (e: Exception) {
            deleteSharedPreferences(context, PREFS_FILE)
            createEncryptedPrefs(context, masterKey)
        }
    }

    private fun createEncryptedPrefs(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
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
                val prefsFile = File(context.filesDir.parent + "/shared_prefs/" + name + ".xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
