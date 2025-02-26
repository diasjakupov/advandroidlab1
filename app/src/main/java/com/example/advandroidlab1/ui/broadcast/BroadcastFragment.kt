package com.example.advandroidlab1.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.advandroidlab1.data.music.MusicService
import com.example.advandroidlab1.databinding.FragmentBroadcastBinding
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding

class BroadcastFragment : BaseFragment<FragmentBroadcastBinding, BroadcastViewModel>() {

    override val binding by viewBinding(FragmentBroadcastBinding::inflate)
    override val viewModel: BroadcastViewModel by viewModels<BroadcastViewModel>()

    private var broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED }?.let {
                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                binding.airplaneModeSwitch.isChecked = isAirplaneModeOn
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        ContextCompat.registerReceiver(
            requireContext(),
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

}