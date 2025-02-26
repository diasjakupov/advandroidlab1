package com.example.advandroidlab1.ui.deeplinks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.advandroidlab1.data.sharing.DefaultInstagramStoriesSharing
import com.example.advandroidlab1.databinding.FragmentDeeplinksBinding
import com.example.advandroidlab1.domain.SocialMediaImageSharing
import com.example.advandroidlab1.ui.core.BaseFragment
import com.example.advandroidlab1.ui.core.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "DeeplinkFragment"

class DeeplinkFragment : BaseFragment<FragmentDeeplinksBinding, DeeplinksViewModel>() {

    override val binding by viewBinding(FragmentDeeplinksBinding::inflate)
    override val viewModel: DeeplinksViewModel by viewModels<DeeplinksViewModel>()

    private var pickImageLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
            viewModel.setUri(uri)
    }

    private lateinit var shareManager: SocialMediaImageSharing


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        shareManager = DefaultInstagramStoriesSharing(requireContext())
        setupClicks()
        observeUri()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun setupClicks() = with(binding){
        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        btnShareStory.setOnClickListener {
            shareManager.share(viewModel.uri.value)
        }
    }

    private fun observeUri() = lifecycleScope.launch {
        viewModel.uri.collectLatest {
            binding.imagePreview.setImageURI(it)
        }
    }
}