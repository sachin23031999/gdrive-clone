package com.sachin.gdrive.common

import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sachin.gdrive.common.log.logE

/**
 * This function add callback to Android Native Back Button.
 */
fun Fragment.handleOnBackPressed(callBack: () -> Unit): OnBackPressedCallback {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callBack()
        }
    }
    activity?.onBackPressedDispatcher?.addCallback(
        viewLifecycleOwner,
        onBackPressedCallback
    ) ?: logE { "Cannot add callBack for onBackPressed" }

    return onBackPressedCallback
}

/**
 * Fragment show toast.
 */
fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

/**
 * Context show toast.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Context get activity.
 */
fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

/**
 * Notification manager tied to context.
 */
val Context.notificationManager: NotificationManager
    get() = getSystemService(NotificationManager::class.java)

/**
 * Navigate to extension for fragment.
 */
fun Fragment.navigateTo(fragmentId: Int) {
    findNavController().navigate(fragmentId)
}

/**
 * Show snack bar at bottom.
 */
fun Fragment.showSnackBar(message: String, duration: Int) {
    Snackbar.make(requireView(), message, duration).show()
}

