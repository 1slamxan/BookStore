package com.example.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.bookapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.logBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        binding.skipBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DashboardUserActivity.class)));
    }
}