package com.example

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast

class IslandDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Island Admin: Active", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Island Admin: Inactive", Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = getComponentName(context)
        
        // Mark profile as active and set app name as the official work profile manager
        dpm.setProfileEnabled(adminComponent)
        dpm.setProfileName(adminComponent, "Island Workspace")
        
        Toast.makeText(context, "Island Workspace initialized successfully", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, IslandDeviceAdminReceiver::class.java)
        }
    }
}
