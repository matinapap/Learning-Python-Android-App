package com.example.dbinject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ALFillTheBlocksActivity extends AppCompatActivity {

    private final ArrayList<EditText> inputFields = new ArrayList<>();
    private ArrayList<String> correctAnswers;
    private String videoUrl;
    private TextView feedbackTextView;
    private Button watchVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alfill_the_blocks);

        String instructionText = getIntent().getStringExtra("instructions_data");
        String programValue = getIntent().getStringExtra("program_data");
        String titleText = getIntent().getStringExtra("title_data");
        videoUrl = getIntent().getStringExtra("video_url");
        correctAnswers = getIntent().getStringArrayListExtra("answers_data");

        TextView instructionTextView = findViewById(R.id.instructionTextView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        LinearLayout container = findViewById(R.id.al_fill_the_block);

        if (titleText != null && !titleText.isEmpty()) {
            titleTextView.setText(titleText);
        }

        if (instructionText != null && !instructionText.isEmpty()) {
            instructionTextView.setText(instructionText);
        }

        if (programValue != null && !programValue.isEmpty()) {
            programValue = programValue.replace("/n", "\n");
            String[] lines = programValue.split("\n");

            for (String line : lines) {
                LinearLayout lineLayout = new LinearLayout(this);
                lineLayout.setOrientation(LinearLayout.HORIZONTAL);
                lineLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                lineLayout.setPadding(0, 16, 0, 16);

                while (line.contains("***")) {
                    int start = line.indexOf("***");

                    if (start > 0) {
                        String before = line.substring(0, start);
                        TextView beforeText = new TextView(this);
                        beforeText.setText(before);
                        beforeText.setTextSize(16);
                        lineLayout.addView(beforeText);
                    }

                    EditText input = new EditText(this);
                    input.setHint("...");
                    input.setEms(6);
                    input.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    inputFields.add(input);
                    lineLayout.addView(input);

                    // Αφαίρεση των αστερίσκων και συνέχιση με το υπόλοιπο κείμενο
                    line = line.substring(start + 3);
                }

// Αν απομένει υπόλοιπο κείμενο, προσθέτουμε το υπόλοιπο
                if (!line.isEmpty()) {
                    TextView remainingText = new TextView(this);
                    remainingText.setText(line);
                    remainingText.setTextSize(16);
                    lineLayout.addView(remainingText);
                }


                container.addView(lineLayout);
            }

            // Κουμπί υποβολής
            Button submitButton = new Button(this);
            submitButton.setText("Υποβολή");
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(0, 32, 0, 16);
            submitButton.setLayoutParams(buttonParams);
            submitButton.setOnClickListener(v -> checkAnswers());
            container.addView(submitButton);

            // Feedback TextView (μετά την υποβολή)
            feedbackTextView = new TextView(this);
            feedbackTextView.setTextSize(16);
            feedbackTextView.setPadding(0, 16, 0, 16);
            container.addView(feedbackTextView);

            // Κουμπί για παρακολούθηση βίντεο (μετά το feedback)
            watchVideoButton = new Button(this);
            watchVideoButton.setText("Δες και κάτι ακόμα!");
            watchVideoButton.setVisibility(View.GONE);
            watchVideoButton.setOnClickListener(v -> {
                if (videoUrl != null && !videoUrl.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                    startActivity(intent);
                }
            });
            container.addView(watchVideoButton);
        }
    }

    private void checkAnswers() {
        if (correctAnswers == null || correctAnswers.size() != inputFields.size()) {
            feedbackTextView.setText("Σφάλμα: δεν βρέθηκαν σωστές απαντήσεις.");
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        boolean allCorrect = true;
        StringBuilder incorrectFeedback = new StringBuilder("Προσπάθησε ξανά.\n\nΣωστές απαντήσεις:\n");

        for (int i = 0; i < inputFields.size(); i++) {
            String userAnswer = inputFields.get(i).getText().toString().trim();
            String expectedAnswer = correctAnswers.get(i).trim();

            if (!userAnswer.equalsIgnoreCase(expectedAnswer)) {
                allCorrect = false;
            }

            incorrectFeedback.append("- ").append(expectedAnswer).append("\n");
        }

        if (allCorrect) {
            feedbackTextView.setText("Σωστά!");
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            watchVideoButton.setVisibility(View.VISIBLE);
        } else {
            feedbackTextView.setText(incorrectFeedback.toString());
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            watchVideoButton.setVisibility(View.GONE);
        }
    }

}
