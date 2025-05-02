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

/**
 * The main dashboard activity for the RideShare app.
 * Displays active rides, user points, and navigation to other features.
 */
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

    /**
     * Called when the activity is first created. Initializes UI and listeners.
     *
     * @param savedInstanceState Previously saved instance state bundle
     */
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

        // Navigates to logout confirmation dialog
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutConfirmation();
            }
        });

        // Navigates to RideFormActivity
        rideFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RideFormActivity.class);
                startActivity(intent);
            }
        });

        //  Navigates to MyPostsActivity
        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyPostsActivity.class);
                startActivity(intent);
            }
        });

        // Navigates to OthersPostsActivity
        othersPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OthersPostsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Loads all active rides from Firebase where the current user is a driver or rider.
     * Only unconfirmed rides are shown.
     */
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

                        // Sort rides chronologically
                        Collections.sort(activeRideList, (ride1, ride2) -> ride1.dateTime.compareTo(ride2.dateTime));

                        activeRideAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load active rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sets up a listener to update the user's point display in real-time.
     */
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

    /**
     * Displays a confirmation dialog before logging out.
     */
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

    /**
     * Logs the user out and redirects to the LoginActivity.
     */
    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Checks on activity start whether a user is still logged in.
     * Redirects to login if not.
     */
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

    /**
     * Saves the current scroll position of the ride list when activity is paused.
     *
     * @param outState Bundle where the state is saved
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        scrollPosition = ((LinearLayoutManager) activeRidesRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        outState.putInt("activeScrollPosition", scrollPosition);
    }

    /**
     * Restores the scroll position of the ride list when activity is resumed.
     *
     * @param savedInstanceState Bundle containing previously saved state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        scrollPosition = savedInstanceState.getInt("activeScrollPosition", 0);
        activeRidesRecyclerView.scrollToPosition(scrollPosition);
    }

    /**
     * Cleans up Firebase listeners to avoid memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && pointsListener != null) {
            userRef.removeEventListener(pointsListener);
        }
    }

}