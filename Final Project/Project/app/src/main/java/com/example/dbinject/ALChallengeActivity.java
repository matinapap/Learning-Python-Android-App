package com.example.dbinject;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ALChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alchallenge);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ανάκτηση από το Intent
        String title = getIntent().getStringExtra("title_data");
        String instructions = getIntent().getStringExtra("instructions");
        String url = getIntent().getStringExtra("url");
        String answer = getIntent().getStringExtra("answer_data");

        // Σύνδεση με τα Views
        TextView textView1 = findViewById(R.id.textView1);
        TextView textView2 = findViewById(R.id.textView2);
        WebView webView = findViewById(R.id.webView);
        Button solutionButton = findViewById(R.id.solutionButton);
        TextView solutionTextView = findViewById(R.id.solutionTextView);

        // Εμφάνιση δεδομένων
        textView1.setText(title);
        textView2.setText(instructions);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        // Κουμπί "Ενδεικτική λύση"
        solutionButton.setOnClickListener(v -> {
            if (answer != null) {
                // Αντικαθιστούμε τα "/n" με πραγματική αλλαγή γραμμής
                String formattedAnswer = answer.replace("/n", "\n");
                solutionTextView.setText("Ενδεικτική λύση:\n" + formattedAnswer);
                solutionTextView.setVisibility(View.VISIBLE);
            } else {
                solutionTextView.setText("Δεν υπάρχει διαθέσιμη λύση.");
                solutionTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}
