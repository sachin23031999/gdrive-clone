package com.sachin.gdrive.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sachin.gdrive.R
import com.sachin.gdrive.common.handleOnBackPressed
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.navigateTo
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.dashboard.DashboardActivity
import com.sachin.gdrive.databinding.FragmentSignInBinding
import org.koin.android.ext.android.inject

class SignInFragment : Fragment() {
    private val binding by lazy { FragmentSignInBinding.inflate(layoutInflater) }
    private val viewModel: SignInViewModel by inject()

    private val gsoContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
            viewModel.onSignIn(result.data)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPress()
        setupObservers()
        viewModel.init(requireContext())
        viewModel.checkLogin(requireContext())
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            logD { "current auth state: $state" }
            when(state) {
                is AuthState.SignInSuccess -> {
                    showToast("Login success!")
                    navigateTo(R.id.dashboardFragment)
                }
                is AuthState.AlreadyLoggedIn -> {
                    navigateTo(R.id.dashboardFragment)
                }
                is AuthState.SignInFailed -> {
                    showToast(state.error)
                }
                is AuthState.NotLoggedIn -> {
                    binding.btLogin.setOnClickListener {
                        viewModel.getSignInIntent()?.let { gsoContract.launch(it) }
                            ?: showToast("Please try again later")
                    }
                }
                else -> {}
            }

        }
    }

    private fun setupBackPress() {
        handleOnBackPressed {
            activity?.finishAffinity()
        }
    }
}