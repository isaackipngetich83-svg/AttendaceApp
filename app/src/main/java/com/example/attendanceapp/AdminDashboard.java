package com.example.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {
    LinearLayout btnopenaddgps,btnopenaddcourse;
    MaterialButton btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        btnopenaddgps = findViewById(R.id.btnopenaddgps) ;
        btnopenaddcourse = findViewById(R.id.btnopenaddcourse) ;
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        btnopenaddgps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(AdminDashboard.this,AddGPS.class);
                startActivity(intent);
                finish();
            }
        });

        btnopenaddcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(AdminDashboard.this,AddCourse.class);
                startActivity(intent);
                finish();
            }
        });


        btnAdminLogout.setOnClickListener(v -> logoutUser());

    }



    // logout logic
    private void logoutUser() {

        // Firebase logout
        FirebaseAuth.getInstance().signOut();

        // Clear Remember Me
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear(); // removes remember + role
        editor.apply();

        // Go back to login screen
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}