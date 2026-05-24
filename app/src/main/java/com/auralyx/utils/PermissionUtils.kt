package com.auralyx.utils
import android.Manifest; import android.content.Context; import android.content.pm.PackageManager; import android.os.Build
import androidx.core.content.ContextCompat
object PermissionUtils {
    fun getRequiredPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.POST_NOTIFICATIONS)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    fun hasStoragePermission(ctx:Context): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        else -> ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
