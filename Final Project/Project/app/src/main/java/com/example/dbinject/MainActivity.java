package com.example.dbinject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    ImageView passwordIcon;
    TextView registerLink;
    boolean passwordVisible;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,insets)->{
            Insets sb=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left,sb.top,sb.right,sb.bottom);
            return insets;
        });
        emailEditText=findViewById(R.id.emailEditText);
        passwordEditText=findViewById(R.id.passwordEditText);
        passwordIcon=findViewById(R.id.passwordIcon);
        registerLink=findViewById(R.id.registerLink);
        mAuth=FirebaseAuth.getInstance();
        usersRef=FirebaseDatabase.getInstance().getReference("users");
        passwordIcon.setOnClickListener(v->{
            passwordVisible=!passwordVisible;
            if(passwordVisible){
                passwordEditText.setTransformationMethod(null);
                passwordIcon.setImageResource(R.drawable.password);
            }else{
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordIcon.setImageResource(R.drawable.password);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
        registerLink.setOnClickListener(v->startActivity(new Intent(this,SignUpActivity.class)));
    }

    public void login(View v){
        String e=emailEditText.getText().toString().trim();
        String p=passwordEditText.getText().toString().trim();
        if(e.isEmpty()){ Toast.makeText(this,"Το email λείπει",Toast.LENGTH_LONG).show(); return;}
        if(p.isEmpty()){ Toast.makeText(this,"Ο κωδικός λείπει",Toast.LENGTH_LONG).show(); return;}
        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(this,task->{
            if(task.isSuccessful()){
                FirebaseUser u=mAuth.getCurrentUser();
                if(u!=null){
                    usersRef.child(u.getUid()).addListenerForSingleValueEvent(new ValueEventListener(){
                        public void onDataChange(DataSnapshot snap){
                            SharedPreferences pr=getSharedPreferences("USER_DATA",MODE_PRIVATE);
                            if(snap.exists()){
                                String name=snap.child("username").getValue(String.class);
                                pr.edit().putString("USERNAME",name).apply();
                                startActivity(new Intent(MainActivity.this,HomeActivity.class).putExtra("USERNAME",name));
                            } else {
                                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                            }
                            finish();
                        }
                        public void onCancelled(DatabaseError e){}
                    });
                }
            } else {
                Exception ex=task.getException();
                if(ex instanceof FirebaseAuthInvalidUserException)
                    Toast.makeText(this,"Δεν υπάρχει ο χρήστης",Toast.LENGTH_LONG).show();
                else if(ex instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(this,"Λάθος στοιχεία",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this,"Σφάλμα: "+ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
