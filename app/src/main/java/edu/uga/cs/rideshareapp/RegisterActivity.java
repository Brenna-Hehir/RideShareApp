package edu.uga.cs.rideshareapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity that handles user registration using Firebase Authentication.
 * Validates user input, creates the account, and stores initial user points.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is starting. Sets up input fields and register button.
     *
     * @param savedInstanceState The previously saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    /**
     * Validates the email and password fields, then attempts to register the user using Firebase.
     * On success, initializes user points and navigates to the LoginActivity.
     * On failure, displays appropriate error messages.
     */
    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Check for valid email format
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce minimum password length
        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d( TAG, "Registration Success" );
                            String userId = mAuth.getCurrentUser().getUid();

                            // Set initial points for the new user
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                            userRef.child("points").setValue(150);

                            // Navigate to LoginActivity after successful registration
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        }
                        else {
                            // Handle potential errors during registration
                            String errorMessage;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "This email address is already registered.";
                            } catch (Exception e) {
                                errorMessage = "Registration failed: " + e.getMessage();
                            }

                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Saves the email and password input state to restore later (e.g. on screen rotation).
     *
     * @param outState The Bundle to save state into.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailInput.getText().toString());
        outState.putString("password", passwordInput.getText().toString());
    }

    /**
     * Restores email and password fields from saved state.
     *
     * @param savedInstanceState The Bundle containing the saved input values.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emailInput.setText(savedInstanceState.getString("email", ""));
        passwordInput.setText(savedInstanceState.getString("password", ""));
    }
}