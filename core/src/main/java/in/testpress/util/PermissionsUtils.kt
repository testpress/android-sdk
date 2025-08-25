package `in`.testpress.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import `in`.testpress.R
import `in`.testpress.core.TestpressSdk.PERMISSIONS_REQUEST_CODE

class PermissionsUtils {
    private val rootLayout: View
    private val activity: Activity
    private var fragment: Fragment? = null
    private var permissions: Array<String> = arrayOf()
    private var resultHandler: PermissionRequestResultHandler? = null
    private var checkPermission = false

    constructor(activity: Activity, rootLayout: View, permissions: Array<String>) {
        this.rootLayout = rootLayout
        this.permissions = permissions
        this.activity = activity
    }

    constructor(
        fragment: Fragment,
        rootLayout: View,
        permissions: Array<String>
    ) : this(fragment.requireActivity(), rootLayout, permissions) {
        this.fragment = fragment
    }

    constructor(activity: Activity, rootLayout: View) {
        this.activity = activity
        this.rootLayout = rootLayout
    }

    val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun requestStoragePermissionWithSnackbar() {
        Snackbar.make(
            rootLayout,
            "Required Storage Permission",
            Snackbar.LENGTH_LONG
        ).setAction("Allow") {
            val intent = Intent()
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.setData(uri)
            activity.startActivity(intent)
        }.show()
    }

    private fun checkPermissionRequired(): Boolean {
        return checkPermissionRequired(activity, permissions)
    }

    private fun checkPermissionRequired(uri: Uri): Boolean {
        return checkPermissionRequired() && isUriRequiresPermissions(uri)
    }

    fun checkPermissionRequired(uri: Uri, resultHandler: PermissionRequestResultHandler) {
        if (checkPermissionRequired(uri)) {
            requestPermissions(resultHandler)
        } else {
            resultHandler.onPermissionGranted()
        }
    }

    private fun requestPermissions(resultHandler: PermissionRequestResultHandler?) {
        this.resultHandler = resultHandler
        if (fragment != null) {
            fragment?.requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                resultHandler?.onPermissionGranted()
            } else {
                val show =
                    if (permissions.isNotEmpty()) ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permissions[0]
                    ) else false

                if (!show) {
                    val builder =
                        AlertDialog.Builder(activity, R.style.TestpressAppCompatAlertDialogStyle)

                    builder.setTitle(R.string.testpress_permission_denied)
                    builder.setMessage(R.string.testpress_permission_denied_message)
                    builder.setPositiveButton(R.string.testpress_go_to_settings) { _, _ ->
                        checkPermission = true
                        val intent = Intent()
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.setData(uri)
                        activity.startActivity(intent)
                    }
                    builder.setNegativeButton(R.string.testpress_deny, null)
                    builder.show()
                } else {
                    Snackbar.make(
                        rootLayout, R.string.testpress_action_cant_done_without_permission,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun onResume() {
        if (checkPermission) {
            checkPermission = false
            requestPermissions(resultHandler)
        }
    }

    fun setPermissions(permissions: Array<String>) {
        this.permissions = permissions
    }

    private fun isUriRequiresPermissions(uri: Uri): Boolean {
        return try {
            activity.contentResolver.openInputStream(uri)?.use {}
            false
        } catch (e: Exception) {
            true
        }
    }

    interface PermissionRequestResultHandler {
        fun onPermissionGranted()
    }

    companion object {
        @JvmOverloads
        fun checkPermissionRequired(
            context: Context,
            permissions: Array<String> = arrayOf()
        ): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions.any {
                context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
        }
    }
}