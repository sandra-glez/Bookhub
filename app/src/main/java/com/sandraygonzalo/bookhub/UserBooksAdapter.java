package com.sandraygonzalo.bookhub;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        Button deleteButton; // nuevo botón

        public BookViewHolder(View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.bookCover);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            deleteButton = itemView.findViewById(R.id.deleteBookButton);
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

        // Acción eliminar
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar libro")
                    .setMessage("¿Estás seguro de que quieres eliminar este libro?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("userBooks")
                                .document(book.getId()) // el modelo debe tener .setId() hecho en ProfileActivity
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    bookList.remove(position);
                                    notifyItemRemoved(position);
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


