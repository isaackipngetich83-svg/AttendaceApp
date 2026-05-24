package com.example.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentDashboard extends AppCompatActivity {

    Button btnLogout, btnMyCourses,btnMarkAttendance;
    TextView txtdeviceid,txtStatus,txtrealdeviceID,txtCourseName,txtVenue,txtTimer,txtsessionTitle,txtGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        btnLogout = findViewById(R.id.btnLogout);
        btnMyCourses = findViewById(R.id.btnMyCourses);
        txtdeviceid = findViewById(R.id.txtdeviceid);
        txtStatus = findViewById(R.id.txtStatus);
        txtrealdeviceID = findViewById(R.id.txtrealdeviceID);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtVenue = findViewById(R.id.txtVenue);
        txtTimer = findViewById(R.id.txtTimer);
        txtsessionTitle = findViewById(R.id.txtsessionTitle);
        txtGps = findViewById(R.id.txtGps);


        // get device id
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        // display id
        txtdeviceid.setText(deviceId);

        // read device id of the logged in account from db
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            // no user
        }
        String loggedinuseremail = user.getEmail();
        txtStatus.setText(loggedinuseremail);
        String formatedemail = loggedinuseremail.replace("." , "_")
                .replace("@" , "_at_");

         // ready form database device id

// reference
        DatabaseReference refdeviceid = FirebaseDatabase
                .getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/")
                .getReference("userDetails");

// load device id
        refdeviceid.child(formatedemail).child("deviceId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String deviceid = snapshot.getValue(String.class);
                            if(deviceid.equals(deviceId)){
                                txtrealdeviceID.setText(deviceid);
                            }else{
                                txtrealdeviceID.setText("Not your device");
                                btnMarkAttendance.setVisibility(View.INVISIBLE);
                            }

                        } else {

                            txtStatus.setText("No device id found");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(StudentDashboard.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
 // ==================== check which course student is enrolled in +=====

        String studentEmail = user.getEmail();

        String formattedEmail = studentEmail.replace(".", "_")
                .replace("@", "_at_");

        DatabaseReference refEnrollments = FirebaseDatabase
                .getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/")
                .getReference("Enrollments");

        refEnrollments.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    // check if this user exists inside current course
                    if (courseSnapshot.child(formattedEmail).exists()) {
                        found = true;
                        String courseCode = courseSnapshot
                                .child(formattedEmail)
                                .child("courseCode")
                                .getValue(String.class);
                        String status = courseSnapshot
                                .child(formattedEmail)
                                .child("status")
                                .getValue(String.class);
                        Toast.makeText(StudentDashboard.this,
                                "Enrolled in: " + courseCode,
                                Toast.LENGTH_LONG).show();
                        loadCourseDetails(courseCode);
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(StudentDashboard.this,
                            "Student not enrolled",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(StudentDashboard.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT).show();

            }
        });


        // ================= MY COURSES =================
        btnMyCourses.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, StudentCourses.class);
            startActivity(intent);
        });

        // ================= LOGOUT =================
        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(StudentDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }

    private void loadCourseDetails(String courseCode) {

        DatabaseReference refCourses = FirebaseDatabase
                .getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/")
                .getReference("CourseSessions");

        refCourses.child(courseCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(StudentDashboard.this,
                                    "Course not found",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean foundOpenSession = false;

                        // LOOP sessions
                        for (DataSnapshot sessionSnap : snapshot.getChildren()) {

                            String status = sessionSnap.child("status").getValue(String.class);

                            if (status != null && status.equals("open")) {

                                foundOpenSession = true;

                                String date = sessionSnap.child("date").getValue(String.class);
                                String time = sessionSnap.child("time").getValue(String.class);
                                String venueCode = sessionSnap.child("venueCode").getValue(String.class);
                                String lecturerEmail = sessionSnap.child("lecturerEmail").getValue(String.class);
                                Long duration = sessionSnap.child("duration").getValue(Long.class);

                                String courseCode = sessionSnap.child("courseCode").getValue(String.class);

                                txtCourseName.setText(courseCode);
                                txtVenue.setText(venueCode);
                                txtTimer.setText("Date : " + date + " Time : " + time);
                                txtsessionTitle.setText(lecturerEmail);
                                loadVenueCoordinates(venueCode);
                                break; // stop after first open session
                            }
                        }

                        if (!foundOpenSession) {
                            Toast.makeText(StudentDashboard.this,
                                    "No active sessions (closed or upcoming)",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(StudentDashboard.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadVenueCoordinates(String venueCode) {

        DatabaseReference refVenues = FirebaseDatabase
                .getInstance("https://attendanceapp-36a50-default-rtdb.firebaseio.com/")
                .getReference("GPSVenues");

        refVenues.child(venueCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(StudentDashboard.this,
                                    "Venue not found",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Double latitude = snapshot.child("latitude").getValue(Double.class);
                        Double longitude = snapshot.child("longitude").getValue(Double.class);
                        String radius = snapshot.child("radius").getValue(String.class);
                        String venueName = snapshot.child("venueName").getValue(String.class);

                        txtGps.setText(
                                venueName + "\nLat: " + latitude +
                                        "\nLng: " + longitude +
                                        "\nRadius: " + radius + "m"
                        );

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(StudentDashboard.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}