package `in`.testpress.util.extension

import android.Manifest
import `in`.testpress.RequestCode
import `in`.testpress.util.Permission
import `in`.testpress.util.PermissionHandler
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

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

fun Activity.isStoragePermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.askStoragePermission(message: String) {
    if (isStoragePermissionGranted()) return
    if (shouldShowRequestPermissionRationale()) {
        requestStoragePermission()
    } else {
        Snackbar.make(
            this.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction("Allow") {
                goToSettings()
            }.show()
    }
}

fun Activity.requestStoragePermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), RequestCode.PERMISSION
        )
    }
}

private fun Activity.shouldShowRequestPermissionRationale(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        false
    }
}

private fun Activity.goToSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$packageName")
    ).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also {
        startActivity(it)
    }
}