package edu.uga.cs.rideshareapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RidePostAdapter extends RecyclerView.Adapter< RidePostAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private List<String> rideKeys;
    private String mode;   // "MY_POSTS or OTHERS_POSTS

    public RidePostAdapter(List<Ride> rideList, List<String> rideKeys, String mode) {
        this.rideList = rideList;
        this.rideKeys = rideKeys;
        this.mode = mode;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item_layout, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);

        holder.fromTextView.setText(ride.from);
        holder.toTextView.setText(ride.to);
        holder.dateTimeTextView.setText(ride.dateTime);

        if (mode.equals("MY_POSTS")) {
            holder.actionButton.setText("Edit");
            holder.actionButton.setOnClickListener(v -> {
               // TODO - Edit logic
            });
        } else if (mode.equals("OTHERS_POSTS")) {
            holder.actionButton.setText("Accept");
            holder.actionButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // Update the ride object accordingly based on if it is an offer or request
                    if (ride.rideType != null && ride.rideType.equals("offer")) {
                        ride.riderId = currentUser.getUid();
                        ride.riderEmail = currentUser.getEmail();
                    } else if (ride.rideType != null && ride.rideType.equals("request")) {
                        ride.driverId = currentUser.getUid();
                        ride.driverEmail = currentUser.getEmail();
                    }
                    ride.accepted = true;

                    DatabaseReference rideRef = FirebaseDatabase.getInstance()
                            .getReference("rides")
                            .child(postSnapshotKey(holder.getAdapterPosition()));

                    rideRef.setValue(ride)
                            .addOnSuccessListener(aVoid -> {
                                int key = holder.getAdapterPosition();
                                rideList.remove(key);
                                rideKeys.remove(key);
                                notifyItemRemoved(key);
                                notifyItemRangeChanged(key, rideList.size());

                                Toast.makeText(holder.itemView.getContext(), "Ride accepted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "Failed to accept ride.", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    private String postSnapshotKey(int position) {
        return rideKeys.get(position);
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromTextView, toTextView, dateTimeTextView;
        Button actionButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            toTextView = itemView.findViewById(R.id.toTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}
