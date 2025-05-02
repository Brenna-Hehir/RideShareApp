package edu.uga.cs.rideshareapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import java.util.List;

/**
 * Activity to display a user's own ride posts (either offers or requests) that haven't been accepted yet.
 * Allows toggling between offer/request views and returning to the main dashboard.
 */
public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView myPostsRecyclerView;
    private RidePostAdapter adapter;
    private List<Ride> rideList;
    private List<String> rideKeys;

    private TextView currentListHeader;

    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    private int scrollPosition = 0;

    // Tracks whether the user is viewing their offers or requests
    private String currentMode = "offer"; // "offer" or "request"

    /**
     * Called when the activity is starting. Initializes UI elements and loads unaccepted posts.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView and adapter
        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        Button myOffersButton   = findViewById(R.id.myOffersButton);
        Button myRequestButton  = findViewById(R.id.myRequestsButton);
        currentListHeader       = findViewById(R.id.currentListHeader);
        Button homeButton       = findViewById(R.id.homeButton);

        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideList = new ArrayList<>();
        rideKeys = new ArrayList<>();
        adapter = new RidePostAdapter(rideList, rideKeys, "MY_POSTS");
        myPostsRecyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadMyPosts();

        // Show user's unaccepted ride offers
        myOffersButton.setOnClickListener(v -> {
            currentMode = "offer";
            currentListHeader.setText("My Unaccepted Offers");
            loadMyPosts();
        });

        // Show user's unaccepted ride requests
        myRequestButton.setOnClickListener(v -> {
            currentMode = "request";
            currentListHeader.setText("My Unaccepted Requests");
            loadMyPosts();
        });

        // Navigate back to the main activity
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(MyPostsActivity.this, MainActivity.class));
        });
    }

    /**
     * Reloads the current list of unaccepted posts when returning to the activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadMyPosts();  // refresh whenever the user returns
    }

    /**
     * Loads unaccepted ride offers or requests (based on currentMode) created by the current user.
     * Populates the adapter with results from Firebase.
     */
    private void loadMyPosts() {
        if (currentUser == null) return;

        // clear both lists before re-fetching
        rideList.clear();
        rideKeys.clear();

        databaseRef
                .orderByChild("accepted")
                .equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Ride ride = postSnapshot.getValue(Ride.class);
                            if (ride == null) continue;

                            String uid = currentUser.getUid();
                            if (currentMode.equals("offer")
                                    && ride.driverId != null
                                    && ride.driverId.equals(uid)) {
                                rideList.add(ride);
                                rideKeys.add(postSnapshot.getKey());
                            }
                            if (currentMode.equals("request")
                                    && ride.riderId != null
                                    && ride.riderId.equals(uid)) {
                                rideList.add(ride);
                                rideKeys.add(postSnapshot.getKey());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    /**
     * Saves the current scroll position and current mode (offer/request) to restore on configuration changes.
     *
     * @param outState The Bundle to write state data to.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        scrollPosition = ((LinearLayoutManager) myPostsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        outState.putInt("scrollPosition", scrollPosition);
        outState.putString("currentMode", currentMode);
    }

    /**
     * Restores scroll position and filter mode (offer/request) after a configuration change.
     *
     * @param savedInstanceState The Bundle containing the saved state.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentMode = savedInstanceState.getString("currentMode", "offer");

        // Update header text based on restored mode
        if (currentMode.equals("offer")) {
            currentListHeader.setText("My Unaccepted Offers");
        } else {
            currentListHeader.setText("My Unaccepted Requests");
        }

        loadMyPosts();

        scrollPosition = savedInstanceState.getInt("scrollPosition", 0);
        myPostsRecyclerView.scrollToPosition(scrollPosition);
    }
}
