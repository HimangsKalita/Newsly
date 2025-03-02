package com.himangskalita.newsly.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.himangskalita.newsly.R
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.databinding.ItemNewsBinding

class NewsAdapter(private val onArticleClick: (Article) -> Unit) : ListAdapter<Article, NewsAdapter.NewsViewHolder>(DiffUtilComparison()) {

    class NewsViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemNewsBinding.inflate(layoutInflater, parent, false)

        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {

        val articleItem = getItem(position)

        holder.binding.root.setOnClickListener {

            onArticleClick(articleItem)
        }

        if (articleItem.urlToImage != null) {

            holder.binding.inIvImage.load(articleItem.urlToImage) {

                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }else {

            holder.binding.inIvImage.load(R.drawable.image_placeholder)
        }

        holder.binding.inTvSource.text = articleItem.source?.name ?: "Source"
        holder.binding.inTvTitle.text = articleItem.title ?: "Title"
        holder.binding.inTvAuthor.text = articleItem.author ?: "Author"
        holder.binding.inTvPublishedDate.text = articleItem.publishedAt?.formattedDate ?: "(Date)"

    }

    private class DiffUtilComparison : DiffUtil.ItemCallback<Article>() {

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {

            return oldItem == newItem
        }
    }

    fun isLastItemOfRecyclerView(position: Int): Boolean {

        return position == itemCount - 1
    }
}