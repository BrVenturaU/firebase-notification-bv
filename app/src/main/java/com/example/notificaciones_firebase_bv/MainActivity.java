package com.example.notificaciones_firebase_bv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etName, etEmail, etPassword;
    private Button btnSave;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSave:{
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String errorMessage = "El {0} debe tener mínimo {1} letras.";
                boolean isValid =
                        validateData(name, 4, etName, MessageFormat.format(errorMessage, "nombre", 4)) &&
                        validateData(email, 8, etEmail, MessageFormat.format(errorMessage, "correo", 8)) &&
                        validateData(password, 6, etPassword, MessageFormat.format(errorMessage, "valor de la contraseña", 6));
                if(isValid){
                    addUser(name, email, password);
                }else{
                    Toast.makeText(this, "Error de validación.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void addUser(String name, String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("email", email);
                    map.put("password", password);
                    String id = firebaseAuth.getCurrentUser().getUid();
                    database.child("users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                                intent.putExtra("id", id);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(MainActivity.this, "Verifique su conexión a internet.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Algo salio mal, por favor vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateData(String value, int minLength, EditText editText, String errorMessage){
        if(value.isEmpty() || value.length() < minLength){
            editText.setError(errorMessage);
            return false;
        }
        return true;

    }
}