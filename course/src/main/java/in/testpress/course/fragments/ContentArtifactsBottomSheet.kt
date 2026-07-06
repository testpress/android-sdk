package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.databinding.FragmentContentArtifactsBottomSheetBinding
import `in`.testpress.course.domain.DomainContentArtifact
import `in`.testpress.course.ui.ContentArtifactsAdapter
import `in`.testpress.course.viewmodels.ContentArtifactsViewModel
import `in`.testpress.course.repository.ContentArtifactsRepository
import `in`.testpress.enums.Status
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ContentArtifactsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentContentArtifactsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContentArtifactsViewModel
    private lateinit var adapter: ContentArtifactsAdapter

    private var contentId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentArtifactsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentId = arguments?.getLong(ARG_CONTENT_ID, -1L) ?: -1L

        setupRecyclerView()
        setupViewModel()
        loadArtifacts()
    }

    private fun setupRecyclerView() {
        adapter = ContentArtifactsAdapter { artifact -> onArtifactClicked(artifact) }
        binding.rvArtifacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContentArtifactsBottomSheet.adapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentArtifactsViewModel(ContentArtifactsRepository(requireContext())) as T
            }
        })[ContentArtifactsViewModel::class.java]
    }

    private fun loadArtifacts() {
        if (contentId == -1L) {
            dismiss()
            return
        }

        binding.btnRetry.setOnClickListener { loadArtifacts() }

        viewModel.loadArtifacts(contentId).observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    val artifacts = resource.data.orEmpty()
                    if (artifacts.isEmpty()) {
                        showEmptyState()
                    } else {
                        showArtifacts(artifacts)
                    }
                }
                Status.ERROR -> showError()
            }
        }
    }

    private fun showLoading() {
        binding.progressBarArtifacts.isVisible = true
        binding.rvArtifacts.isVisible = false
        binding.tvNoArtifacts.isVisible = false
        binding.layoutError.isVisible = false
    }

    private fun showArtifacts(artifacts: List<DomainContentArtifact>) {
        binding.progressBarArtifacts.isVisible = false
        binding.rvArtifacts.isVisible = true
        binding.tvNoArtifacts.isVisible = false
        binding.layoutError.isVisible = false
        adapter.submitList(artifacts)
    }

    private fun showEmptyState() {
        binding.progressBarArtifacts.isVisible = false
        binding.rvArtifacts.isVisible = false
        binding.tvNoArtifacts.isVisible = true
        binding.layoutError.isVisible = false
    }

    private fun showError() {
        binding.progressBarArtifacts.isVisible = false
        binding.rvArtifacts.isVisible = false
        binding.tvNoArtifacts.isVisible = false
        binding.layoutError.isVisible = true
    }

    private fun onArtifactClicked(artifact: DomainContentArtifact) {
        if (artifact.isAccessible && artifact.url != null) {
            downloadArtifact(artifact)
        } else {
            Toast.makeText(
                requireContext(),
                R.string.testpress_resource_locked_message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun downloadArtifact(artifact: DomainContentArtifact) {
        val url = artifact.url ?: return
        try {
            val uri = Uri.parse(url)
            val fileName = "${artifact.name}.${uri.lastPathSegment?.substringAfterLast('.') ?: "file"}"
            val request = DownloadManager.Request(uri).apply {
                setTitle(artifact.name)
                setDescription(getString(R.string.testpress_download))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/octet-stream")
            }
            val downloadManager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(
                requireContext(),
                getString(R.string.testpress_download) + " started",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // Fallback: open in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ContentArtifactsBottomSheet"
        private const val ARG_CONTENT_ID = "arg_content_id"

        fun newInstance(contentId: Long): ContentArtifactsBottomSheet {
            return ContentArtifactsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CONTENT_ID, contentId)
                }
            }
        }
    }
}
