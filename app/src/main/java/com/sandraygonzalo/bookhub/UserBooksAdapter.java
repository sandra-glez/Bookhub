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

    public UserBooksAdapter(List<UserBook> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
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
        holder.genreChipContainer.removeAllViews(); // limpiar antes de reutilizar
        for (String genre : book.getGenres()) {
            TextView chip = new TextView(context);
            chip.setText(genre);
            chip.setBackgroundResource(R.drawable.chip_background_checked); // fondo verde redondeado
            chip.setTextColor(Color.WHITE);
            chip.setTextSize(12f);
            chip.setPadding(28, 10, 28, 10);
            chip.setTypeface(Typeface.DEFAULT_BOLD);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 8, 12, 8);
            chip.setLayoutParams(params);

            holder.genreChipContainer.addView(chip);
        }

        // Acción eliminar
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar libro")
                    .setMessage("¿Estás seguro de que quieres eliminar este libro?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("userBooks")
                                .document(book.getId()) // asegúrate de que el modelo tenga setId() en ProfileActivity
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
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}



