package org.mjdev.desktop.data

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import org.mjdev.desktop.extensions.JSonExt.toJson
import org.mjdev.desktop.interfaces.IApp


@Suppress("unused")
class App(
    private val context: Context?,
    private val info: LauncherActivityInfo,
    override val name: String = info.label.toString(),
    override val fullAppName: String = info.label.toString(),
    override val comment: String = info.applicationInfo.packageName,
    override val cmd: String = info.applicationInfo.packageName, // todo intent
    override val fullTextString: String = info.toJson(),
    override val categories: List<Category> = listOf(guessCategory(context, info))
) : IApp {
    override var isStarting: Boolean = false

    override var isRunning: Boolean = false

    override suspend fun start() {
        context?.startActivity(Intent(
            Intent.ACTION_MAIN
        ).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            setComponent(info.componentName)
        })
    }

    override fun toString(): String = name

    private fun PackageInfo.isSystemPackage(): Boolean {
        return ((applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    companion object {
        @SuppressLint("NewApi")
        private fun guessCategory(
            context: Context?,
            info: LauncherActivityInfo
        ): Category {
            return try {
                ApplicationInfo.getCategoryTitle(
                    context,
                    info.applicationInfo.category
                )?.let { Category(it.toString()) } ?: Category.Empty
            } catch (e: Throwable) {
                Category.Empty
            }
        }
    }
}
