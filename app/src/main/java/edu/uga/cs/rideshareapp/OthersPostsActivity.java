package edu.uga.cs.rideshareapp;

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

        othersPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideList = new ArrayList<>();
        adapter = new RidePostAdapter(rideList, "OTHERS_POSTS"); // mode
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
                            }
                        }
                        if (currentMode.equals("request")) {
                            if (ride.rideType != null && ride.rideType.equals("request") && ride.riderId != null && !ride.riderId.equals(currentUser.getUid())) {
                                rideList.add(ride);
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
}
