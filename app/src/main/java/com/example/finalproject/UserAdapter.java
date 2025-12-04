package com.example.finalproject;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    ArrayList<User> list;
    String mode = "ALL";

    public UserAdapter(ArrayList<User> list) {
        this.list = list;
    }

    public void setMode(String m) {
        this.mode = m;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder h, int pos) {
        User user = list.get(pos);

        // BASIC DETAILS
        h.tvName.setText(user.name);
        h.tvEmail.setText(user.email);
        h.tvRole.setText("Role: " + user.role);

        // ====================================================================================
        // 1️⃣ HIDE BUTTONS IF IN FILTER MODE (View Students or View Teachers)
        // ====================================================================================
        if (!mode.equals("ALL")) {
            h.btnChangeRole.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.GONE);
        } else {
            h.btnChangeRole.setVisibility(View.VISIBLE);
            h.btnDelete.setVisibility(View.VISIBLE);
        }

        // ====================================================================================
        // 2️⃣ SHOW APPROVED/DECLINED COUNTS FOR TEACHERS ONLY
        // ====================================================================================
        if (user.role.equals("Teacher")) {
            h.tvApproved.setVisibility(View.VISIBLE);
            h.tvDeclined.setVisibility(View.VISIBLE);

            loadCounts(h, user.uid);
        } else {
            h.tvApproved.setVisibility(View.GONE);
            h.tvDeclined.setVisibility(View.GONE);
        }

        // ====================================================================================
        // 3️⃣ CHANGE ROLE
        // ====================================================================================
        h.btnChangeRole.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setTitle("Select New Role");

            String[] roles = {"Student", "Teacher"};

            dialog.setItems(roles, (d, which) -> {
                String newRole = roles[which];

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .update("role", newRole)
                        .addOnSuccessListener(a -> {
                            user.role = newRole;
                            notifyItemChanged(pos);
                            Toast.makeText(v.getContext(),
                                    "Role changed to " + newRole,
                                    Toast.LENGTH_SHORT).show();
                        });
            });

            dialog.show();
        });

        // ====================================================================================
        // 4️⃣ DELETE USER (FIRESTORE ONLY — NOT AUTH)
        // ====================================================================================
        h.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
            alert.setTitle("Delete User");
            alert.setMessage("Are you sure?");
            alert.setPositiveButton("Delete", (d, w) -> {

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .delete();

                list.remove(pos);
                notifyItemRemoved(pos);

                Toast.makeText(v.getContext(), "User deleted!", Toast.LENGTH_SHORT).show();
            });

            alert.setNegativeButton("Cancel", null);
            alert.show();
        });
    }

    // ====================================================================================
    // 🔥 COUNT APPROVED / DECLINED APPOINTMENTS
    // ====================================================================================
    private void loadCounts(UserHolder h, String uid) {
        FirebaseFirestore.getInstance().collection("appointments")
                .whereEqualTo("teacherId", uid)
                .get()
                .addOnSuccessListener(result -> {

                    int approved = 0, declined = 0;

                    for (var doc : result) {
                        String status = doc.getString("status");

                        if (status == null) continue;

                        if (status.equals("Approved")) approved++;
                        if (status.equals("Declined")) declined++;
                    }

                    h.tvApproved.setText("Approved: " + approved);
                    h.tvDeclined.setText("Declined: " + declined);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // HOLDER
    static class UserHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvRole, tvApproved, tvDeclined;
        Button btnChangeRole, btnDelete;

        public UserHolder(@NonNull View v) {
            super(v);

            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvRole = v.findViewById(R.id.tvRole);

            tvApproved = v.findViewById(R.id.tvApproved);
            tvDeclined = v.findViewById(R.id.tvDeclined);

            btnChangeRole = v.findViewById(R.id.btnChangeRole);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
