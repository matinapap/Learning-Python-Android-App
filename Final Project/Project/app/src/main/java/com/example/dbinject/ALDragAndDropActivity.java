package com.example.dbinject;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ALDragAndDropActivity extends AppCompatActivity {

    private LinearLayout commandContainer;
    private List<String> correctOrder;
    private TextView feedbackTextView;
    private Button openLinkButton;

    private String videoUrl; // Κρατάμε το link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aldrag_and_drop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView lessonTitleText = findViewById(R.id.textLessonTitle);
        TextView instructionTitleText = findViewById(R.id.textInstructionTitle);
        commandContainer = findViewById(R.id.commandContainer);
        View checkButton = findViewById(R.id.checkButton);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        openLinkButton = findViewById(R.id.openLinkButton);

        // Λήψη δεδομένων από το Intent
        String title = getIntent().getStringExtra("title_data");
        String instructions = getIntent().getStringExtra("instructions_data");
        correctOrder = getIntent().getStringArrayListExtra("answers_data");
        videoUrl = getIntent().getStringExtra("video_url");

        if (lessonTitleText != null && title != null) {
            lessonTitleText.setText(title);
        }

        if (instructionTitleText != null && instructions != null) {
            instructionTitleText.setText(instructions);
        }

        if (correctOrder == null || correctOrder.isEmpty()) {
            finish();
            return;
        }

        openLinkButton.setVisibility(View.GONE);
        openLinkButton.setOnClickListener(v -> {
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(ALDragAndDropActivity.this, "Το link δεν είναι διαθέσιμο.", Toast.LENGTH_SHORT).show();
            }
        });

        checkButton.setOnClickListener(v -> checkOrder());

        setupDraggableCommands();
    }

    private void setupDraggableCommands() {
        List<String> randomizedCommands = new ArrayList<>(correctOrder);
        Collections.shuffle(randomizedCommands);

        for (String command : randomizedCommands) {
            TextView commandView = createCommandTextView(command);
            commandContainer.addView(commandView);
        }

        commandContainer.setOnDragListener(new CommandDragListener());
    }

    private void checkOrder() {
        List<String> currentOrder = new ArrayList<>();
        for (int i = 0; i < commandContainer.getChildCount(); i++) {
            View view = commandContainer.getChildAt(i);
            if (view instanceof TextView && view != feedbackTextView) {
                currentOrder.add(((TextView) view).getText().toString());
            }
        }

        if (currentOrder.equals(correctOrder)) {
            feedbackTextView.setText("✅ Σωστή σειρά!");
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            openLinkButton.setVisibility(View.VISIBLE);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("❌ Λάθος σειρά. Προσπάθησε ξανά.\n\nΣωστή σειρά:\n");
            for (String cmd : correctOrder) {
                sb.append("• ").append(cmd).append("\n");
            }
            feedbackTextView.setText(sb.toString());
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            openLinkButton.setVisibility(View.GONE);
        }
    }

    private TextView createCommandTextView(String command) {
        TextView textView = new TextView(this);
        textView.setText(command);
        textView.setTextSize(18);
        textView.setBackgroundColor(Color.LTGRAY); // Μπορείς να το αφαιρέσεις αν θέλεις μόνο το border
        textView.setPadding(20, 20, 20, 20);
        textView.setTextColor(Color.BLACK);

        // Φορτώνουμε το drawable για το border
        Drawable border = ContextCompat.getDrawable(this, R.drawable.command_border);
        textView.setBackground(border); // Θέτουμε το border ως background

        textView.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item(((TextView) v).getText());
            ClipData dragData = new ClipData(
                    ((TextView) v).getText(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item
            );
            v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), v, 0);
            return true;
        });
        return textView;
    }

    private class CommandDragListener implements View.OnDragListener {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();
                    owner.removeView(draggedView);
                    int dropIndex = getDropIndex(event.getY());
                    commandContainer.addView(draggedView, dropIndex);
                    break;
            }
            return true;
        }

        private int getDropIndex(float y) {
            for (int i = 0; i < commandContainer.getChildCount(); i++) {
                View child = commandContainer.getChildAt(i);
                if (child == feedbackTextView) continue;
                if (y < child.getY() + child.getHeight() / 2) {
                    return i;
                }
            }
            return commandContainer.getChildCount() - 1;
        }
    }
}
