package com.example.advandroidlab1.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.advandroidlab1.databinding.FragmentBroadcastBinding
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding

class BroadcastFragment : BaseFragment<FragmentBroadcastBinding, BroadcastViewModel>() {

    override val binding by viewBinding(FragmentBroadcastBinding::inflate)
    override val viewModel: BroadcastViewModel by viewModels()

    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                val isOn = intent.getBooleanExtra("state", false)
                binding.switchAirplane.isChecked = isOn
                binding.tvAirplaneStatus.text = if (isOn) {
                    "Airplane Mode is ON"
                } else {
                    "Airplane Mode is OFF"
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        ContextCompat.registerReceiver(requireContext(), airplaneModeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(airplaneModeReceiver)
    }
}
