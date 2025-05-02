package edu.uga.cs.rideshareapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView activeRidesRecyclerView;
    private ActiveRideAdapter activeRideAdapter;
    private List<Ride> activeRideList;
    private List<String> activeRideKeys;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    private TextView pointsTextView;

    private DatabaseReference userRef;
    private ValueEventListener pointsListener;
    private int scrollPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        Button logoutButton = findViewById(R.id.logoutButton);
        Button rideFormButton = findViewById(R.id.rideFormButton);
        Button myPostsButton = findViewById(R.id.myPosts);
        Button othersPostsButton = findViewById(R.id.othersPosts);

        activeRidesRecyclerView = findViewById(R.id.activeRidesRecyclerView);
        activeRidesRecyclerView.setHasFixedSize(true);
        activeRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        activeRideList = new ArrayList<>();
        activeRideKeys = new ArrayList<>();

        activeRideAdapter = new ActiveRideAdapter(activeRideList, activeRideKeys);
        activeRidesRecyclerView.setAdapter(activeRideAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("rides");

        pointsTextView = findViewById(R.id.pointsTextView);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("points");
            setupPointsListener();
        }

        loadActiveRides();


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutConfirmation();
            }
        });

        rideFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RideFormActivity.class);
                startActivity(intent);
            }
        });

        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyPostsActivity.class);
                startActivity(intent);
            }
        });

        othersPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OthersPostsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadActiveRides() {
        if (currentUser == null) return;

        databaseRef.orderByChild("accepted").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        activeRideList.clear();
                        activeRideKeys.clear();

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Ride ride = postSnapshot.getValue(Ride.class);

                            if (ride != null) {
                                // Only show rides where current user is involved
                                if ((ride.driverId != null && ride.driverId.equals(currentUser.getUid())) ||
                                        (ride.riderId != null && ride.riderId.equals(currentUser.getUid()))) {

                                    if (!(ride.driverConfirmed && ride.riderConfirmed)) {
                                        ride.setKey(postSnapshot.getKey());
                                        activeRideList.add(ride);
                                        activeRideKeys.add(postSnapshot.getKey());
                                    }
                                }
                            }
                        }

                        Collections.sort(activeRideList, (ride1, ride2) -> ride1.dateTime.compareTo(ride2.dateTime));

                        activeRideAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load active rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPointsListener() {
        pointsListener = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long points = snapshot.getValue(Long.class);
                    pointsTextView.setText("Points: " + (points != null ? points : 0));
                } else {
                    pointsTextView.setText("Points: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pointsTextView.setText("Points: Error");
            }
        });
    }


    private void logoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logoutUser();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to login page
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        scrollPosition = ((LinearLayoutManager) activeRidesRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        outState.putInt("activeScrollPosition", scrollPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        scrollPosition = savedInstanceState.getInt("activeScrollPosition", 0);
        activeRidesRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && pointsListener != null) {
            userRef.removeEventListener(pointsListener);
        }
    }

}