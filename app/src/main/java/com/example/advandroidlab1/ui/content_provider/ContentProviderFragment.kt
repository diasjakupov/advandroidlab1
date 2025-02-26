package com.example.advandroidlab1.ui.content_provider

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.advandroidlab1.databinding.FragmentContentProviderBinding
import com.example.advandroidlab1.ui.content_provider.adapter.CalendarEventAdapter
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding
import kotlinx.coroutines.launch


class ContentProviderFragment : BaseFragment<FragmentContentProviderBinding, ContentProviderViewModel>() {

    override val binding: FragmentContentProviderBinding by viewBinding(FragmentContentProviderBinding::inflate)
    override val viewModel: ContentProviderViewModel by viewModels<ContentProviderViewModel>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchUpcomingEvents()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CalendarEventAdapter()
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvEvents.adapter = adapter
        checkPermission()


        lifecycleScope.launch {
            viewModel.eventsFlow.collect{ eventsList ->
                adapter.updateList(eventsList)
            }
        }
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.fetchUpcomingEvents()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }
}