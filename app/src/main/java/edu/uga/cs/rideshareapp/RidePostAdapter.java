package edu.uga.cs.rideshareapp;

import android.content.Intent;
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

/**
 * RecyclerView.Adapter for displaying rides in either "My Posts" or "Others' Posts" views.
 * Supports editing and deleting rides (for user's own posts), or accepting rides (from others).
 */
public class RidePostAdapter extends RecyclerView.Adapter<RidePostAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private List<String> rideKeys;
    private String mode;   // "MY_POSTS" or "OTHERS_POSTS"

    /**
     * Constructor for RidePostAdapter.
     *
     * @param rideList  List of Ride objects to display
     * @param rideKeys  Corresponding Firebase keys for each ride
     * @param mode      Display mode ("MY_POSTS" or "OTHERS_POSTS")
     */
    public RidePostAdapter(List<Ride> rideList, List<String> rideKeys, String mode) {
        this.rideList = rideList;
        this.rideKeys  = rideKeys;
        this.mode      = mode;
    }

    /**
     * Inflates the layout for each ride item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup
     * @param viewType View type (not used here)
     * @return RideViewHolder instance
     */
    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_layout, parent, false);
        return new RideViewHolder(view);
    }

    /**
     * Binds ride data to the view holder, and sets up appropriate button actions.
     *
     * @param holder   The RideViewHolder
     * @param position The position in the data list
     */
    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        String key = rideKeys.get(position);

        holder.fromTextView.setText("From: " + ride.from);
        holder.toTextView.setText("To: " + ride.to);
        holder.dateTimeTextView.setText(ride.dateTime);

        if ("MY_POSTS".equals(mode)) {
            // User's own posts: show Edit and Delete
            holder.actionButton.setText("Edit");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            // Edit ride: opens RideFormActivity with prefilled data
            holder.actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), RideFormActivity.class);
                intent.putExtra("rideKey",    key);
                intent.putExtra("rideType",   ride.rideType);
                intent.putExtra("from",       ride.from);
                intent.putExtra("to",         ride.to);
                intent.putExtra("dateTime",   ride.dateTime);
                v.getContext().startActivity(intent);
            });

            // Delete ride from Firebase and remove from adapter
            holder.deleteButton.setOnClickListener(v -> {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("rides")
                        .child(key);
                ref.removeValue()
                        .addOnSuccessListener(a -> {
                            rideList.remove(position);
                            rideKeys.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, rideList.size());
                            Toast.makeText(v.getContext(),
                                    "Ride deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Delete failed", Toast.LENGTH_SHORT).show()
                        );
            });

        } else {
            // Other users' posts: show Accept button only
            holder.actionButton.setText("Accept");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);

            // Accept ride: current user becomes rider (if it's an offer) or driver (if it's a request)
            holder.actionButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) return;

                if ("offer".equals(ride.rideType)) {
                    ride.riderId    = currentUser.getUid();
                    ride.riderEmail = currentUser.getEmail();
                } else {
                    ride.driverId    = currentUser.getUid();
                    ride.driverEmail = currentUser.getEmail();
                }
                ride.accepted = true;

                DatabaseReference rideRef = FirebaseDatabase.getInstance()
                        .getReference("rides")
                        .child(rideKeys.get(holder.getAdapterPosition()));
                rideRef.setValue(ride)
                        .addOnSuccessListener(a -> {
                            int idx = holder.getAdapterPosition();
                            rideList.remove(idx);
                            rideKeys.remove(idx);
                            notifyItemRemoved(idx);
                            notifyItemRangeChanged(idx, rideList.size());
                            Toast.makeText(v.getContext(),
                                    "Ride accepted!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Failed to accept ride.", Toast.LENGTH_SHORT).show()
                        );
            });
        }
    }

    /**
     * Returns the total number of ride items in the adapter.
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        return rideList.size();
    }

    /**
     * ViewHolder class that holds view references for a single ride item.
     */
    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromTextView, toTextView, dateTimeTextView;
        Button actionButton, deleteButton;

        /**
         * Constructs a RideViewHolder and binds UI components.
         *
         * @param itemView The inflated item layout
         */
        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            fromTextView     = itemView.findViewById(R.id.fromTextView);
            toTextView       = itemView.findViewById(R.id.toTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            actionButton     = itemView.findViewById(R.id.actionButton);
            deleteButton     = itemView.findViewById(R.id.deleteButton);
        }
    }
}
