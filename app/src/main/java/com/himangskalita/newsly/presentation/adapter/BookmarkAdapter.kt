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

class BookmarkAdapter(private val onBookmarkClick: (Article) -> Unit) :
    ListAdapter<BookmarkArticle, BookmarkAdapter.BookmarkViewHolder>(DiffUtilComparison()) {

    inner class BookmarkViewHolder(val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            ItemBookmarkBinding.inflate(layoutInflater, parent, false)

        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {

        val bookmarkArticle = getItem(position)
        val article = convertBookmarkToArticle(bookmarkArticle)

        holder.binding.root.setOnClickListener {

            onBookmarkClick(article)
        }

        if (bookmarkArticle.urlToImage != null) {

            holder.binding.inIvImage.load(bookmarkArticle.urlToImage) {

                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        } else {

            holder.binding.inIvImage.load(R.drawable.image_placeholder)
        }

        holder.binding.inTvSource.text = bookmarkArticle.source?.name ?: "Source"
        holder.binding.inTvTitle.text = bookmarkArticle.title ?: "Title"
        holder.binding.inTvAuthor.text = bookmarkArticle.author ?: "Author"
        holder.binding.inTvPublishedDate.text = bookmarkArticle.publishedAt ?: "Date"
    }

    private fun convertBookmarkToArticle(bookmarkArticle: BookmarkArticle) = Article(
        author = bookmarkArticle.author,
        content = bookmarkArticle.content,
        description = bookmarkArticle.description,
        publishedAt = bookmarkArticle.publishedAt,
        source = bookmarkArticle.source,
        title = bookmarkArticle.title,
        url = bookmarkArticle.url,
        urlToImage = bookmarkArticle.urlToImage
    )

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