package com.example.finalproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class TeacherDashboardActivity extends AppCompatActivity {

    // Dashboard
    LinearLayout layoutDashboard;
    ImageView teacherImgProfile;
    TextView tvWelcomeTeacher;

    Button btnMyProfile, btnViewAllAppointments, btnViewSurveys, btnLogoutTeacher;

    // Profile Section
    LinearLayout layoutProfile;
    ImageView editTeacherProfileImage;
    EditText etName, etSubject, etPhone, etQualification, etExperience, etDescription;
    Button btnTeacherChangePhoto, btnSaveProfile, btnBackDashboard;
    TextView tvSaveStatus;

    // Firestore
    DocumentReference userRef;
    String uid;

    // Image Upload
    Uri selectedImageUri = null;
    ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        uid = FirebaseUtils.uid();
        if (uid == null) {
            Toast.makeText(this, "Teacher must login online!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

        initViews();
        initImagePicker();
        loadTeacherProfile();
        setupListeners();
    }

    private void initViews() {

        // Dashboard
        layoutDashboard = findViewById(R.id.layoutDashboard);
        teacherImgProfile = findViewById(R.id.teacherImgProfile);
        tvWelcomeTeacher = findViewById(R.id.tvWelcomeTeacher);

        btnMyProfile = findViewById(R.id.btnMyProfile);
        btnViewAllAppointments = findViewById(R.id.btnViewAllAppointments);
        btnViewSurveys = findViewById(R.id.btnViewSurveys);
        btnLogoutTeacher = findViewById(R.id.btnLogoutTeacher);

        // Profile
        layoutProfile = findViewById(R.id.layoutProfile);

        editTeacherProfileImage = findViewById(R.id.editTeacherProfileImage);
        etName = findViewById(R.id.etName);
        etSubject = findViewById(R.id.etSubject);
        etPhone = findViewById(R.id.etPhone);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        etDescription = findViewById(R.id.etDescription);

        btnTeacherChangePhoto = findViewById(R.id.btnTeacherChangePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBackDashboard = findViewById(R.id.btnBackDashboard);
        tvSaveStatus = findViewById(R.id.tvSaveStatus);
    }

    // ----------------- IMAGE PICKER -------------------
    private void initImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(editTeacherProfileImage);
                    }
                }
        );
    }

    // ----------------- LOAD ONLINE PROFILE -------------------
    private void loadTeacherProfile() {
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {

                String name = doc.getString("name");
                String imgUrl = doc.getString("profileImageUrl");

                tvWelcomeTeacher.setText("Welcome, " + name);

                etName.setText(name);
                etSubject.setText(doc.getString("subject"));
                etPhone.setText(doc.getString("phone"));
                etQualification.setText(doc.getString("qualification"));
                etExperience.setText(doc.getString("experience"));
                etDescription.setText(doc.getString("description"));

                Glide.with(this)
                        .load(imgUrl)
                        .circleCrop()
                        .into(teacherImgProfile);

                Glide.with(this)
                        .load(imgUrl)
                        .circleCrop()
                        .into(editTeacherProfileImage);
            }
        });
    }

    // ----------------- BUTTON LISTENERS -------------------
    private void setupListeners() {

        btnMyProfile.setOnClickListener(v -> {
            layoutDashboard.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.VISIBLE);
        });

        btnBackDashboard.setOnClickListener(v -> {
            layoutProfile.setVisibility(View.GONE);
            layoutDashboard.setVisibility(View.VISIBLE);
        });

        btnTeacherChangePhoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        btnSaveProfile.setOnClickListener(v -> {
            if (selectedImageUri != null)
                uploadImageThenSave();
            else
                saveProfile(null);
        });

        btnViewAllAppointments.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, AllAppointmentsActivity.class)));

        btnViewSurveys.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, TeacherSurveyListActivity.class)));

        btnLogoutTeacher.setOnClickListener(v -> {
            FirebaseUtils.auth().signOut();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
        });
    }


    // ----------------- CLOUDINARY UPLOAD -------------------
    private void uploadImageThenSave() {

        new Thread(() -> {
            try {
                String url = uploadToCloudinary(selectedImageUri);

                // ⭐ FIX: Run saveProfile() on main UI thread
                runOnUiThread(() -> saveProfile(url));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }


    private String uploadToCloudinary(Uri uri) throws IOException, JSONException {

        String boundary = "----" + System.currentTimeMillis();
        URL url = new URL(CloudinaryConfig.UPLOAD_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
        out.writeBytes(CloudinaryConfig.UPLOAD_PRESET + "\r\n");

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n\r\n");

        InputStream is = getContentResolver().openInputStream(uri);

        byte[] buffer = new byte[4096];
        int len;

        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.writeBytes("\r\n--" + boundary + "--\r\n");
        out.flush();
        out.close();
        is.close();

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

            String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
            JSONObject json = new JSONObject(response);

            return json.getString("secure_url");
        }

        return null;
    }

    // ----------------- SAVE PROFILE -------------------
    private void saveProfile(String imageUrl) {

        if (imageUrl != null) {
            userRef.update("profileImageUrl", imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(teacherImgProfile);

            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(editTeacherProfileImage);
        }

        userRef.update(
                "name", etName.getText().toString().trim(),
                "subject", etSubject.getText().toString().trim(),
                "phone", etPhone.getText().toString().trim(),
                "qualification", etQualification.getText().toString().trim(),
                "experience", etExperience.getText().toString().trim(),
                "description", etDescription.getText().toString().trim()
        );

        runOnUiThread(() -> {
            tvSaveStatus.setText("Profile Updated ✔");
            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
        });
    }
}
