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
import com.himangskalita.newsly.data.models.BookmarkArticle
import com.himangskalita.newsly.databinding.ItemBookmarkBinding

class BookmarkAdapter : ListAdapter<BookmarkArticle, BookmarkAdapter.BookmarkViewholder>(DiffUtilComparison()) {

    inner class BookmarkViewholder(val binding: ItemBookmarkBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewholder {

        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BookmarkViewholder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewholder, position: Int) {

        val bookmarkArticle = getItem(position)

        if (bookmarkArticle.urlToImage != null) {

            holder.binding.inIvImage.load(bookmarkArticle.urlToImage) {

                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }else {

            holder.binding.inIvImage.load(R.drawable.image_placeholder)
        }

        holder.binding.inTvSource.text = bookmarkArticle.source?.name ?: "Source"
        holder.binding.inTvTitle.text = bookmarkArticle.title ?: "Title"
        holder.binding.inTvAuthor.text = bookmarkArticle.author ?: "Author"
        holder.binding.inTvPublishedDate.text = bookmarkArticle.publishedAt ?: "Date"

        holder.binding.ItemBookmarkView.setOnClickListener {


        }
    }

    private class DiffUtilComparison : DiffUtil.ItemCallback<BookmarkArticle>() {

        override fun areItemsTheSame(oldItem: BookmarkArticle, newItem: BookmarkArticle): Boolean {

            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(
            oldItem: BookmarkArticle,
            newItem: BookmarkArticle
        ): Boolean {

            return oldItem == newItem
        }
    }
}