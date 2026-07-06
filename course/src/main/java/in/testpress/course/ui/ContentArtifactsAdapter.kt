package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.ItemContentArtifactBinding
import `in`.testpress.course.domain.DomainContentArtifact
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ContentArtifactsAdapter(
    private val onArtifactClick: (DomainContentArtifact) -> Unit
) : ListAdapter<DomainContentArtifact, ContentArtifactsAdapter.ArtifactViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtifactViewHolder {
        val binding = ItemContentArtifactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ArtifactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtifactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtifactViewHolder(
        private val binding: ItemContentArtifactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artifact: DomainContentArtifact) {
            binding.tvArtifactName.text = artifact.name

            if (artifact.isAccessible) {
                binding.ivArtifactDownload.isVisible = true
                binding.ivArtifactLocked.isVisible = false
                binding.artifactItemRoot.isEnabled = true
                binding.artifactItemRoot.setOnClickListener { onArtifactClick(artifact) }
            } else {
                binding.ivArtifactDownload.isVisible = false
                binding.ivArtifactLocked.isVisible = true
                binding.artifactItemRoot.isEnabled = false
                binding.artifactItemRoot.setOnClickListener {
                    onArtifactClick(artifact) // still notify so Fragment can show a toast
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DomainContentArtifact>() {
            override fun areItemsTheSame(old: DomainContentArtifact, new: DomainContentArtifact) =
                old.id == new.id

            override fun areContentsTheSame(old: DomainContentArtifact, new: DomainContentArtifact) =
                old == new
        }
    }
}
