package com.example.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class LecturerDashboard extends AppCompatActivity {

    TextView edtDate, txtTime;
    Button btnLogout, btnOpenSession;

    Spinner spinnerCourse, spinnerDuration;

    ArrayList<String> courseList;
    ArrayAdapter<String> courseAdapter, durationAdapter;

    DatabaseReference courseRef, sessionRef;

    String selectedCourse, selectedDate, selectedTime;
    int selectedDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        // ================= UI =================
        edtDate = findViewById(R.id.edtDate);
        txtTime = findViewById(R.id.txtTime);

        btnLogout = findViewById(R.id.btnLogout);
        btnOpenSession = findViewById(R.id.btnOpenSession);

        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerDuration = findViewById(R.id.spinnerDuration);

        // ================= FIREBASE =================
        courseRef = FirebaseDatabase.getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/").getReference("Courses");
        sessionRef = FirebaseDatabase.getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/").getReference("CourseSessions");

        // ================= COURSE LIST =================
        courseList = new ArrayList<>();

        courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseList
        );
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        loadCourses();

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courseList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ================= DATE PICKER =================
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = day + "/" + (month + 1) + "/" + year;
                        edtDate.setText(selectedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));

            dp.show();
        });

        // ================= TIME PICKER =================
        txtTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            TimePickerDialog tp = new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        selectedTime = String.format("%02d:%02d", hour, minute);
                        txtTime.setText(selectedTime);
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true);

            tp.show();
        });

        // ================= DURATION =================
        String[] durations = {"10", "20", "30", "40", "50", "60"};

        durationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                durations
        );

        spinnerDuration.setAdapter(durationAdapter);

        spinnerDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDuration = Integer.parseInt(durations[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ================= OPEN SESSION =================
        btnOpenSession.setOnClickListener(v -> openSession());

        // ================= LOGOUT =================
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // ================= LOAD COURSES =================
    private void loadCourses() {

        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                courseList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String code = snap.getKey();
                    if (code != null) courseList.add(code);
                }

                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= OPEN SESSION =================
    private void openSession() {

        if (selectedCourse == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String lecturerEmail = user.getEmail();

        courseRef.child(selectedCourse)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String venueCode = snapshot.child("venueCode").getValue(String.class);

                        String sessionId = "Session_" + System.currentTimeMillis();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("courseCode", selectedCourse);
                        map.put("lecturerEmail", lecturerEmail);
                        map.put("venueCode", venueCode);
                        map.put("date", selectedDate);
                        map.put("time", selectedTime);
                        map.put("duration", selectedDuration);
                        map.put("status", "open");
                        map.put("openedAt", System.currentTimeMillis());

                        sessionRef.child(selectedCourse)
                                .child(sessionId)
                                .setValue(map)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(LecturerDashboard.this,
                                                "Session Opened",
                                                Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(LecturerDashboard.this,
                                                e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ================= LOGOUT =================
    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}