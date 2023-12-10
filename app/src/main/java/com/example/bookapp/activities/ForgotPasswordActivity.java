package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите!");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.submitBtn.setOnClickListener(v -> validateData());
    }

    private void validateData() {
        email = binding.emailEt.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите электронную почту...", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Неверный адрес электронной почты", Toast.LENGTH_SHORT).show();
        } else {
            recoverPassword();
        }
    }

    private void recoverPassword() {
        progressDialog.setMessage("Отправка инструкций по восстановлению пароля на " + email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "Инструкции по восстановлению пароля отправлены на " + email, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "Не удалось отправить из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}