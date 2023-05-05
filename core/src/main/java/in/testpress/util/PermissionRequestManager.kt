package `in`.testpress.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

class PermissionRequestManager(val activity: Activity, callback: () -> Unit) {

    init {
        if (isPermissionGranted()) {
            callback.invoke()
        } else {
            if (isUserSelectDontAskAgain()){
                requestStoragePermissionWithSnackbar()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isUserSelectDontAskAgain(): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun requestStoragePermissionWithSnackbar() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "Required Storage Permission",
            Snackbar.LENGTH_LONG
        )
            .setAction("Allow") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }.show()
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1500
        )
    }

}