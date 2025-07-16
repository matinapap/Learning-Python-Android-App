package com.example.dbinject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    private long quizStartTime;
    private String lessonNumber;
    private String username;
    private TextView quizTitleTextView;
    private TextView timerTextView;
    private ExpandableListView questionsListView;
    private String selectedQuizId;

    private DatabaseReference userRef;
    private FirebaseDatabase database;
    private DatabaseReference quizRef;
    private List<String> questionHeaders = new ArrayList<>();
    private HashMap<String, List<String>> questionOptions = new HashMap<>();
    private HashMap<String, String> correctAnswers = new HashMap<>();
    private QuizExpandableListAdapter adapter;
    private CountDownTimer timer;
    private long timeLeftInMillis = 180000; // 3 λεπτά για να κάνει το quiz αυτό είναι το όριο σε ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizTitleTextView = findViewById(R.id.quizTitleTextView);
        timerTextView = findViewById(R.id.timerTextView);
        questionsListView = findViewById(R.id.questionsExpandableListView);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("QUIZ_ID")) {
                selectedQuizId = "quiz_" + intent.getIntExtra("QUIZ_ID", -1); // Υποθέτω ότι το Quiz ID είναι 1-based
                loadQuizData();
                startTimer();
                quizStartTime = System.currentTimeMillis();
            } else {
                Toast.makeText(this, "Δεν βρέθηκε ID Quiz.", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                // Μπορείτε να χρησιμοποιήσετε το username εδώ αν χρειάζεται
                Log.d("QuizActivity", "Username που ελήφθη: " + username);
            }
        } else {
            Toast.makeText(this, "Δεν ελήφθη Intent.", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.submitButton).setOnClickListener(v -> submitAnswers());
    }

    private void loadQuizData() {
        database = FirebaseDatabase.getInstance();
        lessonNumber = selectedQuizId.substring("quiz_".length());
        String lessonId = "lesson_" + lessonNumber;
        quizRef = database.getReference("lessons").child(lessonId).child("quizzes");

        Log.d("QuizData", "loadQuizData() called for quizId: " + selectedQuizId);

        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    quizTitleTextView.setText("Quiz " + selectedQuizId.replace("quiz_", ""));
                    questionHeaders.clear();
                    questionOptions.clear();
                    correctAnswers.clear();

                    Log.d("QuizData", "Quiz data found for: " + selectedQuizId);

                    for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                        // Το κάθε παιδί του dataSnapshot είναι μια ερώτηση (με κλειδιά 0, 1, 2, ...)
                        String questionText = questionSnapshot.child("question").getValue(String.class);
                        Log.d("QuizData", "Λήψη ερώτησης από Firebase: " + questionText); // <--- ΠΡΟΣΘΗΚΗ LOG

                        if (questionText != null) {
                            questionHeaders.add(questionText);
                            List<String> optionsList = new ArrayList<>();
                            String correctAnswer = "";
                            DataSnapshot optionsSnapshot = questionSnapshot.child("options");
                            if (optionsSnapshot.exists()) {
                                for (DataSnapshot optionSnapshot : optionsSnapshot.getChildren()) {
                                    String optionText = optionSnapshot.child("text").getValue(String.class);
                                    Boolean isCorrect = optionSnapshot.child("is_correct").getValue(Boolean.class);
                                    Log.d("QuizData", "  Επιλογή: " + optionSnapshot.getKey() + ", Κείμενο: " + optionText + ", Σωστή: " + isCorrect);
                                    if (optionText != null) {
                                        optionsList.add(optionText);
                                        if (isCorrect != null && isCorrect) {
                                            correctAnswer = optionText;
                                        }
                                    }
                                }
                            }
                            questionOptions.put(questionText, optionsList);
                            correctAnswers.put(questionText, correctAnswer);
                        }
                    }
                    Log.d("QuizData", "Μέγεθος questionHeaders πριν setAdapter: " + questionHeaders.size());
                    Log.d("QuizData", "Μέγεθος questionOptions πριν setAdapter: " + questionOptions.size());

                    adapter = new QuizExpandableListAdapter(QuizActivity.this, questionHeaders, questionOptions);
                    questionsListView.setAdapter(adapter);

                    // Επέκταση της πρώτης ερώτησης μετά τη σύνδεση του adapter
                    if (questionHeaders.size() > 0) {
                        questionsListView.expandGroup(0);
                    }

                } else {
                    Toast.makeText(QuizActivity.this, "Δεν βρέθηκαν δεδομένα για το Quiz.", Toast.LENGTH_SHORT).show();
                    Log.w("QuizData", "Δεν βρέθηκαν δεδομένα για το Quiz: " + selectedQuizId);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Σφάλμα φόρτωσης δεδομένων Quiz: " + databaseError.getMessage());
                Toast.makeText(QuizActivity.this, "Σφάλμα φόρτωσης Quiz.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Χρόνος Τέλειωσε!");
                submitAnswers();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText("Χρόνος: " + timeLeftFormatted);
    }

    private void submitAnswers() {
        // Δηλώνουμε τη μεταβλητή ως κανονική (όχι final)
        int correctCount = 0;

        // Υπολογισμός των σωστών απαντήσεων
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            String question = (String) adapter.getGroup(i);
            String selectedAnswer = adapter.getSelectedAnswer(i);
            if (selectedAnswer != null && selectedAnswer.equals(correctAnswers.get(question))) {
                correctCount++;
            }
        }

        // Διακοπή του χρονοδιακόπτη
        timer.cancel();

        // Υπολογισμός της διάρκειας του quiz σε λεπτά
        long quizEndTime = System.currentTimeMillis();
        long quizDurationMillis = quizEndTime - quizStartTime;
        long quizDurationMinutes = quizDurationMillis / (1000 * 60);

        if (adapter.getGroupCount() > 0) {
            double percentage = (double) correctCount / adapter.getGroupCount();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                DatabaseReference quizStatsRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("statistics")
                        .child("quiz_statistics");

                quizStatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lessonNumber = selectedQuizId.substring("quiz_".length());

                        // Ενημέρωση του τελευταίου ολοκληρωμένου quiz
                        quizStatsRef.child("last_completed_quiz").setValue(lessonNumber);

                        // Ενημέρωση επιτυχίας quiz
                        quizStatsRef.child("quiz_success").setValue(percentage >= 0.5);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Σφάλμα στη λήψη των quiz στατιστικών: " + error.getMessage());
                    }
                });
            }

        } else {
            Log.w("Result", "Μηδενικός αριθμός ερωτήσεων.");
        }


        // Εμφάνιση Toast με το σκορ
        Toast.makeText(this, "Το σκορ σου είναι: " + correctCount + " από " + adapter.getGroupCount(), Toast.LENGTH_LONG).show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(userId).child("statistics");

            String quizIdForDatabase = "quiz_" + selectedQuizId.replace("quiz_", "");
            final int totalQuestions = adapter.getGroupCount();

            // Δηλώνουμε τελικές μεταβλητές για χρήση μέσα στον Listener
            final int correctCountFinal = correctCount;
            final int totalQuestionsFinal = totalQuestions;

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Ενημέρωση του συνολικού χρόνου εκπαίδευσης
                    Long totalTrainingTime = dataSnapshot.child("total_training_time").getValue(Long.class);
                    long newTotalTrainingTime = (totalTrainingTime == null ? 0 : totalTrainingTime) + quizDurationMinutes;
                    userRef.child("total_training_time").setValue(newTotalTrainingTime);

                    // Ενημέρωση του highest_reached_quiz
                    int currentQuizNumber = Integer.parseInt(selectedQuizId.replace("quiz_", ""));
                    userRef.child("highest_reached_quiz").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Long highestReached = snapshot.getValue(Long.class);
                            if (highestReached == null || currentQuizNumber > highestReached) {
                                userRef.child("highest_reached_quiz").setValue((long) currentQuizNumber);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Σφάλμα κατά την ενημέρωση του highest_reached_quiz: " + error.getMessage());
                        }
                    });

                    // 🔁 Βήμα 1: Ενημέρωση του quiz που μόλις έγινε
                    userRef.child("correct_answers_" + quizIdForDatabase).setValue((long) correctCountFinal);
                    userRef.child("total_questions_" + quizIdForDatabase).setValue((long) totalQuestionsFinal);

                    // 🔁 Βήμα 2: Υπολογισμός Μ.Ο. από όλα τα quizzes
                    long totalCorrect = 0;
                    long totalQuestions = 0;

                    for (DataSnapshot stat : dataSnapshot.getChildren()) {
                        String key = stat.getKey();
                        if (key != null && key.startsWith("correct_answers_quiz_")) {
                            Long correct = stat.getValue(Long.class);
                            if (correct != null) {
                                totalCorrect += correct;
                            }
                        } else if (key != null && key.startsWith("total_questions_quiz_")) {
                            Long total = stat.getValue(Long.class);
                            if (total != null) {
                                totalQuestions += total;
                            }
                        }
                    }

                    if (totalQuestions > 0) {
                        double avgScore = ((double) totalCorrect / totalQuestions) * 100;
                        userRef.child("avg_score").setValue(String.format(Locale.getDefault(), "%.0f%%", avgScore));
                    } else {
                        userRef.child("avg_score").setValue("0%");
                    }

                    // Ενημέρωση του τελευταίου ολοκληρωμένου quiz
                    DatabaseReference quizStatsRef = userRef.child("quiz_statistics");
                    quizStatsRef.child("last_completed_quiz").setValue(Integer.parseInt(lessonNumber));

                    // Έλεγχος για το αν το quiz ολοκληρώθηκε με επιτυχία
                    if (correctCountFinal >= 2) {
                        quizStatsRef.child("quiz_success").setValue(true);
                        userRef.child("quiz_" + lessonNumber + "_unlocked").setValue(true);
                    }

                    // Μήνυμα ότι τα αποτελέσματα αποθηκεύτηκαν
                    Toast.makeText(QuizActivity.this, "Τα αποτελέσματα αποθηκεύτηκαν.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Σφάλμα αποθήκευσης αποτελεσμάτων: " + databaseError.getMessage());
                    Toast.makeText(QuizActivity.this, "Σφάλμα κατά την αποθήκευση των αποτελεσμάτων.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Μήνυμα αν δεν είναι συνδεδεμένος χρήστης
            Toast.makeText(QuizActivity.this, "Δεν είναι συνδεδεμένος χρήστης.", Toast.LENGTH_SHORT).show();
            Log.w("QuizActivity", "Προσπάθεια αποθήκευσης αποτελεσμάτων χωρίς συνδεδεμένο χρήστη.");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
        }
    }

    public static class QuizExpandableListAdapter extends BaseExpandableListAdapter {

        private final AppCompatActivity context;
        private final List<String> listDataHeader;
        private final HashMap<String, List<String>> listDataChild;
        private final HashMap<Integer, String> userAnswers = new HashMap<>();

        public QuizExpandableListAdapter(AppCompatActivity context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listChildData;
            Log.d("QuizAdapter", "QuizExpandableListAdapter created. Header size: " + (listDataHeader != null ? listDataHeader.size() : 0) + ", Child size: " + (listChildData != null ? listChildData.size() : 0));
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Log.d("QuizAdapter", "getChild() called for group: " + groupPosition + ", child: " + childPosition);
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size() &&
                    this.listDataChild != null && this.listDataChild.containsKey(this.listDataHeader.get(groupPosition)) &&
                    this.listDataChild.get(this.listDataHeader.get(groupPosition)) != null &&
                    childPosition < this.listDataChild.get(this.listDataHeader.get(groupPosition)).size()) {
                return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
            }
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Log.d("QuizAdapter", "getChildId() called for group: " + groupPosition + ", child: " + childPosition + ", returning: " + childPosition);
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context); // όχι this.context
                convertView = inflater.inflate(R.layout.quiz_list_item, parent, false);
            }

            RadioButton radioButton = convertView.findViewById(R.id.radioButtonOption);

            if (radioButton != null && childText != null) {
                radioButton.setText(childText);

                // Αποσύνδεσε προσωρινά τον listener για αποφυγή trigger κατά το setChecked()
                radioButton.setOnCheckedChangeListener(null);

                String selectedAnswer = userAnswers.get(groupPosition);
                radioButton.setChecked(childText.equals(selectedAnswer));

                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        userAnswers.put(groupPosition, childText);

                        // Αναγκάζει το group να ανανεωθεί ώστε να "ξε-επιλεγούν" τα άλλα κουμπιά
                        notifyDataSetChanged();
                    }
                });
            } else {
                Log.w("QuizAdapter", "radioButton ή childText είναι null.");
            }

            return convertView;
        }


        @Override
        public int getChildrenCount(int groupPosition) {
            int count = 0;
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size() &&
                    this.listDataChild != null && this.listDataChild.containsKey(this.listDataHeader.get(groupPosition))) {
                List<String> children = this.listDataChild.get(this.listDataHeader.get(groupPosition));
                count = (children != null) ? children.size() : 0;
            }
            Log.d("QuizAdapter", "getChildrenCount() called for group " + groupPosition + ", returning: " + count);
            return count;
        }

        @Override
        public Object getGroup(int groupPosition) {
            Log.d("QuizAdapter", "getGroup() called for group: " + groupPosition);
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size()) {
                return this.listDataHeader.get(groupPosition);
            }
            return null;
        }

        @Override
        public int getGroupCount() {
            int size = (this.listDataHeader != null) ? this.listDataHeader.size() : 0;
            Log.d("QuizAdapter", "getGroupCount() called, returning: " + size);
            return size;
        }

        @Override
        public long getGroupId(int groupPosition) {
            Log.d("QuizAdapter", "getGroupId() called for group: " + groupPosition + ", returning: " + groupPosition);
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            Log.d("QuizAdapter", "getGroupView() called for group: " + groupPosition + ", isExpanded: " + isExpanded);
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(this.context);
                convertView = inflater.inflate(R.layout.quiz_list_group, parent, false); // Δημιούργησε αυτό το layout
            }
            TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
            if (lblListHeader != null && headerTitle != null) {
                lblListHeader.setText(headerTitle);
            } else {
                Log.w("QuizAdapter", "lblListHeader ή headerTitle είναι null.");
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public String getSelectedAnswer(int groupPosition) {
            return userAnswers.get(groupPosition);
        }
    }
}