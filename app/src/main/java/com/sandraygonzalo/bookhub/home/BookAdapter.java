package com.sandraygonzalo.bookhub.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sandraygonzalo.bookhub.R;
import com.sandraygonzalo.bookhub.profiles.UserBook;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private List<UserBook> bookList;

    public BookAdapter(List<UserBook> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView titleText, authorText;

        public BookViewHolder(View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.book_cover);
            titleText = itemView.findViewById(R.id.book_title);
            authorText = itemView.findViewById(R.id.book_author);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        UserBook book = bookList.get(position);
        holder.titleText.setText(book.getTitle());
        holder.authorText.setText(book.getAuthor());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("book", new Gson().toJson(book)); // Pasamos el objeto como JSON
            context.startActivity(intent);
        });


        Glide.with(holder.itemView.getContext())
                .load(book.getCoverImage())
                .into(holder.coverImage);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}



