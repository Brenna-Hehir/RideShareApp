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

public class RidePostAdapter extends RecyclerView.Adapter<RidePostAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private List<String> rideKeys;
    private String mode;   // "MY_POSTS" or "OTHERS_POSTS"

    public RidePostAdapter(List<Ride> rideList, List<String> rideKeys, String mode) {
        this.rideList = rideList;
        this.rideKeys  = rideKeys;
        this.mode      = mode;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_layout, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        String key = rideKeys.get(position);

        holder.fromTextView.setText("From: " + ride.from);
        holder.toTextView.setText("To: " + ride.to);
        holder.dateTimeTextView.setText(ride.dateTime);

        if ("MY_POSTS".equals(mode)) {
            // show both Edit & Delete
            holder.actionButton.setText("Edit");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), RideFormActivity.class);
                intent.putExtra("rideKey",    key);
                intent.putExtra("rideType",   ride.rideType);
                intent.putExtra("from",       ride.from);
                intent.putExtra("to",         ride.to);
                intent.putExtra("dateTime",   ride.dateTime);
                v.getContext().startActivity(intent);
            });

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
            // OTHERS_POSTS: only Accept
            holder.actionButton.setText("Accept");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);

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

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromTextView, toTextView, dateTimeTextView;
        Button actionButton, deleteButton;

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
