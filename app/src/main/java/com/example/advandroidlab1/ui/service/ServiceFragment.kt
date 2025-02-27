package com.example.advandroidlab1.ui.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ServiceFragment : BaseFragment<FragmentServiceBinding, ServiceViewModel>() {

    override val binding by viewBinding(FragmentServiceBinding::inflate)
    override val viewModel: ServiceViewModel by viewModels<ServiceViewModel>()

    private val songUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicService.ACTION_UPDATE_SONG) {
                val songTitle = intent.getStringExtra(MusicService.EXTRA_CURRENT_SONG)
                requireActivity().runOnUiThread {
                    binding.tvCurrentSong.text = "Now Playing: ${songTitle ?: "Unknown"}"
                }
            } else if (intent?.action == MusicService.ACTION_NO_SONG) {
                requireActivity().runOnUiThread {
                    binding.tvCurrentSong.text = "Nothing is playing"
                }
            }
        }
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadFiles()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkAndRequestNotificationPermission()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Audio permission is required to play music",
                Toast.LENGTH_LONG
            ).show()
            binding.tvCurrentSong.text = "Permission required to access music files"
            updateButtonStates(false)
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                requireContext(),
                "Notification permission is needed for the music player to work properly",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestAudioPermission()
        observeAudioFiles()
        setupButtons()
    }

    private fun observeAudioFiles() = lifecycleScope.launch {
        viewModel.audioFiles.filterNotNull().collect { files ->
            updateButtonStates(files.isNotEmpty())
            if (files.isEmpty()) {
                binding.tvCurrentSong.text = "No music files found on device"
            }
        }
    }

    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            val files = viewModel.audioFiles.value
            if (files.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No music files available", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val playlist = ArrayList<AudioFile>(files)
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_PLAY
                putParcelableArrayListExtra(MusicService.EXTRA_PLAYLIST, playlist)
            }

            startMusicService(intent)
        }

        binding.btnPause.setOnClickListener {
            requireContext().startService(Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_PAUSE
            })
        }

        binding.btnStop.setOnClickListener {
            requireContext().startService(Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_STOP
            })
        }
    }

    private fun startMusicService(intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Could not start music service. Check permissions.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkAndRequestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadFiles()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkAndRequestNotificationPermission()
                }
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    requireContext(),
                    "Music permission is needed to play audio files",
                    Toast.LENGTH_LONG
                ).show()
                audioPermissionLauncher.launch(permission)
            }
            else -> {
                audioPermissionLauncher.launch(permission)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("ServiceFragment", "Permission is already granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Toast.makeText(
                    requireContext(),
                    "Notification permission is needed for the music player",
                    Toast.LENGTH_LONG
                ).show()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateButtonStates(enabled: Boolean) {
        binding.btnStart.isEnabled = enabled
        binding.btnPause.isEnabled = enabled
        binding.btnStop.isEnabled = enabled
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(MusicService.ACTION_UPDATE_SONG).apply {
            addAction(MusicService.ACTION_NO_SONG)
        }
        try {
            ContextCompat.registerReceiver(
                requireContext(),
                songUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            Log.e("ServiceFragment", "Error registering receiver: ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(songUpdateReceiver)
        } catch (e: Exception) {
            Log.e("ServiceFragment", "Error registering receiver: ${e.message}")
        }
    }
}