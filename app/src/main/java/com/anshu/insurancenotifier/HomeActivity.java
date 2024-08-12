package com.anshu.insurancenotifier;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView schemesRecyclerView;
    private SchemeAdapter schemeAdapter;
    private List<Scheme> schemeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        schemesRecyclerView = findViewById(R.id.schemesRecyclerView);
        schemesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        schemeList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        db.collection("users").document(auth.getCurrentUser().getUid()).collection("pdfs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name="anshu",date="2023-12-12";
                        if (document.contains("name") && document.get("name") instanceof String) {
                            name = document.getString("name");
                            Toast.makeText(HomeActivity.this, "name: "+name, Toast.LENGTH_SHORT).show();
                        }
                        if (document.contains("renewalDate") && document.get("renewalDate") instanceof String) {
                            date =document.getString("renewalDate");
                            Toast.makeText(HomeActivity.this, "date: "+date, Toast.LENGTH_SHORT).show();
                        }

                        schemeList.add(new Scheme(name,date,daysUntil(date)));
                        schemeAdapter = new SchemeAdapter(schemeList);
                        schemesRecyclerView.setAdapter(schemeAdapter);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
    public static int daysUntil(String dateString) {
        // Define the date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Parse the input string into a Date object
            Date targetDate = sdf.parse(dateString);

            // Get the current date
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
            currentCalendar.set(Calendar.MINUTE, 0);
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);

            // Get the target date
            Calendar targetCalendar = Calendar.getInstance();
            targetCalendar.setTime(targetDate);
            targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
            targetCalendar.set(Calendar.MINUTE, 0);
            targetCalendar.set(Calendar.SECOND, 0);
            targetCalendar.set(Calendar.MILLISECOND, 0);

            // Calculate the difference in milliseconds
            long differenceInMillis = targetCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

            // Convert the difference from milliseconds to days
            int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(differenceInMillis);

            return daysRemaining;

        } catch (ParseException e) {
            e.printStackTrace();
            // Return a special value (e.g., -1) if there was a problem with parsing
            return -1;
        }
    }
}