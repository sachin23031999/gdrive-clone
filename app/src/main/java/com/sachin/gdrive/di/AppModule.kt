package com.sachin.gdrive.di

import com.sachin.gdrive.MainViewModel
import com.sachin.gdrive.auth.SignInViewModel
import com.sachin.gdrive.repository.AuthRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { AuthRepository() }
    viewModel { MainViewModel(get()) }
    viewModel { SignInViewModel(get()) }
}