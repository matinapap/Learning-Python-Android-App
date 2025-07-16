package com.example.dbinject;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class AdaptiveLearning {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public void SelectTheImage(String lesson, String unit, String type, Context context) {
        DatabaseReference baseRef = database.getReference("lessons")
                .child(lesson)
                .child("pass")
                .child(type);

        DatabaseReference exercisesRef = baseRef.child("exercises");
        DatabaseReference urlRef = baseRef.child("url");

        urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot urlSnapshot) {
                String url = urlSnapshot.getValue(String.class);

                exercisesRef.child("program").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot programSnapshot) {
                        List<String> programBlocks = new ArrayList<>();
                        for (DataSnapshot blockSnap : programSnapshot.getChildren()) {
                            String blockText = blockSnap.getValue(String.class);
                            if (blockText != null) {
                                programBlocks.add(blockText);
                            }
                        }

                        exercisesRef.child("instruction").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot instructionSnapshot) {
                                String instructionValue = instructionSnapshot.getValue(String.class);

                                exercisesRef.child("answers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot answersSnapshot) {

                                        String correctAnswerKey = String.valueOf(answersSnapshot.getValue());

                                        Intent intent = new Intent(context, ALSelectTheImageActivity.class);
                                        intent.putExtra("program_data", new ArrayList<>(programBlocks));
                                        intent.putExtra("instructions_data", instructionValue);
                                        intent.putExtra("title_data", unit);
                                        intent.putExtra("url", url);
                                        intent.putExtra("answer_data", correctAnswerKey);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(context, "Σφάλμα λήψης απάντησης: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Σφάλμα λήψης οδηγιών: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Σφάλμα λήψης blocks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Σφάλμα λήψης URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Challenge(String lesson, String unit, String type, Context context) {
        DatabaseReference baseRef = database.getReference("lessons")
                .child(lesson)
                .child("pass")
                .child(type);

        DatabaseReference instructionsRef = baseRef.child("instruction");
        DatabaseReference answersRef = baseRef.child("answers");
        DatabaseReference urlRef = baseRef.child("url");

        urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot urlSnapshot) {
                String url = urlSnapshot.getValue(String.class);

                instructionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot instructionsSnapshot) {
                        String instructions = instructionsSnapshot.getValue(String.class);

                        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot answersSnapshot) {
                                String rawAnswer = String.valueOf(answersSnapshot.getValue());

                                // Αντικαθιστούμε τα `/n` με πραγματικές αλλαγές γραμμής
                                String formattedAnswer = rawAnswer.replace("/n", "\n");

                                Intent intent = new Intent(context, ALChallengeActivity.class);
                                intent.putExtra("title_data", unit);
                                intent.putExtra("url", url);
                                intent.putExtra("instructions", instructions);
                                intent.putExtra("answer_data", formattedAnswer);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Σφάλμα λήψης απάντησης: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Σφάλμα λήψης οδηγιών: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Σφάλμα λήψης URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void FillTheGaps(String lesson, String unit, String type, Context context) {
        DatabaseReference baseRef = database.getReference("lessons")
                .child(lesson)
                .child("pass")
                .child(type);

        DatabaseReference exercisesRef = baseRef.child("exercises");
        DatabaseReference urlRef = baseRef.child("url");

        urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot urlSnapshot) {
                String videoUrl = urlSnapshot.getValue(String.class);

                exercisesRef.child("program").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot programSnapshot) {
                        String programValue = programSnapshot.getValue(String.class);

                        exercisesRef.child("instruction").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot instructionSnapshot) {
                                String instructionValue = instructionSnapshot.getValue(String.class);

                                exercisesRef.child("answers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot answersSnapshot) {
                                        List<String> answersList = new ArrayList<>();
                                        for (DataSnapshot answerSnap : answersSnapshot.getChildren()) {
                                            String answer = answerSnap.getValue(String.class);
                                            if (answer != null) answersList.add(answer);
                                        }

                                        Intent intent = new Intent(context, ALFillTheBlocksActivity.class);
                                        intent.putExtra("program_data", programValue);
                                        intent.putExtra("instructions_data", instructionValue);
                                        intent.putExtra("title_data", unit);
                                        intent.putExtra("video_url", videoUrl); // <--- ΝΕΟ
                                        intent.putStringArrayListExtra("answers_data", new ArrayList<>(answersList));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(context, "Σφάλμα λήψης απαντήσεων: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Σφάλμα λήψης οδηγιών: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Σφάλμα λήψης προγράμματος: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Σφάλμα λήψης URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DragAndDrop(String lesson, String unit, String type, Context context) {
        DatabaseReference baseRef = database.getReference("lessons")
                .child(lesson)
                .child("pass")
                .child(type);

        DatabaseReference exercisesRef = baseRef.child("exercises");
        DatabaseReference urlRef = baseRef.child("url");

        urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot urlSnapshot) {
                String videoUrl = urlSnapshot.getValue(String.class);

                exercisesRef.child("program").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot programSnapshot) {
                        String programValue = programSnapshot.getValue(String.class);

                        exercisesRef.child("instruction").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot instructionSnapshot) {
                                String instructionValue = instructionSnapshot.getValue(String.class);

                                List<String> answerLines = new ArrayList<>();
                                if (programValue != null) {
                                    String[] lines = programValue.split("/n");  // ← αν έχει γίνει λάθος, ίσως είναι "\n" αντί για "/n"
                                    for (String line : lines) {
                                        String trimmed = line.trim();
                                        if (!trimmed.isEmpty()) {
                                            answerLines.add(trimmed);
                                        }
                                    }
                                }

                                if (answerLines.isEmpty()) {
                                    Toast.makeText(context, "Το πρόγραμμα δεν περιέχει έγκυρες γραμμές.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Intent intent = new Intent(context, ALDragAndDropActivity.class);
                                intent.putExtra("program_data", programValue);
                                intent.putExtra("instructions_data", instructionValue);
                                intent.putExtra("title_data", unit);
                                intent.putExtra("video_url", videoUrl); // ➤ προσθήκη URL
                                intent.putStringArrayListExtra("answers_data", new ArrayList<>(answerLines));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Σφάλμα λήψης οδηγιών: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Σφάλμα λήψης προγράμματος: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Σφάλμα λήψης URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void VisualFail(String lesson, Context context) {
        DatabaseReference lessonsRef = database.getReference("lessons")
                .child(lesson)
                .child("fail")
                .child("url");

        lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url != null && !url.trim().isEmpty()) {
                    url = url.trim();

                    Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    youtubeIntent.setPackage("com.google.android.youtube");

                    if (youtubeIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(youtubeIntent);
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                    }
                } else {
                    Toast.makeText(context, "Μη έγκυρο URL.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AL_Lesson1(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && learningStyle != null) {
                    DatabaseReference lessonsRef = database.getReference("lessons")
                            .child("lesson_1")
                            .child("pass")
                            .child("url");

                    lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = snapshot.getValue(String.class);
                            if (url != null) {
                                url = url.trim();
                                Intent intent = new Intent(context, ALTheoryActivity.class);
                                intent.putExtra("lesson_url", url);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Το URL είναι κενό.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (Boolean.FALSE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {
                    DatabaseReference lessonsRef = database.getReference("lessons")
                            .child("lesson_1")
                            .child("fail")
                            .child("url");

                    lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = snapshot.getValue(String.class);
                            if (url != null) {
                                url = url.trim();

                                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                youtubeIntent.setPackage("com.google.android.youtube");

                                if (youtubeIntent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(youtubeIntent);
                                } else {
                                    // fallback στον browser
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    context.startActivity(browserIntent);
                                }
                            } else {
                                Toast.makeText(context, "Το URL είναι κενό.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (Boolean.FALSE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {
                    Intent intent = new Intent(context, LessonPatternActivity.class);
                    intent.putExtra("LESSON_ID", "Ενότητα 1");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AL_Lesson2(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    FillTheGaps("lesson_2", "Ενότητα 2", "visual", context);
                } else if (Boolean.TRUE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {

                    DragAndDrop("lesson_2", "Ενότητα 2", "theoretical", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    VisualFail("lesson_2", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {
                    Intent intent = new Intent(context, LessonPatternActivity.class);
                    intent.putExtra("LESSON_ID", "Ενότητα 2");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AL_Lesson3(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    SelectTheImage("lesson_3", "Ενότητα 3", "visual", context);
                } else if (Boolean.TRUE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {

                    DragAndDrop("lesson_3", "Ενότητα 3", "theoretical", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    VisualFail("lesson_3", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {
                    Intent intent = new Intent(context, LessonPatternActivity.class);
                    intent.putExtra("LESSON_ID", "Ενότητα 3");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AL_Lesson4(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    FillTheGaps("lesson_4", "Ενότητα 4", "visual", context);
                } else if (Boolean.TRUE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {

                    SelectTheImage("lesson_4", "Ενότητα 4", "theoretical", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    VisualFail("lesson_4", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {
                    Intent intent = new Intent(context, LessonPatternActivity.class);
                    intent.putExtra("LESSON_ID", "Ενότητα 4");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void AL_Lesson5(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    Challenge("lesson_5", "Ενότητα 5", "visual", context);
                } else if (Boolean.TRUE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {

                    Challenge("lesson_5", "Ενότητα 5", "theoretical", context);
                } else if (Boolean.FALSE.equals(quizSuccess) && "Οπτικός (Visual)".equals(learningStyle)) {

                    VisualFail("lesson_5", context);

                } else if (Boolean.FALSE.equals(quizSuccess) && "Θεωρητικός".equals(learningStyle)) {
                    Intent intent = new Intent(context, LessonPatternActivity.class);
                    intent.putExtra("LESSON_ID", "Ενότητα 5");
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AL_Lesson6(String userId, Context context) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean quizSuccess = snapshot.child("statistics")
                        .child("quiz_statistics")
                        .child("quiz_success")
                        .getValue(Boolean.class);

                String learningStyle = snapshot.child("learning_style").getValue(String.class);

                if (Boolean.TRUE.equals(quizSuccess) && learningStyle != null) {
                    DatabaseReference lessonsRef = database.getReference("lessons")
                            .child("lesson_6")
                            .child("pass")
                            .child("url");

                    lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = snapshot.getValue(String.class);
                            if (url != null) {
                                url = url.trim();
                                Intent intent = new Intent(context, ALTheoryActivity.class);
                                intent.putExtra("lesson_url", url);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Το URL είναι κενό.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (Boolean.FALSE.equals(quizSuccess) && learningStyle != null) {
                    new AlertDialog.Builder(context)
                            .setTitle("Αποτυχία Διαγωνίσματος")
                            .setMessage("Απέτυχες το διαγώνισμα, ξαναδιάβασε τις ενότητες 1-5.")
                            .setPositiveButton("OK", null)
                            .setCancelable(true)
                            .show();
                } else {
                    Toast.makeText(context, "Σφάλμα κατά την ανάκτηση δεδομένων.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Σφάλμα σύνδεσης με τη βάση.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
