package com.example.dbinject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class ALSelectTheImageActivity extends AppCompatActivity {

    private GridLayout textContainer;
    private TextView feedbackText;
    private String correctAnswer; // Θέση σωστού κουτιού (π.χ. "2")
    private TextView selectedBox = null;

    private Button openUrlButton;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alselect_the_image);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        feedbackText = findViewById(R.id.feedbackText);
        TextView titleText = findViewById(R.id.titleText);
        TextView instructionText = findViewById(R.id.instructionText);
        textContainer = findViewById(R.id.imageContainer);
        Button checkButton = findViewById(R.id.checkButton);

        openUrlButton = findViewById(R.id.openUrlButton);
        url = getIntent().getStringExtra("url");

        openUrlButton.setOnClickListener(v -> {
            if (url != null && !url.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                startActivity(browserIntent);
            } else {
                feedbackText.setText("❌ Δεν υπάρχει διαθέσιμος σύνδεσμος.");
                feedbackText.setTextColor(Color.RED);
            }
        });


        String title = getIntent().getStringExtra("title_data");
        String instruction = getIntent().getStringExtra("instructions_data");
        ArrayList<String> programTexts = getIntent().getStringArrayListExtra("program_data");
        correctAnswer = getIntent().getStringExtra("answer_data"); // Π.χ. "2"

        if (title != null) titleText.setText(title);
        if (instruction != null) instructionText.setText(instruction);
        if (programTexts != null && !programTexts.isEmpty()) {
            populateTextOptions(programTexts);
        }

        checkButton.setOnClickListener(v -> checkAnswers());
    }

    private void populateTextOptions(List<String> texts) {
        for (String item : texts) {
            TextView textBox = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.MATCH_PARENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(16, 16, 16, 16);
            textBox.setLayoutParams(params);
            textBox.setPadding(24, 24, 24, 24);
            textBox.setText(item.replace("/n", "\n"));
            textBox.setTextSize(18);
            textBox.setBackgroundColor(Color.LTGRAY);
            textBox.setTextColor(Color.parseColor("#124559"));

            textBox.setOnClickListener(v -> {
                if (selectedBox != null) {
                    selectedBox.setBackgroundColor(Color.LTGRAY);
                    selectedBox.setTextColor(Color.parseColor("#124559"));
                }

                if (textBox.equals(selectedBox)) {
                    selectedBox = null;
                } else {
                    selectedBox = textBox;
                    textBox.setBackgroundColor(Color.parseColor("#124559"));
                    textBox.setTextColor(Color.WHITE);
                }
            });

            textContainer.addView(textBox);
        }
    }

    private void checkAnswers() {
        if (selectedBox == null) {
            feedbackText.setText("⚠️ Επίλεξε μια απάντηση πρώτα.");
            feedbackText.setTextColor(Color.parseColor("#FFA000")); // Πορτοκαλί για προειδοποίηση
            return;
        }

        int correctIndex;
        try {
            correctIndex = Integer.parseInt(correctAnswer); // π.χ. "2"
        } catch (NumberFormatException e) {
            feedbackText.setText("⚠️ Σφάλμα στον αριθμό σωστής απάντησης.");
            feedbackText.setTextColor(Color.RED);
            return;
        }

        int selectedIndex = textContainer.indexOfChild(selectedBox) + 1;

        if (selectedIndex == correctIndex) {
            feedbackText.setText("✅ Σωστά! Επέλεξες το " + selectedIndex + "ο κουτί.");
            feedbackText.setTextColor(Color.parseColor("#388E3C")); // Πράσινο
            openUrlButton.setVisibility(View.VISIBLE); // Εμφανίζει το κουμπί
        } else {
            feedbackText.setText("❌ Λάθος. Επέλεξες το " + selectedIndex + "ο αντί για το " + correctIndex + "ο.");
            feedbackText.setTextColor(Color.RED);
            openUrlButton.setVisibility(View.GONE); // Απόκρυψη αν ήταν εμφανές
        }

    }

}
