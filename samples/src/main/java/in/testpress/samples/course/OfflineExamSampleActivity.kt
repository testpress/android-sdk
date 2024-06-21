package `in`.testpress.samples.course

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.enums.Status
import `in`.testpress.samples.databinding.ActivityOfflineExamSampleBinding
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OfflineExamSampleActivity: BaseToolBarActivity() {

    private lateinit var binding: ActivityOfflineExamSampleBinding
    private lateinit var offlineExamViewModel: OfflineExamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineExamSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViewModel()
        setActionBarTitle("Offline Exam")

        binding.downloads.setOnClickListener {
            if (binding.editText.text.isNullOrEmpty()){
                Toast.makeText(this,"Content Id required to download the exam",Toast.LENGTH_SHORT).show()
            } else {
                offlineExamViewModel.downloadExam(binding.editText.text.toString().toLong())
            }
        }

        observeDownloadExamResult()
    }

    private fun initializeViewModel() {
        offlineExamViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineExamViewModel(
                    OfflineExamRepository(this@OfflineExamSampleActivity)
                ) as T
            }
        })[OfflineExamViewModel::class.java]
    }

    private fun observeDownloadExamResult() {
        offlineExamViewModel.downloadExamResult.observe(this){ result ->
            when(result.status){
                Status.SUCCESS -> {
                    binding.downloads.isVisible = true
                    binding.loadingProgressBar.isVisible = false
                    Toast.makeText(this,"Download completed",Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    binding.loadingProgressBar.isVisible = true
                    binding.downloads.isVisible = false
                }
                Status.ERROR -> {
                    binding.downloads.isVisible = true
                    binding.loadingProgressBar.isVisible = false
                    Toast.makeText(this,"Download Failed",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


}