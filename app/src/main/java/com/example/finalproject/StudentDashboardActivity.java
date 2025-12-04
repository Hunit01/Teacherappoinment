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
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class StudentDashboardActivity extends AppCompatActivity {

    // PAGE 1 (Dashboard)
    LinearLayout layoutDashboard;
    ImageView imgProfile;
    TextView tvWelcome;

    Button btnEditMode, btnSearchTeacher, btnBook, btnMyAppointments, btnMySurveys, btnLogout;

    // PAGE 2 (Edit Profile)
    ScrollView layoutEditProfile;
    ImageView editImgProfile;
    EditText editName, editClass, editPhone;
    Button btnEditChangePhoto, btnSaveEdit, btnBack;

    // Local User
    UserEntity localUser;

    // Image Picker
    Uri selectedImageUri = null;
    ActivityResultLauncher<String> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        initViews();
        initImagePicker();
        loadLocalUser();
        setupListeners();
    }

    // ---------------------------------------------------------
    // INITIALIZE VIEWS
    // ---------------------------------------------------------
    private void initViews() {

        layoutDashboard = findViewById(R.id.layoutDashboard);
        layoutEditProfile = findViewById(R.id.layoutEditProfile);

        imgProfile = findViewById(R.id.imgProfile);
        tvWelcome = findViewById(R.id.tvWelcome);

        btnEditMode = findViewById(R.id.btnEditMode);
        btnSearchTeacher = findViewById(R.id.btnSearchTeacher);
        btnBook = findViewById(R.id.btnBook);
        btnMyAppointments = findViewById(R.id.btnMyAppointments);
        btnMySurveys = findViewById(R.id.btnMySurveys);
        btnLogout = findViewById(R.id.btnLogoutStudent);

        // EDIT PAGE
        editImgProfile = findViewById(R.id.editImgProfile);
        editName = findViewById(R.id.editName);
        editClass = findViewById(R.id.editClass);
        editPhone = findViewById(R.id.editPhone);

        btnEditChangePhoto = findViewById(R.id.btnEditChangePhoto);
        btnSaveEdit = findViewById(R.id.btnSaveEdit);
        btnBack = findViewById(R.id.btnBack);
    }

    // ---------------------------------------------------------
    // IMAGE PICKER
    // ---------------------------------------------------------
    private void initImagePicker() {

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;

                        // Preview in EDIT PAGE (CIRCULAR)
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(editImgProfile);
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // LOAD USER (ROOM)
    // ---------------------------------------------------------
    private void loadLocalUser() {

        new Thread(() -> {
            localUser = AppDatabase.getInstance(this).userDao().getSavedUser();

            runOnUiThread(() -> {
                if (localUser != null) {

                    tvWelcome.setText("Welcome, " + localUser.name);

                    // Dashboard Image (Circular)
                    Glide.with(this)
                            .load(localUser.profileImageUrl)
                            .circleCrop()
                            .into(imgProfile);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------
    // BUTTON LISTENERS
    // ---------------------------------------------------------
    private void setupListeners() {

        btnEditMode.setOnClickListener(v -> showEditPage());

        btnEditChangePhoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        btnBack.setOnClickListener(v -> showDashboard());

        btnSaveEdit.setOnClickListener(v -> {
            if (selectedImageUri != null)
                uploadImageThenSave();
            else
                saveProfile(null);
        });

        btnSearchTeacher.setOnClickListener(
                v -> startActivity(new android.content.Intent(this, SearchTeacherActivity.class)));

        btnBook.setOnClickListener(
                v -> startActivity(new android.content.Intent(this, SearchTeacherActivity.class)));

        btnMyAppointments.setOnClickListener(
                v -> startActivity(new android.content.Intent(this, ViewAppointmentsActivity.class)));

        btnMySurveys.setOnClickListener(
                v -> startActivity(new android.content.Intent(this, StudentSurveyListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseUtils.auth().signOut();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ---------------------------------------------------------
    // SHOW EDIT PROFILE PAGE
    // ---------------------------------------------------------
    private void showEditPage() {

        layoutDashboard.setVisibility(View.GONE);
        layoutEditProfile.setVisibility(View.VISIBLE);

        editName.setText(localUser.name);
        editClass.setText(localUser.studentClass);
        editPhone.setText(localUser.phone);

        // Circular in Edit Page
        Glide.with(this)
                .load(localUser.profileImageUrl)
                .circleCrop()
                .into(editImgProfile);
    }

    // ---------------------------------------------------------
    // SHOW DASHBOARD
    // ---------------------------------------------------------
    private void showDashboard() {
        layoutDashboard.setVisibility(View.VISIBLE);
        layoutEditProfile.setVisibility(View.GONE);
        loadLocalUser();
    }

    // ---------------------------------------------------------
    // CLOUDINARY UPLOAD
    // ---------------------------------------------------------
    private void uploadImageThenSave() {

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String url = uploadToCloudinary(selectedImageUri);

                // ⭐ FIX: Run saveProfile() on MAIN THREAD
                runOnUiThread(() -> saveProfile(url));

            } catch (Exception e) {
                e.printStackTrace();
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
        conn.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=" + boundary
        );

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        // upload preset
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
        out.writeBytes(CloudinaryConfig.UPLOAD_PRESET + "\r\n");

        // image
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n\r\n");

        InputStream is = getContentResolver().openInputStream(uri);
        byte[] buffer = new byte[4096];
        int len;

        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.writeBytes("\r\n");
        out.writeBytes("--" + boundary + "--\r\n");
        out.flush();
        out.close();
        is.close();

        if (conn.getResponseCode() == 200) {
            String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
            JSONObject json = new JSONObject(response);
            return json.getString("secure_url");
        }

        return null;
    }

    // ---------------------------------------------------------
    // SAVE PROFILE → FIRESTORE + ROOM
    // ---------------------------------------------------------
    private void saveProfile(String imageUrl) {

        String name = editName.getText().toString();
        String cls = editClass.getText().toString();
        String phone = editPhone.getText().toString();

        if (imageUrl != null)
            localUser.profileImageUrl = imageUrl;

        localUser.name = name;
        localUser.studentClass = cls;
        localUser.phone = phone;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(localUser.uid)
                .update(
                        "name", name,
                        "studentClass", cls,
                        "phone", phone,
                        "profileImageUrl", localUser.profileImageUrl
                );

        new Thread(() ->
                AppDatabase.getInstance(this).userDao().saveUser(localUser)
        ).start();

        runOnUiThread(() -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            showDashboard();
        });
    }
}
