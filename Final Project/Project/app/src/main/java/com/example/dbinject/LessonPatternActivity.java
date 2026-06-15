package com.example.dbinject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
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

public class LessonPatternActivity extends AppCompatActivity {

    private String username;
    private String lesson;
    private String lesson_id;
    private TextView lessonIdTextView;
    private TextView titleTextView;
    private FirebaseDatabase database;
    private DatabaseReference lessonsRef;
    private long lessonStartTime;

    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private CustomExpandableListAdapter listAdapter;
    private boolean lessonCompleted = false; // Flag για να αποφευχθούν πολλαπλές εγγραφές ολοκλήρωσης

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lesson_pattern);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        expandableListView = findViewById(R.id.lessonExpandableList);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        lessonIdTextView = findViewById(R.id.moduleTitle);
        titleTextView = findViewById(R.id.title);

        database = FirebaseDatabase.getInstance();
        lessonsRef = database.getReference("lessons");

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                Toast.makeText(this, "Username που ελήφθη: " + username, Toast.LENGTH_SHORT).show();
            }

            if (intent.hasExtra("LESSON_ID")) {
                lesson = intent.getStringExtra("LESSON_ID");
                lessonIdTextView.setText(lesson);

                if (lesson.equals("Ενότητα 1")) {
                    lesson_id = "lesson_1";
                } else if (lesson.equals("Ενότητα 2")) {
                    lesson_id = "lesson_2";
                } else if (lesson.equals("Ενότητα 3")) {
                    lesson_id = "lesson_3";
                } else if (lesson.equals("Ενότητα 4")) {
                    lesson_id = "lesson_4";
                } else if (lesson.equals("Ενότητα 5")) {
                    lesson_id = "lesson_5";
                }
            }
        }

        loadLessonData();
        lessonStartTime = SystemClock.elapsedRealtime(); // Καταγράφουμε την ώρα έναρξης του μαθήματος
    }

    private void loadLessonData() {
        if (lesson_id != null) {
            DatabaseReference currentLessonRef = lessonsRef.child(lesson_id);

            currentLessonRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String title = snapshot.getValue(String.class);
                    titleTextView.setText(title != null ? title : "Τίτλος δεν βρέθηκε");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Σφάλμα τίτλου: " + error.getMessage());
                    Toast.makeText(LessonPatternActivity.this, "Σφάλμα φόρτωσης τίτλου.", Toast.LENGTH_SHORT).show();
                }
            });

            currentLessonRef.child("theory").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        populateExpandableList(snapshot);
                    } else {
                        Toast.makeText(LessonPatternActivity.this, "Δεν βρέθηκε θεωρία.", Toast.LENGTH_SHORT).show();
                        Log.d("LessonData", "Δεν βρέθηκε κόμβος 'theory' για " + lesson_id);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LessonPatternActivity.this, "Σφάλμα φόρτωσης θεωρίας.", Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Σφάλμα φόρτωσης θεωρίας για " + lesson_id + ": " + error.getMessage());
                }
            });
        } else {
            Log.e("LessonData", "Το lesson_id είναι null. Δεν μπορώ να φορτώσω δεδομένα.");
            Toast.makeText(this, "Σφάλμα: Δεν βρέθηκε ID μαθήματος.", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateExpandableList(DataSnapshot theorySnapshot) {
        listDataHeader.clear();
        listDataChild.clear();

        String lastHeaderTitle = null;

        for (DataSnapshot sectionSnapshot : theorySnapshot.getChildren()) {
            String headerTitle = sectionSnapshot.child("title").getValue(String.class);
            String childContent = sectionSnapshot.child("content").getValue(String.class);

            Log.d("LessonData", "Επεξεργασία Section: " + sectionSnapshot.getKey() + ", Title: " + headerTitle + ", Content: " + childContent);

            if (headerTitle == null) continue;

            if (!headerTitle.equalsIgnoreCase("Πόρος")) {
                lastHeaderTitle = headerTitle;
                listDataHeader.add(headerTitle);
                List<String> childList = new ArrayList<>();
                if (childContent != null) {
                    childList.add(childContent);
                    Log.d("LessonData", "Προσθήκη στην ομάδα '" + headerTitle + "': " + childContent);
                }
                listDataChild.put(headerTitle, childList);
            } else if (lastHeaderTitle != null && childContent != null) {
                if (listDataChild.containsKey(lastHeaderTitle)) {
                    listDataChild.get(lastHeaderTitle).add(childContent);
                    Log.d("LessonData", "Προσθήκη 'Πόρου' στην ομάδα '" + lastHeaderTitle + "': " + childContent);
                } else {
                    Log.w("LessonData", "Προσπάθεια προσθήκης 'Πόρου' σε μη υπάρχουσα ομάδα: " + lastHeaderTitle);
                }
            }
        }

        listAdapter = new CustomExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(listAdapter);
        //expandAll();
    }


    public void expandAll(View view) {
        if (listAdapter != null && expandableListView != null) {
            int groupCount = listAdapter.getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                expandableListView.expandGroup(i);
            }
        }
    }


    public void collapseAll(View view) {
        if (listAdapter != null) {
            int groupCount = listAdapter.getGroupCount();
            Log.d("LessonData", "collapseAll() called, groupCount: " + groupCount);
            for (int i = 0; i < groupCount; i++) {
                expandableListView.collapseGroup(i);
            }
        } else {
            Log.w("LessonData", "Ο listAdapter είναι null, δεν μπορώ να συρρικνώσω τις ομάδες.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Καταγράφουμε την ολοκλήρωση του μαθήματος όταν η Activity σταματάει (θεωρούμε ότι ο χρήστης το διάβασε όλο)
        if (!lessonCompleted) {
            long lessonEndTime = SystemClock.elapsedRealtime();
            long lessonDurationMillis = lessonEndTime - lessonStartTime;
            long lessonDurationMinutes = (lessonDurationMillis / (1000 * 60));

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userStatsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("statistics");

                // Αποθήκευση του τελευταίου ολοκληρωμένου μαθήματος
                userStatsRef.child("last_completed_course").setValue(lesson);

                // Ενημέρωση του συνολικού χρόνου εκπαίδευσης
                userStatsRef.child("total_training_time").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long currentTotalTime = dataSnapshot.getValue(Long.class);
                        long newTotalTrainingTime = (currentTotalTime == null ? 0 : currentTotalTime) + lessonDurationMinutes;
                        userStatsRef.child("total_training_time").setValue(newTotalTrainingTime);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Σφάλμα κατά την ενημέρωση του συνολικού χρόνου μαθήματος: " + databaseError.getMessage());
                    }
                });
                lessonCompleted = true; // Σημαίνουμε ότι η ολοκλήρωση καταγράφηκε
            } else {
                Log.w("LessonPatternActivity", "Δεν είναι συνδεδεμένος χρήστης κατά την ολοκλήρωση του μαθήματος.");
            }
        }
    }

    public static class CustomExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> listDataHeader;
        private HashMap<String, List<String>> listDataChild;

        public CustomExpandableListAdapter(Context context, List<String> listDataHeader,
                                           HashMap<String, List<String>> listChildData) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listChildData;
            Log.d("AdapterInfo", "CustomExpandableListAdapter created. Header size: " + (listDataHeader != null ? listDataHeader.size() : 0) + ", Child size: " + (listChildData != null ? listChildData.size() : 0));
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Log.d("AdapterInfo", "getChild() called for group: " + groupPosition + ", child: " + childPosition);
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size() &&
                    this.listDataChild != null && this.listDataChild.get(this.listDataHeader.get(groupPosition)) != null &&
                    childPosition < this.listDataChild.get(this.listDataHeader.get(groupPosition)).size()) {
                return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
            }
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Log.d("AdapterInfo", "getChildId() called for group: " + groupPosition + ", child: " + childPosition + ", returning: " + childPosition);
            return childPosition;
        }

        /*@Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            Log.d("AdapterInfo", "getChildView() CALLED - Group: " + groupPosition + ", Child: " + childPosition + ", Text: " + getChild(groupPosition, childPosition));
            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = convertView.findViewById(R.id.lblListItem);
            ImageView imgChildImage = convertView.findViewById(R.id.lessonImageView);

            if (childText != null && childText.startsWith("https://")) {
                txtListChild.setVisibility(View.GONE);
                imgChildImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(childText)
                        .placeholder(R.drawable.placeholder) // προαιρετικό
                        .into(imgChildImage);
            } else {
                txtListChild.setVisibility(View.VISIBLE);
                imgChildImage.setVisibility(View.GONE);
                txtListChild.setText(childText);
            }

            return convertView;
        }

        private boolean isImageUrl(String text) {
            return text.matches("^https?:.*\\.(jpg|jpeg|png|gif|bmp|webp)$");
        }*/
        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String rawText = (String) getChild(groupPosition, childPosition);
            final String childText = rawText != null ? rawText.trim() : "";

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = convertView.findViewById(R.id.lblListItem);
            ImageView imgChildImage = convertView.findViewById(R.id.lessonImageView);

            Log.d("ChildViewContent", "Text: " + childText);

            if (!childText.isEmpty() && (childText.startsWith("http://") || childText.startsWith("https://"))) {
                txtListChild.setVisibility(View.GONE);
                imgChildImage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(childText)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .centerInside()
                        .into(imgChildImage);
            } else {
                txtListChild.setVisibility(View.VISIBLE);
                imgChildImage.setVisibility(View.GONE);
                txtListChild.setText(childText);
            }

            return convertView;
        }



        @Override
        public int getChildrenCount(int groupPosition) {
            int count = 0;
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size() && this.listDataChild != null && this.listDataChild.containsKey(this.listDataHeader.get(groupPosition))) {
                count = this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
            }
            Log.d("AdapterInfo", "getChildrenCount() called for group " + groupPosition + ", returning: " + count);
            return count;
        }

        @Override
        public Object getGroup(int groupPosition) {
            Log.d("AdapterInfo", "getGroup() called for group: " + groupPosition);
            if (this.listDataHeader != null && groupPosition < this.listDataHeader.size()) {
                return this.listDataHeader.get(groupPosition);
            }
            return null;
        }

        @Override
        public int getGroupCount() {
            int size = (this.listDataHeader != null) ? this.listDataHeader.size() : 0;
            Log.d("AdapterInfo", "getGroupCount() called, returning: " + size);
            return size;
        }

        @Override
        public long getGroupId(int groupPosition) {
            Log.d("AdapterInfo", "getGroupId() called for group: " + groupPosition + ", returning: " + groupPosition);
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            Log.d("AdapterInfo", "getGroupView() called for group: " + groupPosition + ", isExpanded: " + isExpanded);
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
            if (lblListHeader != null && headerTitle != null) {
                lblListHeader.setTypeface(null, Typeface.BOLD);
                lblListHeader.setText(headerTitle);
            } else {
                Log.w("AdapterInfo", "lblListHeader ή headerTitle είναι null.");
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
    }
}