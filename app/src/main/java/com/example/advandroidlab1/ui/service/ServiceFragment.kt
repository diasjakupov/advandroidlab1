package com.example.advandroidlab1.ui.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.advandroidlab1.data.music.MusicService
import com.example.advandroidlab1.databinding.FragmentServiceBinding
import com.example.advandroidlab1.domain.AudioFile
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServiceFragment : BaseFragment<FragmentServiceBinding, ServiceViewModel>() {


    override val binding by viewBinding(FragmentServiceBinding::inflate)
    override val viewModel: ServiceViewModel by viewModels<ServiceViewModel>()

    private val songUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { it.action == MusicService.ACTION_UPDATE_SONG }?.let {
                val songTitle = it.getStringExtra(MusicService.EXTRA_CURRENT_SONG)
                binding.tvCurrentSong.text = "Now Playing: ${songTitle ?: "Unknown"}"
            }
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted) viewModel.loadFiles()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        permissionRequest.launch(Manifest.permission.READ_MEDIA_AUDIO)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filter = IntentFilter(MusicService.ACTION_UPDATE_SONG)
        ContextCompat.registerReceiver(
            requireContext(),
            songUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        binding.btnStart.setOnClickListener {
            viewModel.audioFiles.value.let { files ->
                if (files.isNotEmpty()) {
                    val playlist = ArrayList<AudioFile>(files)
                    val intent = Intent(requireContext(), MusicService::class.java).apply {
                        action = MusicService.ACTION_PLAY
                        putParcelableArrayListExtra(MusicService.EXTRA_PLAYLIST, playlist)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requireContext().startForegroundService(intent)
                    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requireContext().startForegroundService(intent)
                    }
                    else {
                        requireContext().startService(intent)
                    }
                }
            }
        }

        binding.btnPause.setOnClickListener {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_PAUSE
            }
            requireContext().startService(intent)
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_STOP
            }
            requireContext().startService(intent)
        }
    }



}