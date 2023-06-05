package `in`.testpress.util.extension

import `in`.testpress.util.Permission
import `in`.testpress.util.PermissionHandler
import androidx.fragment.app.Fragment

fun Fragment.checkPermissionsGranted(
    requiredPermissions: List<Permission>,
    action: () -> Unit
) {
    PermissionHandler().checkPermissionsGranted(this.requireActivity(),requiredPermissions,action)
}