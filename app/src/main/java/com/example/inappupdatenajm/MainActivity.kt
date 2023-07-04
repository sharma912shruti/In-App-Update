package com.example.inappupdatenajm

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity() {

    private val MY_REQUEST_CODE = 1

    private lateinit var appUpdateManager: AppUpdateManager

    private lateinit var textView : TextView
    var update :Int? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForUpdates(true)
        registerInstallStateListener()
        textView = findViewById<TextView>(R.id.hello_button)
        textView.setOnClickListener {
//            showInstallPrompt()
        }

        findViewById<Button>(R.id.flexible_btn).setOnClickListener {
            checkForUpdates(true)
        }
        findViewById<Button>(R.id.immidiate_btn).setOnClickListener {
            checkForUpdates(false)
        }
        findViewById<TextView>(R.id.version_detail).text = BuildConfig.VERSION_NAME +", "+ BuildConfig.VERSION_CODE
    }

    private fun checkForUpdates(shouldFlexible:Boolean) {
        appUpdateManager = AppUpdateManagerFactory.create(this)


        update = if(shouldFlexible){
            AppUpdateType.FLEXIBLE
        }else{
            AppUpdateType.IMMEDIATE
        }


        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(update!!)
            ) {
                // Request the update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    update!!,
                    this,
                    MY_REQUEST_CODE
                )
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,"UpdateFailure"+ resultCode.toString() ,Toast.LENGTH_LONG).show()
                textView.text = "Please try again later"
                Toast.makeText(this,textView.text.toString(),Toast.LENGTH_LONG).show()
                // If the update flow fails or is canceled, you can decide how to proceed
                // For example, you can show a message to the user or retry the update later
            }
        }
    }

    private val installStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
               Toast.makeText(this,"UpdateSuccess"+state.installStatus().toString(),Toast.LENGTH_LONG).show()
                // The update has been downloaded and is ready to be installed
                // You can prompt the user to install the update
                showInstallPrompt()
            }else{
               Toast.makeText(this,"UpdateSuccess"+ state.installStatus().toString(),Toast.LENGTH_LONG).show()
            }
        }

    // Call this function to register the install state listener
    private fun registerInstallStateListener() {
        Toast.makeText(this,"UpdateListener Registered",Toast.LENGTH_LONG).show()
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    // Call this function to unregister the install state listener
    private fun unregisterInstallStateListener() {
       Toast.makeText(this,"UpdateListener UnRegistered",Toast.LENGTH_LONG).show()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterInstallStateListener()
    }

    private fun showInstallPrompt() {
        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogBuilder.setIcon(R.drawable.ic_dialog_icon)
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            if(update == AppUpdateType.FLEXIBLE) {
                appUpdateManager.completeUpdate()
            }
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            // Handle negative button click here
        }
        findViewById<TextView>(R.id.version_detail).text = BuildConfig.VERSION_NAME +", "+ BuildConfig.VERSION_CODE
        textView.text = "Your application is updated, Enjoy new version of application"
        alertDialogBuilder.setMessage("Your application is updated, Enjoy new version of application ")
        alertDialogBuilder.setCancelable(true)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

//        val customView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_view, null)
//        alertDialogBuilder.setView(customView)


    }

}