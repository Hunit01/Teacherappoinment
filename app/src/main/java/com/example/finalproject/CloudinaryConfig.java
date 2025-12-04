package com.example.finalproject;

public class CloudinaryConfig {

    public static final String CLOUD_NAME = "dyrv0qqhq";

    // UNSIGNED PRESET (correct)
    public static final String UPLOAD_PRESET = "ml_default";

    // Correct URLs
    public static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public static final String DEFAULT_AVATAR_URL =
            "https://res.cloudinary.com/" + CLOUD_NAME + "/image/upload/v1733210000/default_avatar.png";
}
