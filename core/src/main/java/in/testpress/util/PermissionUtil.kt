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
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

object PermissionUtil {

    enum class RequiredPermission(val permission: String, val description: String) {
        CAMERA(Manifest.permission.CAMERA, "Camera"),
        MICROPHONE(Manifest.permission.RECORD_AUDIO, "Microphone"),
        WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage"),
        READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, "Storage");

        companion object {
            fun getAllPermissions(): List<RequiredPermission> {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        listOf(CAMERA, MICROPHONE, READ_STORAGE)
                    }
                    else -> {
                        values().asList()
                    }
                }
            }
        }
    }

    fun askAllPermission(activity: Activity) {
        val requiredPermission =
            RequiredPermission.getAllPermissions().map { it.permission }.toTypedArray()
        ActivityCompat.requestPermissions(
            activity,
            requiredPermission,
            RequestCode.PERMISSION
        )
    }

    fun checkIsPermissionsGranted(
        activity: Activity,
        permissions: List<RequiredPermission>,
        action: () -> Unit
    ) {
        val requiredPermission = mutableListOf<String>()
        for (permission in permissions) {
            if (!isPermissionGranted(activity, permission.permission)) {
                requiredPermission.add(permission.description)
            }
        }
        if (requiredPermission.isEmpty()) {
            action.invoke()
        } else {
            requestPermissionsWithSnackbar(
                activity,
                getSnackBarMessage(requiredPermission)
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    true
                } else {
                    ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
            else -> {
                ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestPermissionsWithSnackbar(activity: Activity, message: String) {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setAction("Allow") {
            openAppSettingPage(activity)
        }.show()
    }

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

    private fun openAppSettingPage(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

}