package `in`.testpress.course.fragments

import `in`.testpress.course.databinding.OfflineExamOptionsBottomSheetBinding
import `in`.testpress.database.entities.OfflineExam
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OfflineExamOptionsBottomSheet: BottomSheetDialogFragment() {

    private var _binding: OfflineExamOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!
    var offlineExam: OfflineExam? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OfflineExamOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomSheetTitle.text = offlineExam?.title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}