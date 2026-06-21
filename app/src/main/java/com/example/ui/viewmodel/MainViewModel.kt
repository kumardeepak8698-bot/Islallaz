package com.example.ui.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IslandDeviceAdminReceiver
import com.example.data.AppConfigEntity
import com.example.data.AppDatabase
import com.example.model.AppInfo
import com.example.repository.AppDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val appDao = db.appDao()
    private val repository = AppDetailsRepository(application, appDao)

    private val _isRunningInIsland = MutableStateFlow(false)
    val isRunningInIsland: StateFlow<Boolean> = _isRunningInIsland.asStateFlow()

    private val _isIslandConfigured = MutableStateFlow(false)
    val isIslandConfigured: StateFlow<Boolean> = _isIslandConfigured.asStateFlow()

    private val _isProfileOwner = MutableStateFlow(false)
    val isProfileOwner: StateFlow<Boolean> = _isProfileOwner.asStateFlow()

    private val _mainlandApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val mainlandApps: StateFlow<List<AppInfo>> = _mainlandApps.asStateFlow()

    private val _islandApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val islandApps: StateFlow<List<AppInfo>> = _islandApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _appConfigs = MutableStateFlow<Map<String, AppConfigEntity>>(emptyMap())
    val appConfigs: StateFlow<Map<String, AppConfigEntity>> = _appConfigs.asStateFlow()

    init {
        checkStatus()
        observeConfigs()
        refreshApps()
    }

    fun checkStatus() {
        _isRunningInIsland.value = repository.isRunningInIsland()
        _isIslandConfigured.value = repository.isIslandProfileConfigured()
        _isProfileOwner.value = repository.isProfileOwner()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun observeConfigs() {
        viewModelScope.launch {
            repository.observeAllConfigs().collect { configs ->
                _appConfigs.value = configs.associateBy { it.packageName }
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get mainland apps
                val mainland = repository.getMainlandApps()
                _mainlandApps.value = mainland

                // Get island apps if configured
                val isConfigured = repository.isIslandProfileConfigured()
                _isIslandConfigured.value = isConfigured
                val islandUser = repository.getIslandUserHandle()
                if (islandUser != null) {
                    val island = repository.getIslandApps(islandUser)
                    _islandApps.value = island
                } else {
                    _islandApps.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load apps: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProvisioningIntent(): Intent {
        val adminComponent = repository.getAdminComponent()
        return Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent)
            } else {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, adminComponent.packageName)
            }
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true)
        }
    }

    fun cloneApp(packageName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.cloneAppToIsland(packageName)
            if (success) {
                refreshApps()
                onSuccess()
            } else {
                if (!repository.isProfileOwner()) {
                    onError("Cloning can only be initiated inside the Work Profile or through Google Play. Please switch to the Island environment.")
                } else {
                    onError("Failed to clone package '$packageName'. Make sure the app exists in the mainland package system.")
                }
            }
            _isLoading.value = false
        }
    }

    fun freezeApp(packageName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!repository.isProfileOwner()) {
                onError("Freezing apps is a restricted capability. Please open Island from inside the Work Profile to perform this action.")
                _isLoading.value = false
                return@launch
            }
            val success = repository.freezeApp(packageName)
            if (success) {
                refreshApps()
                onSuccess()
            } else {
                onError("Failed to freeze package '$packageName'. Check profile admin permissions.")
            }
            _isLoading.value = false
        }
    }

    fun unfreezeApp(packageName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!repository.isProfileOwner()) {
                onError("Unfreezing apps is a restricted capability. Please open Island from inside the Work Profile to perform this action.")
                _isLoading.value = false
                return@launch
            }
            val success = repository.unfreezeApp(packageName)
            if (success) {
                refreshApps()
                onSuccess()
            } else {
                onError("Failed to unfreeze package '$packageName'.")
            }
            _isLoading.value = false
        }
    }

    fun saveAppConfig(packageName: String, label: String?, category: String, notes: String, isAutoFreeze: Boolean) {
        viewModelScope.launch {
            repository.saveAppConfig(packageName, label, category, notes, isAutoFreeze)
            refreshApps()
        }
    }

    fun wipeProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.wipeIslandProfile()
            if (success) {
                refreshApps()
                checkStatus()
                onSuccess()
            } else {
                onError("Could not remove profile. Removing can only be performed by the active Profile Owner app inside the Work Profile.")
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
