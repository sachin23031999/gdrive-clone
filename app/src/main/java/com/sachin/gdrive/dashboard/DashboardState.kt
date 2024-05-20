package com.sachin.gdrive.dashboard

import com.sachin.gdrive.model.DriveEntity

/**
 * Holds the state for dashboard UI.
 */
sealed class DashboardState {

    data object InitSuccess : DashboardState()

    data object InitFailed : DashboardState()

    data class FetchSuccess(val entities: List<DriveEntity>) : DashboardState()

    data class FetchFailed(val error: String) : DashboardState()
}