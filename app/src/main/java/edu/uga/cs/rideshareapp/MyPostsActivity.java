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

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView myPostsRecyclerView;
    private RidePostAdapter adapter;
    private List<Ride> rideList;
    private List<String> rideKeys;

    private TextView currentListHeader;

    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    private String currentMode = "offer"; // "offer" or "request"

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

        myOffersButton.setOnClickListener(v -> {
            currentMode = "offer";
            currentListHeader.setText("My Unaccepted Offers");
            loadMyPosts();
        });

        myRequestButton.setOnClickListener(v -> {
            currentMode = "request";
            currentListHeader.setText("My Unaccepted Requests");
            loadMyPosts();
        });

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(MyPostsActivity.this, MainActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyPosts();  // refresh whenever the user returns
    }

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
}
