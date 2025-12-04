package com.example.finalproject;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    Context context;
    ArrayList<Appointment> list;
    String currentUserId;

    ActivityResultLauncher<String> uploadPdfLauncher;
    Uri selectedPdfUri;

    String selectedAppointmentId = "";
    String notesMessageTemp = "";

    public AppointmentAdapter(Context context, ArrayList<Appointment> list) {
        this.context = context;
        this.list = list;
        this.currentUserId = FirebaseUtils.uid();

        uploadPdfLauncher = ((AppCompatActivity) context)
                .registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                selectedPdfUri = uri;
                                uploadPdfToCloud(uri, notesMessageTemp);
                            }
                        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Appointment a = list.get(pos);
        boolean isStudent = currentUserId.equals(a.studentId);
        boolean isTeacher = currentUserId.equals(a.teacherId);

        h.hideAll();

        h.name.setText(isStudent ? "Teacher: " + a.teacherName : "Student: " + a.studentName);
        h.date.setText("Date: " + a.date);
        h.time.setText("Time: " + a.timeSlot);
        h.msg.setText("Message: " + a.message);
        h.status.setText("Status: " + a.status);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // ================================
        // STUDENT VIEW (Download, Cancel, Reschedule)
        // ================================
        if (isStudent) {

            // ---- DOWNLOAD NOTES ----
            if (a.notesUrl != null && !a.notesUrl.isEmpty()) {
                h.btnViewNotes.setVisibility(View.VISIBLE);
                h.btnViewNotes.setText("📄 Download Notes");

                h.btnViewNotes.setOnClickListener(v -> {
                    String cleanFile = buildFileName(a);
                    downloadPdfUsingDownloadManager(a.notesUrl, cleanFile);
                });
            }

            // ---- FILL SURVEY ----
            if (a.surveyPending && !a.surveyCompleted) {
                h.btnFillSurvey.setVisibility(View.VISIBLE);
                h.btnFillSurvey.setOnClickListener(v -> {
                    Intent i = new Intent(context, SurveyActivity.class);
                    i.putExtra("appointmentId", a.appointmentId);
                    i.putExtra("teacherId", a.teacherId);
                    i.putExtra("teacherName", a.teacherName);
                    i.putExtra("studentName", a.studentName);
                    context.startActivity(i);
                });
            }

            // ======================================
            // 🔥 CANCEL ALLOWED IN Pending + Approved
            // ======================================
            if (a.status.equals("Pending") || a.status.equals("Approved")) {
                h.btnCancel.setVisibility(View.VISIBLE);

                h.btnCancel.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Cancel Appointment")
                            .setMessage("Are you sure you want to cancel this appointment?")
                            .setPositiveButton("Yes", (d, w) -> {

                                db.collection("appointments")
                                        .document(a.appointmentId)
                                        .update(
                                                "status", "Cancelled",
                                                "cancelledBy", "Student"
                                        );

                                a.status = "Cancelled";
                                notifyItemChanged(h.getAdapterPosition());
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }


            // ======================================
            // 🔥 RESCHEDULE ALLOWED IN Pending + Approved
            // ======================================
            if (a.status.equals("Pending") || a.status.equals("Approved")) {
                h.btnReschedule.setVisibility(View.VISIBLE);

                h.btnReschedule.setOnClickListener(v -> {

                    // When rescheduled → status becomes Pending again
                    db.collection("appointments")
                            .document(a.appointmentId)
                            .update(
                                    "status", "Pending",
                                    "rescheduled", true
                            );

                    a.status = "Pending";  // Update UI immediately
                    notifyItemChanged(h.getAdapterPosition());

                    // Go to reschedule UI
                    Intent i = new Intent(context, RescheduleActivity.class);
                    i.putExtra("appointmentId", a.appointmentId);
                    context.startActivity(i);
                });
            }

        }


        // ================================
        // TEACHER VIEW
        // ================================
        if (isTeacher) {

            // APPROVE / DECLINE IN PENDING
            if (a.status.equals("Pending")) {
                h.btnApprove.setVisibility(View.VISIBLE);
                h.btnDecline.setVisibility(View.VISIBLE);

                h.btnApprove.setOnClickListener(v -> {
                    db.collection("appointments")
                            .document(a.appointmentId)
                            .update("status", "Approved");

                    a.status = "Approved";
                    notifyItemChanged(h.getAdapterPosition());
                });

                h.btnDecline.setOnClickListener(v -> showDeclineReasonDialog(a, h));
            }

            // MARK COMPLETED
            if (a.status.equals("Approved") && !a.completed) {
                h.btnCompleted.setVisibility(View.VISIBLE);

                h.btnCompleted.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Complete Appointment")
                            .setMessage("Mark this appointment as completed?")
                            .setPositiveButton("Yes", (d, w) -> {
                                db.collection("appointments")
                                        .document(a.appointmentId)
                                        .update(
                                                "status", "Completed",
                                                "completed", true
                                        );

                                a.status = "Completed";
                                a.completed = true;
                                notifyItemChanged(h.getAdapterPosition());
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            // SEND SURVEY AFTER COMPLETION
            if (a.completed && !a.surveyPending && !a.surveyCompleted) {
                h.btnSendSurvey.setVisibility(View.VISIBLE);

                h.btnSendSurvey.setOnClickListener(v ->
                        db.collection("appointments")
                                .document(a.appointmentId)
                                .update("surveyPending", true)
                );
            }

            // UPLOAD NOTES
            if (a.completed) {
                h.btnUploadNotes.setVisibility(View.VISIBLE);
                h.btnUploadNotes.setText(
                        (a.notesUrl == null || a.notesUrl.isEmpty())
                                ? "📄 Upload Notes"
                                : "🔄 Replace Notes"
                );

                h.btnUploadNotes.setOnClickListener(v -> showUploadNotesDialog(a));

                // VIEW NOTES
                if (a.notesUrl != null && !a.notesUrl.isEmpty()) {
                    h.btnViewNotes.setVisibility(View.VISIBLE);
                    h.btnViewNotes.setText("📄 View Notes");

                    h.btnViewNotes.setOnClickListener(v -> {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(a.notesUrl));
                            context.startActivity(i);
                        } catch (Exception e) {
                            Toast.makeText(context, "Cannot open PDF", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }


    // FILE NAME FORMAT
    private String buildFileName(Appointment a) {
        return "Notes_" + a.teacherName + "_" + a.date.replace("/", "-") + ".pdf";
    }


    // DOWNLOAD MANAGER
    private void downloadPdfUsingDownloadManager(String url, String fileName) {
        try {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            if (dm == null) {
                Toast.makeText(context, "Download Manager unavailable", Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri = Uri.parse(url);

            DownloadManager.Request req = new DownloadManager.Request(uri);
            req.setTitle(fileName);
            req.setDescription("Downloading notes...");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            dm.enqueue(req);

            Toast.makeText(context, "Download started — check Downloads folder", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    // DECLINE DIALOG
    private void showDeclineReasonDialog(Appointment a, ViewHolder h) {

        EditText input = new EditText(context);
        input.setHint("Enter decline reason");

        new AlertDialog.Builder(context)
                .setTitle("Decline Appointment")
                .setView(input)
                .setPositiveButton("Submit", (d, w) -> {

                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) reason = "Not available";

                    FirebaseFirestore.getInstance()
                            .collection("appointments")
                            .document(a.appointmentId)
                            .update(
                                    "status", "Declined",
                                    "declineReason", reason
                            );

                    a.status = "Declined";
                    notifyItemChanged(h.getAdapterPosition());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // UPLOAD NOTES POPUP
    private void showUploadNotesDialog(Appointment a) {

        EditText input = new EditText(context);
        input.setHint("Enter notes message");

        new AlertDialog.Builder(context)
                .setTitle("Upload Notes")
                .setView(input)
                .setPositiveButton("Choose PDF", (d, w) -> {

                    notesMessageTemp = input.getText().toString().trim();
                    if (notesMessageTemp.isEmpty()) notesMessageTemp = "Notes uploaded";

                    selectedAppointmentId = a.appointmentId;

                    uploadPdfLauncher.launch("application/pdf");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // UPLOAD TO CLOUDINARY
    private void uploadPdfToCloud(Uri uri, String notesMessage) {

        new Thread(() -> {
            try {

                String pdfUrl = CloudinaryUploader.uploadUriStreaming(
                        context.getContentResolver(),
                        uri,
                        "notes_" + System.currentTimeMillis() + ".pdf"
                );

                FirebaseFirestore.getInstance()
                        .collection("appointments")
                        .document(selectedAppointmentId)
                        .update(
                                "notesUrl", pdfUrl,
                                "notesMessage", notesMessage,
                                "notesUploadedAt", System.currentTimeMillis()
                        );

                ((AppCompatActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Notes uploaded ✔", Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    // VIEW HOLDER
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, date, time, msg, status;
        Button btnApprove, btnDecline, btnCancel, btnReschedule,
                btnCompleted, btnFillSurvey, btnSendSurvey,
                btnViewNotes, btnUploadNotes;

        ViewHolder(@NonNull View v) {
            super(v);

            name = v.findViewById(R.id.tvTeacherName);
            date = v.findViewById(R.id.tvDate);
            time = v.findViewById(R.id.tvTime);
            msg = v.findViewById(R.id.tvMessage);
            status = v.findViewById(R.id.tvStatus);

            btnApprove = v.findViewById(R.id.btnApprove);
            btnDecline = v.findViewById(R.id.btnDecline);
            btnCancel = v.findViewById(R.id.btnCancel);
            btnReschedule = v.findViewById(R.id.btnReschedule);
            btnCompleted = v.findViewById(R.id.btnCompleted);

            btnFillSurvey = v.findViewById(R.id.btnFillSurvey);
            btnSendSurvey = v.findViewById(R.id.btnSendSurvey);

            btnViewNotes = v.findViewById(R.id.btnViewNotes);
            btnUploadNotes = v.findViewById(R.id.btnUploadNotes);
        }

        void hideAll() {
            btnApprove.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnReschedule.setVisibility(View.GONE);
            btnCompleted.setVisibility(View.GONE);
            btnFillSurvey.setVisibility(View.GONE);
            btnSendSurvey.setVisibility(View.GONE);
            btnViewNotes.setVisibility(View.GONE);
            btnUploadNotes.setVisibility(View.GONE);
        }
    }
}
