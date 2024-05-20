package com.sachin.gdrive.di

import com.sachin.gdrive.MainViewModel
import com.sachin.gdrive.auth.SignInViewModel
import com.sachin.gdrive.dashboard.DashboardViewModel
import com.sachin.gdrive.notification.NotificationManager
import com.sachin.gdrive.repository.AuthRepository
import com.sachin.gdrive.repository.DriveRepository
import com.sachin.gdrive.provider.DriveServiceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { NotificationManager(get()) }
    single { AuthRepository() }
    factory { DriveRepository(get()) }
    factory { DriveServiceProvider(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { DashboardViewModel(get(), get()) }
}