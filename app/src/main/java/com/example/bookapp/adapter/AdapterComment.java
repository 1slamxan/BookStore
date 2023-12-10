package com.example.bookapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.databinding.RowCommentBinding;
import com.example.bookapp.model.ModelComment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment> {
    private final Context context;
    private ArrayList<ModelComment> commentArrayList;
    private RowCommentBinding binding;
    private FirebaseAuth firebaseAuth;

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        ModelComment modelComment = commentArrayList.get(position);
        String date = MyApplication.formatTimestamp(modelComment.getTimestamp());

        holder.dateTv.setText(date);
        holder.commentTv.setText(modelComment.getComment());

        loadUsersDetails(modelComment, holder);

        holder.itemView.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() != null && modelComment.getUid().equals(firebaseAuth.getUid())) {
                deleteComment(modelComment, holder);
            }
        });
    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Удалить комментарий")
                .setMessage("Вы уверены, что хотите удалить этот комментарий?")
                .setPositiveButton("УДАЛИТЬ", (dialog, which) -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                    reference.child(modelComment.getBookId())
                            .child("Comments")
                            .child(modelComment.getId())
                            .removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(context, "Удалено...", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Не удалось удалить комментарий из-за " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("ОТМЕНА", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadUsersDetails(ModelComment modelComment, HolderComment holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelComment.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        holder.nameTv.setText(snapshot.child("name").getValue(String.class));

                        try {
                            Glide.with(context)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(holder.profileIv);
                        } catch (Exception e) {
                            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class HolderComment extends RecyclerView.ViewHolder {

        ShapeableImageView profileIv;
        TextView nameTv, dateTv, commentTv;

        public HolderComment(@NonNull View itemView) {
            super(itemView);

            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
        }
    }
}
