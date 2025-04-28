package edu.uga.cs.rideshareapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startingPointInput = findViewById(R.id.startingPoint);
        destinationInput = findViewById(R.id.destination);
        dateInput = findViewById(R.id.date);
        timeInput = findViewById(R.id.time);
        rideTypeGroup = findViewById(R.id.rideType);
        rideOfferButton = findViewById(R.id.rideOffer);
        rideRequestButton = findViewById(R.id.rideRequest);
        postRideButton = findViewById(R.id.postRideButton);

        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        timeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        postRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRideForm();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog (
                RideFormActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = String.format("%02d-%02d-%04d", month + 1, dayOfMonth, year);
                        dateInput.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                RideFormActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime;
                        int hour = hourOfDay;
                        String amPm;

                        if (hourOfDay >= 12) {
                            amPm = "PM";
                            if (hourOfDay > 12) {
                                hour = hourOfDay - 12;
                            }
                        } else {
                            amPm = "AM";
                            if (hourOfDay == 0) {
                                hour = 12;
                            }
                        }

                        selectedTime = String.format("%02d:%02d %s", hour, minute, amPm);
                        timeInput.setText(selectedTime);
                    }
                },
                hour, minute, false
        );
        timePickerDialog.show();
    }

    private void submitRideForm() {
        String startingPoint = startingPointInput.getText().toString().trim();
        String destination = destinationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int rideTypeId = rideTypeGroup.getCheckedRadioButtonId();

        // making sure all fields are filled out
        if (rideTypeId == -1) {
            Toast.makeText(this, "Please select Ride Offer or Ride Request.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startingPoint.isEmpty()) {
            Toast.makeText(this, "Please enter a starting point.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter a destination.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.isEmpty()) {
            Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // making sure the date and time selected is in the future
        String dateTimeStr = date + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a", Locale.getDefault());
        try {
            Date selectedDateTime = sdf.parse(dateTimeStr);
            Date currentDateTime = new Date();

            if (selectedDateTime != null && selectedDateTime.before(currentDateTime)) {
                Toast.makeText(this, "Please select a date and time in the future.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date or time format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // setup Firebase push
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        String rideType = (rideTypeId == R.id.rideOffer) ? "offer" : "request";

        // prepare ride information
        Ride ride = new Ride(
                rideType,
                rideType.equals("offer") ? uid : null,      // driverId
                rideType.equals("request") ? uid : null,    // riderId
                startingPoint,
                destination,
                date + " " + time,
                false,                              // accepted
                false,                                       // driverConfirmed
                false,                                       // riderConfirmed
                rideType.equals("offer") ? email : null,     // driverEmail
                rideType.equals("request") ? email : null    // riderEmail
        );

        // Push to Firebase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("rides");
        databaseRef.push().setValue(ride);

        Toast.makeText(this, "Ride posted successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}