package com.sandraygonzalo.bookhub;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserBooksAdapter extends RecyclerView.Adapter<UserBooksAdapter.BookViewHolder> {

    private List<UserBook> bookList;
    private Context context;
    private boolean isOwner;

    public UserBooksAdapter(List<UserBook> bookList, Context context, boolean isOwner) {
        this.bookList = bookList;
        this.context = context;
        this.isOwner = isOwner;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView title, author;
        ImageButton deleteButton;
        FlexboxLayout genreChipContainer;

        public BookViewHolder(View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.bookCover);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            deleteButton = itemView.findViewById(R.id.deleteBookButton);
            genreChipContainer = itemView.findViewById(R.id.genreChipContainer);
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

        // Cargar chips de géneros
        holder.genreChipContainer.removeAllViews();
        for (String genre : book.getGenres()) {
            TextView chip = new TextView(context);
            chip.setText(genre);
            chip.setBackgroundResource(R.drawable.chip_background_checked);
            chip.setTextColor(Color.WHITE);
            chip.setTextSize(10f);
            chip.setPadding(20, 10, 20, 10);
            chip.setTypeface(Typeface.DEFAULT_BOLD);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 8, 10, 8);
            chip.setLayoutParams(params);

            holder.genreChipContainer.addView(chip);
        }

        // Mostrar u ocultar botón de eliminar según si es el dueño
        if (isOwner) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Eliminar libro")
                        .setMessage("¿Estás seguro de que quieres eliminar este libro?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("userBooks")
                                    .document(book.getId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        int currentPos = holder.getAdapterPosition();
                                        if (currentPos != RecyclerView.NO_POSITION) {
                                            bookList.remove(currentPos);
                                            notifyItemRemoved(currentPos);
                                        }
                                        Toast.makeText(context, "Libro eliminado", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null);
        }
    }


    @Override
    public int getItemCount() {
        return bookList.size();
    }
}



