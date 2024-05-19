package com.sachin.gdrive.dashboard

import com.sachin.gdrive.model.DriveEntity

sealed class DashboardState {

    data object InitSuccess : DashboardState()

    data object InitFailed : DashboardState()

    data class FetchInProgress(val progress: Int): DashboardState()

    data class FetchSuccess(val entities: List<DriveEntity>) : DashboardState()
}