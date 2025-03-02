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

class SearchAdapter(private val onSearchArticleClicked: (Article) -> Unit): ListAdapter<Article, SearchAdapter.SearchViewHolder>(DiffUtilComparison()) {

    inner class SearchViewHolder(val binding: ItemNewsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemNewsBinding.inflate(layoutInflater, parent, false)


        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {

        val searchArticleItem = getItem(position)

        holder.binding.root.setOnClickListener {

            onSearchArticleClicked(searchArticleItem)
        }

        if (searchArticleItem.urlToImage != null) {

            holder.binding.inIvImage.load(searchArticleItem.urlToImage) {

                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }else {

            holder.binding.inIvImage.load(R.drawable.image_placeholder)
        }

        holder.binding.inTvSource.text = searchArticleItem.source?.name ?: "Source"
        holder.binding.inTvTitle.text = searchArticleItem.title ?: "Title"
        holder.binding.inTvAuthor.text = searchArticleItem.author ?: "Author"
        holder.binding.inTvPublishedDate.text = searchArticleItem.publishedAt?.formattedDate ?: "(Date)"
    }

    private class DiffUtilComparison: DiffUtil.ItemCallback<Article>() {

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {

            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {

            return oldItem == newItem
        }
    }
}