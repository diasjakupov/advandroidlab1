package com.example.advandroidlab1.ui.content_provider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.advandroidlab1.databinding.BottomSheetEventDetailsBinding
import com.example.advandroidlab1.databinding.FragmentContentProviderBinding
import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent
import com.example.advandroidlab1.ui.core.BaseBottomSheetDialogFragment
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsBottomSheet  :
    BaseBottomSheetDialogFragment<BottomSheetEventDetailsBinding, ContentProviderViewModel>() {

    override val binding: BottomSheetEventDetailsBinding by viewBinding(BottomSheetEventDetailsBinding::inflate)
    override val viewModel: ContentProviderViewModel by viewModels<ContentProviderViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val event = arguments?.getParcelable<CalendarEvent>(ARG_EVENT)
        event?.let {
            binding.tvEventTitle.text = it.title
            binding.tvEventDescription.text = "Description: " + (it.description ?: "No Description")
            binding.tvEventStatus.text = "Status: " + (it.status ?: "No Status")
            val durationInMinutes = ((it.end - it.begin) / (1000 * 60))
            binding.tvEventDuration.text = "Duration: $durationInMinutes minutes"
        }
    }


    companion object {
        private const val ARG_EVENT = "arg_event"


        fun newInstance(event: CalendarEvent): EventDetailsBottomSheet {
            val fragment = EventDetailsBottomSheet()
            val args = Bundle()
            args.putParcelable(ARG_EVENT, event)
            fragment.arguments = args
            return fragment
        }
    }
}
