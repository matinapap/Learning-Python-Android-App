package com.example.dbinject;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class ALTheoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altheory); // Η διάταξη του Activity

        WebView webView = findViewById(R.id.webView); // Εύρεση του WebView
        webView.setWebViewClient(new WebViewClient()); // Να ανοίγονται οι σύνδεσμοι στο WebView και όχι σε εξωτερικό browser

        // Λήψη του URL από το Intent
        String url = getIntent().getStringExtra("lesson_url");

        // Αν το URL δεν είναι null, το φορτώνουμε στο WebView
        if (url != null) {
            webView.loadUrl(url);
        }
    }
}
