package com.example.finalproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

    public static String uid() {
        return FirebaseAuth.getInstance().getUid();
    }

    // 🔥 Use displayName if available, otherwise part before @ from email
    public static String username() {
        FirebaseUser user = auth().getCurrentUser();
        if (user == null) return "Unknown User";

        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }

        String email = user.getEmail();
        if (email == null) return "Unknown User";

        String beforeAt = email.split("@")[0];
        // Make first letter uppercase
        if (beforeAt.length() > 1) {
            return beforeAt.substring(0, 1).toUpperCase() + beforeAt.substring(1);
        }
        return beforeAt;
    }
}
