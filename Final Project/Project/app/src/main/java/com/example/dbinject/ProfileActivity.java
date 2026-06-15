package com.example.dbinject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView registrationDateTextView;
    private TextView averageScoreTextView;
    private TextView reachedQuizTextView;
    private TextView totalTrainingTimeTextView;
    private TextView lastCompletedCourseTextView;
    private ImageView profileImageView;
    private Button logoutButton;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private TextView userLearningStyleTextView;
    private TextView lastLoginTextView;

    private String loggedInUsername;
    private String loggedInUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);
        registrationDateTextView = findViewById(R.id.registration_date);
        averageScoreTextView = findViewById(R.id.average_score);
        reachedQuizTextView = findViewById(R.id.reached_quiz);
        totalTrainingTimeTextView = findViewById(R.id.total_training_time);
        lastCompletedCourseTextView = findViewById(R.id.last_completed_course);
        logoutButton = findViewById(R.id.logoutButton);
        userLearningStyleTextView = findViewById(R.id.user_learning_style);

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Αποσύνδεση του χρήστη από το Firebase Auth
            Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish(); // Κλείσιμο του ProfileActivity για να μην μπορεί ο χρήστης να επιστρέψει με το back button
        });
        // Λήψη του username που μεταφέρθηκε από το Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            loggedInUsername = intent.getStringExtra("USERNAME");
            // Λήψη του user_id από το FirebaseAuth
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                loggedInUserId = currentUser.getUid();
                // Ανάκτηση δεδομένων χρήστη από τη Firebase
                loadUserData();
            } else {
                Toast.makeText(this, "Δεν είναι συνδεδεμένος χρήστης.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Δεν βρέθηκε username.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        if (loggedInUserId != null) {
            DatabaseReference userRef = usersRef.child(loggedInUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String learningStyle = dataSnapshot.child("learning_style").getValue(String.class);
                    String lastLogin = dataSnapshot.child("last_login").getValue(String.class);

                    if (learningStyle != null) {
                        userLearningStyleTextView.setText("Στυλ Μάθησης: " + learningStyle);
                    }

                    if (lastLogin != null) {
                        lastLoginTextView.setText("Τελευταία Σύνδεση: " + lastLogin);
                    }

                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String registrationDate = dataSnapshot.child("registration_date").getValue(String.class);

                        if (username != null) {
                            userNameTextView.setText(username);
                        }
                        if (email != null) {
                            userEmailTextView.setText(email);
                        }
                        if (registrationDate != null) {
                            registrationDateTextView.setText("Ημερομηνία Εγγραφής: " + registrationDate);
                        }
                        if (lastLogin != null) {
                            // Μπορείτε να μορφοποιήσετε την ημερομηνία αν χρειάζεται
                            // lastLoginTextView.setText("Τελευταία Σύνδεση: " + lastLogin);
                        }

                        DataSnapshot statisticsSnapshot = dataSnapshot.child("statistics");
                        if (statisticsSnapshot.exists()) {
                           // String avgScore = statisticsSnapshot.child("avg_score").getValue(String.class);
                            Long totalTrainingTime = statisticsSnapshot.child("total_training_time").getValue(Long.class); // Υποθέτουμε ότι αποθηκεύετε τον χρόνο σε λεπτά
                            Long reachedQuiz = 0L;
                            for (int i = 1; i <= 5; i++) {
                                Long correctAnswers = statisticsSnapshot.child("correct_answers_quiz_" + i).getValue(Long.class);
                                Long totalQuestions = statisticsSnapshot.child("total_questions_quiz_" + i).getValue(Long.class);
                                if (totalQuestions != null && correctAnswers != null && totalQuestions > 0 && (double) correctAnswers / totalQuestions >= 0.5) {
                                    reachedQuiz = (long) i;
                                } else if (i == 1 && (totalQuestions == null || totalQuestions == 0)) {
                                    reachedQuiz = 0L; // Ή 1 αν θεωρείτε το πρώτο quiz πάντα διαθέσιμο
                                } else {
                                    break; // Σταματάμε στο πρώτο quiz που δεν έχει περαστεί
                                }
                            }
                            Long certificates = statisticsSnapshot.child("certificates_earned").getValue(Long.class);
                            String lastCourse = statisticsSnapshot.child("last_completed_course").getValue(String.class);

                            // Υπολογισμός Μ.Ο. δυναμικά από όλα τα quiz
                            long totalCorrect = 0;
                            long totalQuestions = 0;

                            for (DataSnapshot stat : statisticsSnapshot.getChildren()) {
                                String key = stat.getKey();
                                if (key != null && key.startsWith("correct_answers_quiz_")) {
                                    Long correct = stat.getValue(Long.class);
                                    if (correct != null) totalCorrect += correct;
                                } else if (key != null && key.startsWith("total_questions_quiz_")) {
                                    Long total = stat.getValue(Long.class);
                                    if (total != null) totalQuestions += total;
                                }
                            }

                            if (totalQuestions > 0) {
                                double avg = ((double) totalCorrect / totalQuestions) * 100;
                                averageScoreTextView.setText(String.format(Locale.getDefault(), "%.0f%%", avg));
                            } else {
                                averageScoreTextView.setText("-");
                            }

                            reachedQuizTextView.setText(String.valueOf(reachedQuiz));
                            if (totalTrainingTime != null) {
                                totalTrainingTimeTextView.setText(String.valueOf(totalTrainingTime));
                            } else {
                                totalTrainingTimeTextView.setText("0");
                            }
                            if (lastCourse != null) {
                                lastCompletedCourseTextView.setText(lastCourse);
                            } else {
                                lastCompletedCourseTextView.setText("-");
                            }
                        } else {
                            averageScoreTextView.setText("-");
                            reachedQuizTextView.setText("0");
                            totalTrainingTimeTextView.setText("0");
                            lastCompletedCourseTextView.setText("-");
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Δεν βρέθηκαν στοιχεία για τον χρήστη.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "Σφάλμα ανάκτησης δεδομένων χρήστη: " + databaseError.getMessage());
                    Toast.makeText(ProfileActivity.this, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Το loggedInUserId είναι null.", Toast.LENGTH_SHORT).show();
        }
    }
}