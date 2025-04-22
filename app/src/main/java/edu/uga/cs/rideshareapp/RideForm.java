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

import java.util.Calendar;

public class RideForm extends AppCompatActivity {

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
                RideForm.this,
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
                RideForm.this,
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
        // TODO: Implement this class - add the post to the database etc
        Toast.makeText(RideForm.this, "Post Ride button clicked!", Toast.LENGTH_SHORT).show();
    }

}