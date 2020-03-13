package `in`.testpress.exam.ui.adapters

import `in`.testpress.exam.R
import `in`.testpress.exam.ui.OnShareAppListener
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.share_app_item.view.*

class ShareToUnlockAdapter(
    val values: List<ResolveInfo>,
    val manager: PackageManager,
    val listener: OnShareAppListener
) :
    RecyclerView.Adapter<ShareToUnlockAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as ResolveInfo
            listener.onClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.share_app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.iconView.setImageDrawable(values[position].loadIcon(manager))
        holder.titleView.text = item.loadLabel(manager)

        with(holder.view) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val iconView: ImageView = view.icon
    }
}