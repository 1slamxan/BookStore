package com.example.bookapp.activities;

import static com.example.bookapp.utils.Constants.PROFILE_EDIT_TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
import com.example.bookapp.model.ModelUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    private ActivityProfileEditBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private String name;
    private String email;
    private ProgressDialog progressDialog;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Log.d(PROFILE_EDIT_TAG, "onActivityResult: Picked From Camera " + imageUri);
                        Intent data = o.getData();

                        binding.profileIv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        imageUri = data.getData();
                        Log.d(PROFILE_EDIT_TAG, "onActivityResult: Picked From Gallery " + imageUri);
                        binding.profileIv.setImageURI(imageUri);

                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите!");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.profileIv.setOnClickListener(v -> showImageAttachMenu());
        binding.updateBtn.setOnClickListener(v -> validateData());
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Введите свое имя!", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Неверный адрес электронной почты", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri == null) {
                updateProfile("");
            } else {
                uploadImage();
            }
        }

    }

    private void uploadImage() {
        Log.d(PROFILE_EDIT_TAG, "uploadImage: Uploading profile image...");
        progressDialog.setMessage("Обновление изображения профиля...");
        progressDialog.show();

        StorageReference reference = FirebaseStorage.getInstance().getReference("ProfileImage/" + firebaseAuth.getUid());
        reference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(PROFILE_EDIT_TAG, "onSuccess: Profile image uploaded");
                    Log.d(PROFILE_EDIT_TAG, "onSuccess: Getting url of uploaded image");
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedImageUrl = "" + uriTask.getResult();

                    Log.d(PROFILE_EDIT_TAG, "onSuccess: Uploaded Image URL " + uploadedImageUrl);
                    updateProfile(uploadedImageUrl);
                })
                .addOnFailureListener(e -> {
                    Log.d(PROFILE_EDIT_TAG, "onFailure: Failed to update profile image due to " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, "Не удалось обновить изображение профиля из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void updateProfile(String imageUrl) {
        Log.d(PROFILE_EDIT_TAG, "updateProfile: Updating user profile...");
        progressDialog.setMessage("Обновление профиля пользователя...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        hashMap.put("email", "" + email);
        if (imageUri != null) {
            hashMap.put("profileImage", imageUrl);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(PROFILE_EDIT_TAG, "updateProfile: Profile updated");
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d(PROFILE_EDIT_TAG, "updateProfile: Failed to update due to " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, "Не удалось обновить из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void showImageAttachMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Камера");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Галерея");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which == 0) {
                pickImageCamera();
            } else if (which == 1) {
                pickImageGallery();
            }
            return false;
        });
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void loadUserInfo() {
        Log.d(PROFILE_EDIT_TAG, "loadUerInfo: loading uer info...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser modelUser = snapshot.getValue(ModelUser.class);

                        binding.nameEt.setText(modelUser.getName());
                        binding.emailEt.setText(modelUser.getEmail());

                        Glide.with(ProfileEditActivity.this)
                                .load(modelUser.getProfileImage())
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}