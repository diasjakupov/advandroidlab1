package com.example.advandroidlab1.data.sharing

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.advandroidlab1.R
import com.example.advandroidlab1.domain.SocialMediaImageSharing

private const val TAG = ""

class DefaultInstagramStoriesSharing(private val content: Context): SocialMediaImageSharing {
    override fun share(uri: Uri?) {
        if(uri == null){
            Toast.makeText(content, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            content.packageManager.getPackageInfo("com.instagram.android", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(content, "Instagram app is not installed", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(uri, "image/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra("source_application", content.packageName)
        }

        val resInfoList: List<ResolveInfo> = content.packageManager.queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            content.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            val chooserIntent =
                Intent.createChooser(intent, content.getString(R.string.share_to_ig_story));
            content.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
            Toast.makeText(content, "Failed to share to Instagram Story", Toast.LENGTH_SHORT).show()
        }
    }
}