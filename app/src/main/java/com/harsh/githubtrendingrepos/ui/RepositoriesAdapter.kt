package com.harsh.githubtrendingrepos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.githubtrendingrepos.R
import com.harsh.githubtrendingrepos.data.model.Repo
import kotlinx.android.synthetic.main.item_repository.view.*

class RepositoriesAdapter : RecyclerView.Adapter<RepositoriesAdapter.RepositoryViewHolder>() {

    inner class RepositoryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    private var onItemClickListener: ((Repo, Int) -> Unit)? = null
    var selectedItemUrl : String? = null
    var selectedItemPosition : Int = RecyclerView.NO_POSITION

    private val differCallback = object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem == newItem && oldItem.isSelected == newItem.isSelected
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.id == newItem.id && oldItem.isSelected == newItem.isSelected
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_repository,
                parent,
                false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repo = differ.currentList[position]
        holder.itemView.apply {
            holder.itemView.isSelected = repo.url == selectedItemUrl
            Glide.with(this).load(repo.owner.avatarUrl).into(avatar)
            name.text = repo.fullName
            description.text = repo.description
            language.text = repo.language

            setOnClickListener {
                onItemClickListener?.let { it(repo, position) }
            }
        }
    }

    fun setOnItemClickListener(listener: (Repo, Int) -> Unit) {
        onItemClickListener = listener
    }


}