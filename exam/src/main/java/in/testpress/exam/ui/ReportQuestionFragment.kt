package `in`.testpress.exam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.enums.Status
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.databinding.ReportQuestionFragmentBinding
import `in`.testpress.exam.models.ReportQuestionResponse
import `in`.testpress.network.Resource

class ReportQuestionFragment : Fragment() {

    private var questionId: String = ""
    private lateinit var apiClient: TestpressExamApiClient
    private lateinit var binding: ReportQuestionFragmentBinding
    private lateinit var viewModel: ReportQuestionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = TestpressExamApiClient(requireContext())
        parseArguments()
        initializeViewModel()
    }

    private fun parseArguments() {
        questionId = arguments!!.getString("question_id")?:""
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReportQuestionViewModel(apiClient,questionId) as T
            }
        }).get(ReportQuestionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ReportQuestionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.reportQuestion.observe(this){ resource ->
            when (resource.status) {
                Status.LOADING ->{
                    Toast.makeText(requireContext(),"Loading",Toast.LENGTH_SHORT).show()
                }
                Status.SUCCESS -> {
                    if(resource.data == null){
                        Toast.makeText(requireContext(),"Null",Toast.LENGTH_SHORT).show()
                    } else {
                        validateContainer(resource.data!!)
                    }
                }
                Status.ERROR ->{
                    Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateContainer(result: ReportQuestionResponse){
        if (result.is_report_resolved){
            showQuestionResolvedContainer()
            return
        }
        if (result.results.isNotEmpty()){
            showAlreadyReportQuestionContainer()
            bindView(result.results)
            return
        }
        showReportQuestionContainer()
    }

    private fun showReportQuestionContainer(){
        binding.reportQuestionContainer.visibility = View.VISIBLE
        binding.submitButton.setOnClickListener {
            viewModel.submit()
        }
    }

    private fun showAlreadyReportQuestionContainer(){
        binding.alreadyRepotedQuestionContainer.visibility = View.VISIBLE
    }

    private fun showQuestionResolvedContainer(){
        binding.resolvedContainer.visibility = View.VISIBLE
    }

    private fun bindView(result: List<ReportQuestionResponse.ReportQuestion>){
        binding.reportedReason.text = "Reason: %s".format(result[0].type_display)
        binding.reportedTime.text = "Time: %s".format(result[0].created)
        binding.reportedDiscription.text = "Discription: %s".format(result[0].description.ifEmpty { "NA" })
    }

}

class ReportQuestionViewModel(
    private val apiClient: TestpressExamApiClient,
    private val questionId: String
) : ViewModel() {

    private var _reportQuestion: MutableLiveData<Resource<ReportQuestionResponse>> = MutableLiveData()
    val reportQuestion: LiveData<Resource<ReportQuestionResponse>>
        get() = _reportQuestion

    init {
        getReportQuestion()
        _reportQuestion.postValue(Resource.loading(null))
    }

    private fun getReportQuestion() {
        apiClient.getReportQuestionResponse(questionId).enqueue(object :TestpressCallback<ReportQuestionResponse>(){
            override fun onSuccess(result: ReportQuestionResponse) {
                _reportQuestion.postValue(Resource.success(result))
            }

            override fun onException(exception: TestpressException) {
                _reportQuestion.postValue(Resource.error(exception,null))
            }

        })
    }

    fun submit(){
        _reportQuestion.postValue(Resource.loading(null))
        apiClient.postReportQuestion(questionId).enqueue(object :TestpressCallback<ReportQuestionResponse>(){
            override fun onSuccess(result: ReportQuestionResponse) {
                _reportQuestion.postValue(Resource.success(result))
            }

            override fun onException(exception: TestpressException) {
                _reportQuestion.postValue(Resource.error(exception,null))
            }

        })

    }

}