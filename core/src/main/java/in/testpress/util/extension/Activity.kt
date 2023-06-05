package `in`.testpress.util.extension

import `in`.testpress.RequestCode
import `in`.testpress.util.Permission
import `in`.testpress.util.PermissionHandler
import android.app.Activity
import androidx.core.app.ActivityCompat

// Extension function for Activity to request all permissions
fun Activity.askAllPermissions() {
    val requiredPermission =
        Permission.getAllPermissions().map { it.permission }.toTypedArray()
    ActivityCompat.requestPermissions(this, requiredPermission, RequestCode.PERMISSION)
}

fun Activity.checkPermissionsGranted(
    requiredPermissions: List<Permission>,
    action: () -> Unit
) {
    PermissionHandler().checkPermissionsGranted(this,requiredPermissions,action)
}