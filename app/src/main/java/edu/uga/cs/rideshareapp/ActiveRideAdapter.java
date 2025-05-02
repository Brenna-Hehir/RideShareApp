package edu.uga.cs.rideshareapp;

import android.graphics.Color;
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
import com.google.firebase.database.ServerValue;

import java.util.List;

/**
 * RecyclerView.Adapter implementation for displaying a list of active rides.
 * Allows users to confirm ride completion and updates Firebase accordingly.
 */
public class ActiveRideAdapter extends RecyclerView.Adapter<ActiveRideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private List<String> rideKeys; // Firebase keys matching each ride

    /**
     * Constructs a new ActiveRideAdapter.
     *
     * @param rideList  The list of active Ride objects to display
     * @param rideKeys  The list of Firebase keys corresponding to each ride
     */
    public ActiveRideAdapter(List<Ride> rideList, List<String> rideKeys) {
        this.rideList = rideList;
        this.rideKeys = rideKeys;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type.
     *
     * @param parent   The parent ViewGroup into which the new view will be added
     * @param viewType The view type of the new view
     * @return A new RideViewHolder
     */
    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.active_ride_item, parent, false);
        return new RideViewHolder(itemView);
    }

    /**
     * Binds the data to the view at the specified position.
     *
     * @param holder   The RideViewHolder to bind data to
     * @param position The position in the list to bind
     */
    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        boolean isDriver = ride.driverId != null && ride.driverId.equals(currentUser.getUid());
        boolean isRider = ride.riderId != null && ride.riderId.equals(currentUser.getUid());

        // Set Date/Time at Top
        holder.dateTimeTextView.setText(ride.dateTime);

        // Set Role and Points
        if (isDriver) {
            holder.roleTextView.setText("Driver");
            holder.pointsTextView.setText("+50 points");
            holder.pointsTextView.setTextColor(Color.parseColor("#008000")); // Green
        } else if (isRider) {
            holder.roleTextView.setText("Rider");
            holder.pointsTextView.setText("-50 points");
            holder.pointsTextView.setTextColor(Color.parseColor("#FF0000")); // Red
        }

        holder.fromTextView.setText("From: " + ride.from);
        holder.toTextView.setText("To: " + ride.to);

        // Determine if this user has already confirmed the ride
        boolean userAlreadyConfirmed = (isDriver && ride.driverConfirmed) || (isRider && ride.riderConfirmed);

        if (userAlreadyConfirmed) {
            holder.confirmButton.setText("Waiting for\n other user\n to confirm");
            holder.confirmButton.setEnabled(false);
        } else {
            holder.confirmButton.setText("Confirm Ride\n Completed");
            holder.confirmButton.setEnabled(true);

            // Set click listener to update confirmation status
            holder.confirmButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                String rideKey = rideKeys.get(adapterPosition);

                DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference("rides")
                        .child(rideKey);

                // Update the correct confirmation field
                if (isDriver) {
                    ride.driverConfirmed = true;
                } else if (isRider) {
                    ride.riderConfirmed = true;
                }

                rideRef.setValue(ride)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), "Confirmation recorded!", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(holder.getAdapterPosition());

                            // If both users confirmed, finalize the ride
                            if (ride.driverConfirmed && ride.riderConfirmed) {
                                finalizeCompletedRide(ride, adapterPosition);
                            }
                        });
            });
        }
    }

    /**
     * Returns the total number of items in the adapter.
     *
     * @return Number of rides in the list
     */
    @Override
    public int getItemCount() {
        return rideList.size();
    }

    /**
     * Finalizes a completed ride by updating user points and removing the ride from Firebase.
     *
     * @param ride     The Ride object that has been confirmed by both parties
     * @param rideKey The index of the ride in the adapter/rideKeys list
     */
    private void finalizeCompletedRide(Ride ride, int rideKey) {
        int ridePoints = 50;

        // Add driver points
        DatabaseReference driverPointsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(ride.driverId)
                .child("points");

        // Remove rider points
        DatabaseReference riderPointsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(ride.riderId)
                .child("points");

        driverPointsRef.setValue(ServerValue.increment(ridePoints));
        riderPointsRef.setValue(ServerValue.increment(-ridePoints));

        // Remove from Active Rides
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference("rides")
                .child(String.valueOf(rideKey));
        rideRef.removeValue();

    }

    /**
     * ViewHolder class for active ride list items.
     */
    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView, roleTextView, fromTextView, toTextView, pointsTextView;
        Button confirmButton;

        /**
         * Constructs a RideViewHolder and binds UI elements.
         *
         * @param itemView The item view layout inflated from XML
         */
        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            toTextView = itemView.findViewById(R.id.toTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
            confirmButton = itemView.findViewById(R.id.confirmButton);
        }
    }
}
