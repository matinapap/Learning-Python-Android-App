package com.example.dbinject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.dbinject.AdaptiveLearning;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private String username;
    private String selectedLessonId;
    private Spinner quizSpinner;
    private TextView welcomeTextView;
    private String[] quizOptions = {"Διάλεξε κουίζ","Quiz 1", "Quiz 2", "Quiz 3", "Quiz 4", "Quiz 5", "Τελικό Quiz"};
    private boolean[] quizLockedStatus = new boolean[quizOptions.length];

    // Αρχικά όλα κλειδωμένα
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private int totalQuizzes = quizOptions.length;

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = database.getReference("users").child(currentUser.getUid()).child("statistics");
            loadUserProgress();
        } else {
            setupQuizSpinner();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ImageView infoIcon = findViewById(R.id.infoIcon);
        infoIcon.setOnClickListener(v -> {
            showInfoPopup();
        });

        welcomeTextView = findViewById(R.id.welcomeTextView);

        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            username = intent.getStringExtra("USERNAME");
            Toast.makeText(this, "Καλώς ήρθατε, " + username + "!", Toast.LENGTH_LONG).show();
            welcomeTextView.setText("Καλωσορίσατε, "+username);
        } else if (currentUser != null) {
            username = currentUser.getUid(); // Χρήση του UID αν δεν μεταφέρθηκε username
        } else {
            Toast.makeText(this, "Καλώς ήρθατε!", Toast.LENGTH_LONG).show();
        }

        if (currentUser != null) {
            userRef = database.getReference("users").child(currentUser.getUid()).child("statistics");
            loadUserProgress();
        } else {

            setupQuizSpinner();
        }
    }

    private void loadUserProgress() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 1; i < quizOptions.length; i++) {
                    // Πρώτο κουίζ πάντα ξεκλείδωτο
                    if (i == 1) {
                        quizLockedStatus[i] = false;
                    } else {
                        String previousQuizKey = "quiz_" + (i - 1) + "_unlocked";
                        Boolean previousUnlocked = dataSnapshot.child(previousQuizKey).getValue(Boolean.class);
                        quizLockedStatus[i] = (previousUnlocked == null || !previousUnlocked);
                    }
                }

                setupQuizSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Σφάλμα στη φόρτωση προόδου: " + error.getMessage());
            }
        });
    }






    private void setupQuizSpinner() {
        quizSpinner = findViewById(R.id.quizSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getFilteredQuizOptions());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quizSpinner.setAdapter(adapter);

        final boolean[] firstSelection = {true};
        quizSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuiz = (String) parent.getItemAtPosition(position);
                int quizIndex = position + 1; // 1-based index

                if (firstSelection[0]) {
                    firstSelection[0] = false;
                } else {
                    if (!quizLockedStatus[position]) {
                        Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
                        intent.putExtra("QUIZ_ID", quizIndex-1);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(HomeActivity.this, "Το " + selectedQuiz + " είναι κλειδωμένο.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Δεν έγινε επιλογή
            }
        });
    }

    public void goLesson(View view) {
        Intent lessonIntent = null;
        if (view.getId() == R.id.lesson1) {
            selectedLessonId = "Ενότητα 1";
            lessonIntent = new Intent(HomeActivity.this, LessonPatternActivity.class);
        } else if (view.getId() == R.id.lesson2) {
            selectedLessonId = "Ενότητα 2";
            lessonIntent = new Intent(HomeActivity.this, LessonPatternActivity.class);
        } else if (view.getId() == R.id.lesson3) {
            selectedLessonId = "Ενότητα 3";
            lessonIntent = new Intent(HomeActivity.this, LessonPatternActivity.class);
        } else if (view.getId() == R.id.lesson4) {
            selectedLessonId = "Ενότητα 4";
            lessonIntent = new Intent(HomeActivity.this, LessonPatternActivity.class);
        } else if (view.getId() == R.id.lesson5) {
            selectedLessonId = "Ενότητα 5";
            lessonIntent = new Intent(HomeActivity.this, LessonPatternActivity.class);
        }

        if (lessonIntent != null) {
            Toast.makeText(this, "Επιλέξατε το μάθημα με ID: " + selectedLessonId, Toast.LENGTH_SHORT).show();
            lessonIntent.putExtra("USERNAME", username);
            lessonIntent.putExtra("LESSON_ID", selectedLessonId);
            startActivity(lessonIntent);
        }
    }

    private List<String> getFilteredQuizOptions() {
        List<String> filteredOptions = new ArrayList<>();
        for (int i = 0; i < quizOptions.length; i++) {
            filteredOptions.add(quizOptions[i] + (quizLockedStatus[i] ? " (Κλειδωμένο)" : ""));
        }
        return filteredOptions;
    }

    public void goToProfile(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra("USERNAME", username);
            startActivity(profileIntent);
        } else {
            Toast.makeText(this, "Δεν είναι συνδεδεμένος χρήστης.", Toast.LENGTH_SHORT).show();
        }
    }

