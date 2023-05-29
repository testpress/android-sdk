package `in`.testpress.util

import `in`.testpress.RequestCode
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// Enum class representing required permissions with their descriptions
enum class RequiredPermission(val permission: String, val description: String) {
    CAMERA(Manifest.permission.CAMERA, "Camera"),
    MICROPHONE(Manifest.permission.RECORD_AUDIO, "Microphone"),
    WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage"),
    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, "Storage");

    companion object {
        // Function to get all permissions based on the device's SDK version
        fun getAllPermissions(): List<RequiredPermission> =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    Log.d("TAG", "getAllPermissions: ${Build.VERSION.SDK_INT}")
                    listOf(CAMERA, MICROPHONE, READ_STORAGE)
                }
                else -> {
                    Log.d("TAG", "getAllPermissions: ${Build.VERSION.SDK_INT}")
                    values().asList()
                }
            }
    }
}

// Utility class for handling permissions
class PermissionUtil {

    // Check if required permissions are granted, and take action accordingly
    fun checkPermissionsGranted(
        activity: Activity,
        requiredPermissions: List<RequiredPermission>,
        action: () -> Unit
    ) {
        val permissions = mutableListOf<String>()
        for (requiredPermission in requiredPermissions) {
            if (!isPermissionGranted(activity, requiredPermission.permission)) {
                permissions.add(requiredPermission.description)
            }
        }
        if (permissions.isEmpty()) {
            action.invoke()
        } else {
            requestPermissionsWithSnackbar(
                activity,
                getSnackBarMessage(permissions)
            )
        }
    }

    // Check if a specific permission is granted
    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> isWriteStoragePermissionGranted(context)
            else -> ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Check if WRITE_EXTERNAL_STORAGE permission is granted
    private fun isWriteStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Show a Snackbar with a message requesting permissions
    private fun requestPermissionsWithSnackbar(activity: Activity, message: String) {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setAction("Allow") {
            openAppSettingsPage(activity)
        }.show()
    }

    // Generate the Snackbar message based on the required permissions
    private fun getSnackBarMessage(permissions: MutableList<String>): String {
        val distinctPermissions = permissions.distinct()
        return when {
            distinctPermissions.isEmpty() -> "Permission required to access this functionality"
            distinctPermissions.size == 1 -> "${distinctPermissions[0]} permission required to access this functionality"
            else -> {
                val joinedPermissions = distinctPermissions.dropLast(1).joinToString(", ")
                val lastPermission = distinctPermissions.last()
                "$joinedPermissions, and $lastPermission permissions required to access this functionality"
            }
        }
    }

    // Open the app's settings page
    private fun openAppSettingsPage(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

}

// Extension function for Activity to request all permissions
fun Activity.mayBeAskAllPermissions() {
    val requiredPermission =
        RequiredPermission.getAllPermissions().map { it.permission }.toTypedArray()
    ActivityCompat.requestPermissions(this, requiredPermission, RequestCode.PERMISSION)
}

// Extension function for Activity to check if required permissions are granted
fun Activity.checkPermissionsGranted(
    requiredPermissions: List<RequiredPermission>,
    action: () -> Unit
) {
    PermissionUtil().checkPermissionsGranted(this,requiredPermissions,action)
}

// Extension function for Fragment to check if required permissions are granted
fun Fragment.checkPermissionsGranted(
    requiredPermissions: List<RequiredPermission>,
    action: () -> Unit
) {
    PermissionUtil().checkPermissionsGranted(this.requireActivity(),requiredPermissions,action)
}