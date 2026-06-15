package com.example.dbinject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SignUpActivity extends AppCompatActivity {
    EditText usernameEditText, emailEditText, passwordEditText, confirmEditText;
    ImageView togglePassword, toggleConfirm;
    Spinner learningStyleSpinner;
    TextView loginLink;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    boolean passVisible, confVisible;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,i)->{
            Insets sb = i.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left,sb.top,sb.right,sb.bottom);
            return i;
        });

        usernameEditText    = findViewById(R.id.usernameEditText);
        emailEditText       = findViewById(R.id.emailEditText);
        passwordEditText    = findViewById(R.id.passwordEditText);
        confirmEditText     = findViewById(R.id.confirmEditText);
        togglePassword      = findViewById(R.id.togglePassword);
        toggleConfirm       = findViewById(R.id.toggleConfirm);
        learningStyleSpinner= findViewById(R.id.learningStyleSpinner);
        loginLink           = findViewById(R.id.loginLink);

        mAuth   = FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference("users");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.learning_styles, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        learningStyleSpinner.setAdapter(adapter);

        togglePassword.setOnClickListener(v->{
            passVisible = !passVisible;
            passwordEditText.setTransformationMethod(
                    passVisible ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()
            );
            togglePassword.setImageResource(
                    passVisible ? R.drawable.password : R.drawable.password
            );
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        toggleConfirm.setOnClickListener(v->{
            confVisible = !confVisible;
            confirmEditText.setTransformationMethod(
                    confVisible ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()
            );
            toggleConfirm.setImageResource(
                    confVisible ? R.drawable.password : R.drawable.password
            );
            confirmEditText.setSelection(confirmEditText.getText().length());
        });

        loginLink.setOnClickListener(v->{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    public void singUp(View v) {
        String u = usernameEditText.getText().toString().trim();
        String e = emailEditText.getText().toString().trim();
        String p = passwordEditText.getText().toString().trim();
        String c = confirmEditText.getText().toString().trim();
        String l = learningStyleSpinner.getSelectedItem().toString();
        if (TextUtils.isEmpty(u)||TextUtils.isEmpty(e)||TextUtils.isEmpty(p)||TextUtils.isEmpty(c)) return;
        if (!p.equals(c)) {
            confirmEditText.setError("Οι κωδικοί δεν ταιριάζουν");
            return;
        }
        mAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(task->{
            if (task.isSuccessful()) {
                FirebaseUser fu = mAuth.getCurrentUser();
                if (fu!=null) saveUserData(fu.getUid(),u,e,l);
            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                emailEditText.setError("Email ήδη χρησιμοποιείται");
            }
        });
    }

    void saveUserData(String uid,String u,String e,String l){
        Map<String,Object> data = new HashMap<>();
        data.put("username",u);
        data.put("email",e);
        data.put("learning_style",l);
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        data.put("registration_date",now);
        Map<String,Object> stats = new HashMap<>();
        stats.put("total_quizzes",0);
        stats.put("correct_answers",0);
        stats.put("avg_score","0%");
        data.put("statistics",stats);
        Map<String,Object> prog = new HashMap<>();
        prog.put("status","");
        prog.put("time_spent",0);
        data.put("lesson_progress",prog);


        usersRef.child(uid).setValue(data).addOnSuccessListener(a->{
            startActivity(new Intent(this,HomeActivity.class).putExtra("USERNAME",u));
            finish();
        });
    }
}
