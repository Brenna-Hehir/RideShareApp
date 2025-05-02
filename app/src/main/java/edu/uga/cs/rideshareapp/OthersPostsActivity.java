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

public class OthersPostsActivity extends AppCompatActivity {

    private RecyclerView othersPostsRecyclerView;
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
        setContentView(R.layout.activity_others_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        othersPostsRecyclerView = findViewById(R.id.othersPostsRecyclerView);
        Button othersOffersButton = findViewById(R.id.othersOffersButton);
        Button othersRequestsButton = findViewById(R.id.othersRequestsButton);
        currentListHeader = findViewById(R.id.currentListHeader);
        Button homeButton = findViewById(R.id.homeButton);

        othersPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideList = new ArrayList<>();
        rideKeys = new ArrayList<>();
        adapter = new RidePostAdapter(rideList, rideKeys,"OTHERS_POSTS"); // mode
        othersPostsRecyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadOthersPosts();

        othersOffersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = "offer";
                currentListHeader.setText("Other's Unaccepted Offers");
                loadOthersPosts();
            }
        });

        othersRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = "request";
                currentListHeader.setText("Other's Unaccepted Requests");
                loadOthersPosts();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OthersPostsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadOthersPosts() {
        if (currentUser == null) return;

        databaseRef.orderByChild("accepted").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rideList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Ride ride = postSnapshot.getValue(Ride.class);

                    if (ride != null) {
                        if (currentMode.equals("offer")) {
                            if (ride.rideType != null && ride.rideType.equals("offer") && ride.driverId != null && !ride.driverId.equals(currentUser.getUid())) {
                                rideList.add(ride);
                                rideKeys.add(postSnapshot.getKey());
                            }
                        }
                        if (currentMode.equals("request")) {
                            if (ride.rideType != null && ride.rideType.equals("request") && ride.riderId != null && !ride.riderId.equals(currentUser.getUid())) {
                                rideList.add(ride);
                                rideKeys.add(postSnapshot.getKey());
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentMode", currentMode);

        // Optional: preserve scroll position too
        int scrollPosition = ((LinearLayoutManager) othersPostsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        outState.putInt("scrollPosition", scrollPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentMode = savedInstanceState.getString("currentMode", "offer");

        if (currentMode.equals("offer")) {
            currentListHeader.setText("Other's Unaccepted Offers");
        } else {
            currentListHeader.setText("Other's Unaccepted Requests");
        }

        loadOthersPosts();

        int scrollPosition = savedInstanceState.getInt("scrollPosition", 0);
        othersPostsRecyclerView.scrollToPosition(scrollPosition);
    }
}
