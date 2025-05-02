package edu.uga.cs.rideshareapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RideFormActivity extends AppCompatActivity {

    private EditText startingPointInput, destinationInput, dateInput, timeInput;
    private RadioGroup rideTypeGroup;
    private RadioButton rideOfferButton, rideRequestButton;
    private Button postRideButton;

    private String rideKey;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        startingPointInput = findViewById(R.id.startingPoint);
        destinationInput   = findViewById(R.id.destination);
        dateInput          = findViewById(R.id.date);
        timeInput          = findViewById(R.id.time);
        rideTypeGroup      = findViewById(R.id.rideType);
        rideOfferButton    = findViewById(R.id.rideOffer);
        rideRequestButton  = findViewById(R.id.rideRequest);
        postRideButton     = findViewById(R.id.postRideButton);

        // check for edit mode
        Intent intent = getIntent();
        if (intent.hasExtra("rideKey")) {
            isEdit   = true;
            rideKey  = intent.getStringExtra("rideKey");
            postRideButton.setText("Save Changes");

            String rideType = intent.getStringExtra("rideType");
            if ("offer".equals(rideType)) {
                rideOfferButton.setChecked(true);
            } else {
                rideRequestButton.setChecked(true);
            }

            startingPointInput.setText(intent.getStringExtra("from"));
            destinationInput.setText(intent.getStringExtra("to"));

            String dateTime = intent.getStringExtra("dateTime"); // "MM-dd-yyyy hh:mm a"
            String[] parts = dateTime.split(" ", 2);
            dateInput.setText(parts[0]);
            timeInput.setText(parts[1]);
        }

        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setOnClickListener(v -> showTimePicker());
        postRideButton.setOnClickListener(v -> submitRideForm());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        dateInput.setText(String.format("%02d-%02d-%04d", month+1, day, year)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    int hour = (hourOfDay == 0 ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay));
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    timeInput.setText(String.format("%02d:%02d %s", hour, minute, amPm));
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void submitRideForm() {
        String from = startingPointInput.getText().toString().trim();
        String to   = destinationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int checked = rideTypeGroup.getCheckedRadioButtonId();

        if (checked == -1 ||
                from.isEmpty() ||
                to.isEmpty() ||
                date.isEmpty() ||
                time.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // future date/time check
        String dtStr = date + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a", Locale.getDefault());
        try {
            Date sel = sdf.parse(dtStr);
            if (sel != null && sel.before(new Date())) {
                Toast.makeText(this,
                        "Please select a date/time in the future.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this,
                    "Invalid date/time format.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid       = user.getUid();
        String email     = user.getEmail();
        String rideType  = (checked == R.id.rideOffer) ? "offer" : "request";

        Ride ride = new Ride(
                rideType,
                rideType.equals("offer") ? uid    : null,
                rideType.equals("request") ? uid  : null,
                from,
                to,
                dtStr,
                false,   // accepted
                false,   // driverConfirmed
                false,   // riderConfirmed
                rideType.equals("offer") ? email   : null,
                rideType.equals("request") ? email : null
        );

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("rides");

        if (isEdit) {
            dbRef.child(rideKey)
                    .setValue(ride)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "Ride updated",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Update failed",
                                    Toast.LENGTH_SHORT).show()
                    );
        } else {
            dbRef.push()
                    .setValue(ride)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "Ride posted successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Post failed",
                                    Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save user input
        outState.putString("startingPoint", startingPointInput.getText().toString());
        outState.putString("destination", destinationInput.getText().toString());
        outState.putString("date", dateInput.getText().toString());
        outState.putString("time", timeInput.getText().toString());

        // Save selected ride type (RadioGroup)
        int selectedRideTypeId = rideTypeGroup.getCheckedRadioButtonId();
        outState.putInt("selectedRideTypeId", selectedRideTypeId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore user input
        startingPointInput.setText(savedInstanceState.getString("startingPoint", ""));
        destinationInput.setText(savedInstanceState.getString("destination", ""));
        dateInput.setText(savedInstanceState.getString("date", ""));
        timeInput.setText(savedInstanceState.getString("time", ""));

        // Restore selected radio button
        int selectedRideTypeId = savedInstanceState.getInt("selectedRideTypeId", -1);
        if (selectedRideTypeId != -1) {
            rideTypeGroup.check(selectedRideTypeId);
        }
    }


}