//Προσαρμοσμένη Μάθηση
public void goToAdaptiveLearning(View view) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();


                DatabaseReference userStatsRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                .child(userId)
                .child("statistics")
                .child("quiz_statistics")
                .child("last_completed_quiz");

                userStatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long lastCompleted = snapshot.getValue(Long.class);

                        if (lastCompleted != null && lastCompleted == 1) {

                            AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                            adaptiveLearning.AL_Lesson1(userId, HomeActivity.this);

                        } else if (lastCompleted != null && lastCompleted == 2) {

                            AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                            adaptiveLearning.AL_Lesson2(userId, HomeActivity.this);

                        } else if (lastCompleted != null && lastCompleted == 3) {

                            AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                            adaptiveLearning.AL_Lesson3(userId, HomeActivity.this);

                        } else if (lastCompleted != null && lastCompleted == 4) {

                            AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                            adaptiveLearning.AL_Lesson4(userId, HomeActivity.this);

                        } else if (lastCompleted != null && lastCompleted == 5) {

                            AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                            adaptiveLearning.AL_Lesson5(userId, HomeActivity.this);

                        } else if (lastCompleted != null && lastCompleted == 6) {

                                AdaptiveLearning adaptiveLearning = new AdaptiveLearning();
                                adaptiveLearning.AL_Lesson6(userId, HomeActivity.this);

                        } else {

                            Toast.makeText(HomeActivity.this, "Πρέπει να κάνεις το 1ο Quiz!", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}

    private void showInfoPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Οδηγίες Χρήσης")
                .setMessage("📘 Οδηγίες Χρήσης Εφαρμογής\n\nΚαλώς ήρθες στην εκπαιδευτική εφαρμογή Python!\n\nΗ εφαρμογή περιλαμβάνει 5 ενότητες μάθησης. Οι ενότητες περιέχουν :\n\n✔️ Θεωρία: Οπτικοποιημένο και δομημένο περιεχόμενο, με δυνατότητα εικόνων.\n\n✔️ Διαδραστικές Δραστηριότητες:\n  - Drag & Drop (Σειροθέτηση εντολών)\n  - Fill the Blocks (Συμπλήρωση κενών στον κώδικα)\n  - Επιλογή Σωστής Εντολής\n\n✔️ 📝 Quiz: Ένα τεστ αξιολόγησης με ερωτήσεις πολλαπλής επιλογής.\n  - Πρέπει να πετύχεις τουλάχιστον 50% για να ξεκλειδώσεις το επόμενο quiz.\n\n✔️ 🎓 Προσαρμοσμένη Μάθηση:\n  Αν δεν περάσεις ένα quiz, σου προτείνονται στοχευμένα επαναληπτικά μαθήματα και ασκήσεις.\n\n✔️ 👤 Προφίλ Χρήστη:\n  Από την εικόνα χρήστη (πάνω αριστερά), μπορείς να δεις:\n  - Email και username\n  - Μέση επίδοση (%)\n  - Χρόνο εκπαίδευσης (λεπτά)\n  - Τελευταίο μάθημα που ολοκληρώθηκε\n\n⚙️ Χρήση:\n1. Επέλεξε ενότητα για να ξεκινήσεις τη θεωρία και τις δραστηριότητες πατώντας απλά επάνω σε αυτήν.\n2. Κάνε quiz στο τέλος κάθε ενότητας, επιλέγοντάς το από το αρχικό μενού.\n3. Παρακολούθησε την πρόοδό σου μέσω του προφίλ σου.\n4. Χρησιμοποίησε την Προσαρμοσμένη Μάθηση για ενίσχυση σε ό,τι χρειάζεσαι.\n\nΚαλή μελέτη και καλή επιτυχία!")
                .setPositiveButton("ΟΚ", null)
                .show();
    }


}