package `in`.testpress.util.extension

import `in`.testpress.RequestCode
import `in`.testpress.util.Permission
import `in`.testpress.util.PermissionHandler
import android.app.Activity
import android.os.Message
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat

fun Activity.askAllPermissions() {
    val requiredPermission =
        Permission.getAllPermissions().map { it.permission }.toTypedArray()
    ActivityCompat.requestPermissions(this, requiredPermission, RequestCode.PERMISSION)
}

fun Activity.performActionIfPermissionsGranted(
    requiredPermissions: List<Permission>,
    action: () -> Unit
) {
    PermissionHandler().performActionIfPermissionsGranted(this,requiredPermissions,action)
}

fun Activity.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}