package com.example.repository

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.example.IslandDeviceAdminReceiver
import com.example.model.AppInfo
import com.example.data.AppDao
import com.example.data.AppConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class AppDetailsRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    private val packageManager: PackageManager = context.packageManager
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val adminComponent = IslandDeviceAdminReceiver.getComponentName(context)

    fun isProfileOwner(): Boolean {
        return dpm.isProfileOwnerApp(context.packageName)
    }

    fun isRunningInIsland(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            userManager.isManagedProfile
        } else {
            dpm.isProfileOwnerApp(context.packageName)
        }
    }

    fun isIslandProfileConfigured(): Boolean {
        val currentProfile = Process.myUserHandle()
        val profiles = userManager.userProfiles
        return profiles.any { it != currentProfile }
    }

    fun getIslandUserHandle(): UserHandle? {
        val currentProfile = Process.myUserHandle()
        return userManager.userProfiles.firstOrNull { it != currentProfile }
    }

    fun getAdminComponent() = adminComponent

    fun wipeIslandProfile(): Boolean {
        return try {
            if (isProfileOwner()) {
                dpm.wipeData(0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getMainlandApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val mainlandApps = mutableListOf<AppInfo>()
        
        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            if (pkg.packageName == context.packageName) continue
            
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg.packageName)
            val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || launchIntent != null
            if (!isUserApp) continue

            val appName = appInfo.loadLabel(packageManager).toString()
            val versionName = pkg.versionName ?: "1.0.0"
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            mainlandApps.add(
                AppInfo(
                    packageName = pkg.packageName,
                    appName = appName,
                    versionName = versionName,
                    isSystem = isSystem,
                    isFrozen = false,
                    isCloned = false
                )
            )
        }
        mainlandApps.sortedBy { it.appName.lowercase() }
    }

    suspend fun getIslandApps(islandUser: UserHandle): List<AppInfo> = withContext(Dispatchers.IO) {
        val islandApps = mutableListOf<AppInfo>()
        
        try {
            val activities = launcherApps.getActivityList(null, islandUser)
            val activePackages = activities.map { it.applicationInfo.packageName }.toSet()

            if (isProfileOwner()) {
                // If PO inside the managed profile, we can scan uninstalled and hidden packages
                val poPackages = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
                for (pkg in poPackages) {
                    if (pkg.packageName == context.packageName) continue
                    val info = pkg.applicationInfo ?: continue
                    val isHidden = dpm.isApplicationHidden(adminComponent, pkg.packageName)
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val appName = info.loadLabel(packageManager).toString()

                    islandApps.add(
                        AppInfo(
                            packageName = pkg.packageName,
                            appName = appName,
                            versionName = pkg.versionName ?: "1.0.0",
                            isSystem = isSystem,
                            isFrozen = isHidden,
                            isCloned = true
                        )
                    )
                }
            } else {
                // Running on Mainland, we list active packages in Managed Profile
                for (act in activities) {
                    if (act.applicationInfo.packageName == context.packageName) continue
                    val info = act.applicationInfo
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val appName = info.loadLabel(packageManager).toString()
                    
                    islandApps.add(
                        AppInfo(
                            packageName = info.packageName,
                            appName = appName,
                            versionName = "1.0.0",
                            isSystem = isSystem,
                            isFrozen = false,
                            isCloned = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        islandApps.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    suspend fun cloneAppToIsland(packageName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isProfileOwner()) {
                dpm.installExistingPackage(adminComponent, packageName)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun freezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isProfileOwner()) {
                dpm.setApplicationHidden(adminComponent, packageName, true)
                val existing = appDao.getConfigForPackage(packageName) ?: AppConfigEntity(packageName)
                appDao.insertOrUpdateConfig(existing.copy(lastActionTimestamp = System.currentTimeMillis(), category = "Frozen"))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unfreezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isProfileOwner()) {
                dpm.setApplicationHidden(adminComponent, packageName, false)
                val existing = appDao.getConfigForPackage(packageName) ?: AppConfigEntity(packageName)
                appDao.insertOrUpdateConfig(existing.copy(lastActionTimestamp = System.currentTimeMillis(), category = "Isolated"))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun observeAllConfigs(): Flow<List<AppConfigEntity>> {
        return appDao.getAllConfigs()
    }

    suspend fun saveAppConfig(packageName: String, label: String?, category: String, notes: String, isAutoFreeze: Boolean) {
        val existing = appDao.getConfigForPackage(packageName) ?: AppConfigEntity(packageName)
        appDao.insertOrUpdateConfig(
            existing.copy(
                customLabel = label,
                category = category,
                notes = notes,
                isAutoFreeze = isAutoFreeze
            )
        )
    }

    suspend fun getAppConfig(packageName: String): AppConfigEntity? {
        return appDao.getConfigForPackage(packageName)
    }
}
