package com.sandraygonzalo.bookhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<UserBook> bookList;

    public BookAdapter(List<UserBook> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_card, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        UserBook book = bookList.get(position);

        holder.titleText.setText(book.getTitle());
        holder.authorText.setText(book.getAuthor());
        holder.descriptionText.setText(book.getDescription());

        // Si tu libro tiene imagen, puedes usar Glide aquí:
        /*
        if (book.getCoverImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                .load(book.getCoverImageUrl())
                .into(holder.coverImage);
        }
        */

        // TODO: Añadir onClickListener si quieres abrir detalles o perfil
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, authorText, descriptionText;
        // ImageView coverImage; // si lo necesitas

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.bookTitle);
            authorText = itemView.findViewById(R.id.bookAuthor);
            descriptionText = itemView.findViewById(R.id.bookDescription);
            // coverImage = itemView.findViewById(R.id.bookCover);
        }
    }
}
