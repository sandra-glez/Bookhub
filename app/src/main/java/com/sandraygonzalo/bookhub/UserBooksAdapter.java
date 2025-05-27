package com.sandraygonzalo.bookhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserBooksAdapter extends RecyclerView.Adapter<UserBooksAdapter.BookViewHolder> {

    private List<UserBook> bookList;
    private Context context;

    public UserBooksAdapter(List<UserBook> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView title, author;

        public BookViewHolder(View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.bookCover);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        UserBook book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());

        Glide.with(context)
                .load(book.getCoverImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.coverImage);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}

