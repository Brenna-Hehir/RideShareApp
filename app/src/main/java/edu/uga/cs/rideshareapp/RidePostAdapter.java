package edu.uga.cs.rideshareapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RidePostAdapter extends RecyclerView.Adapter< RidePostAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private String mode;   // "MY_POSTS or OTHERS_POSTS

    public RidePostAdapter(List<Ride> rideList, String mode) {
        this.rideList = rideList;
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
                // TODO - Accept logic
            });
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
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
