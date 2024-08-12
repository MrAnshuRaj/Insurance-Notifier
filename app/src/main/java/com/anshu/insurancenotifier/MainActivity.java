package com.anshu.insurancenotifier;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView etOutput;
    Button selectpdf, goTOHome;
    private ImageView pdfThumbnail;
    private TextView pdfFileName;
    private String APIKEY = "AIzaSyDQCsffreD2Tg-qGIr76yaI8P5D2yzCOoQ";
    String fileName;
    Bitmap thumbnail;
    String date;
    ActivityResultLauncher<Intent> resultLauncher;
    final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue with notifications
                    createNotificationChannel();
                } else {
                    // Permission is denied. Notify the user
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });
    private static final String CHANNEL_ID = "InsuranceNotifier";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etOutput =findViewById(R.id.output);
        selectpdf = findViewById(R.id.selectPdfBtn);
        pdfThumbnail = findViewById(R.id.pdfThumbnail);
        pdfFileName = findViewById(R.id.pdfFileName);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askNotificationPermission();
        } else {
            createNotificationChannel();
        }
        goTOHome = findViewById(R.id.listPageBtn);
        goTOHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        });

        selectpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                    if (ActivityCompat.checkSelfPermission(
                            MainActivity.this,
                            android.Manifest.permission
                                    .READ_EXTERNAL_STORAGE)
                            != PackageManager
                            .PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        android.Manifest.permission
                                                .READ_EXTERNAL_STORAGE},
                                1);
                    } else {
                        selectPDF();
                    }
                }
                else
                {
                    selectPDF();
                }
            }

        });
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(
                            ActivityResult result)
                    {
                        // Initialize result data
                        Intent data = result.getData();
                        if (data != null) {
                            Uri sUri = data.getData();
                            fileName = getFileName(sUri);
                            pdfFileName.setText(fileName);
                            thumbnail = generatePdfThumbnail(sUri);

                            assert sUri != null;
                            readPdf(sUri);
                        }
                    }
                });
    }
    private void askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            createNotificationChannel();
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

            Toast.makeText(this, "Notification permission is required to receive alerts", Toast.LENGTH_SHORT).show();
        } else {

            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }
    private void readPdf(Uri uri)
    {
        final String[] text = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String parsedText = "";
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    PdfReader reader = new PdfReader(inputStream);
                    int n = reader.getNumberOfPages();

                    for (int i = 0; i < n; i++) {
                        parsedText += PdfTextExtractor.getTextFromPage(reader, i + 1).trim() +"\n";
                    }
                    text[0] =parsedText;
                    reader.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            etOutput.setText(text[0]);
                            date=gemini(text[0]);

                        }
                    });
                } catch (Exception e) {
                    System.out.println(e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }}).start();
    }
    private void selectPDF()
    {
        // Initialize intent
        Intent intent
                = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType("application/pdf");
        // Launch intent
        resultLauncher.launch(intent);
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        // check condition
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            selectPDF();
        }
        else {
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               selectPDF();
           }
           else {Toast
                   .makeText(getApplicationContext(),
                           "Permission Denied",
                           Toast.LENGTH_SHORT)
                   .show();}

        }
    }
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String displayName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return displayName;
    }
    private void saveToFirebase(String pdfName,String renewalDate, Bitmap thumbnail) throws ParseException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        PDFData pdfData = new PDFData(pdfName, renewalDate);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        db.collection("users").document(auth.getCurrentUser().getUid()).
        collection("pdfs").add(pdfData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firebase", "Error adding document", e);
                });
        SharedPreferences sharedPreferences = getSharedPreferences("InsurancePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("pdfName", pdfName);
        editor.putString("renewalDate", renewalDate);
        editor.apply();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Date sevenDaysBefore,threeDaysBefore,oneDayBefore;
        Date renewal = sdf.parse(renewalDate);
        calendar.setTime(renewal);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        sevenDaysBefore = calendar.getTime();

        calendar.setTime(renewal);
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        threeDaysBefore = calendar.getTime();

        calendar.setTime(renewal);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        oneDayBefore = calendar.getTime();

        scheduleNotification(MainActivity.this, sevenDaysBefore, "Renewal in 7 days!", 1);
        scheduleNotification(MainActivity.this, threeDaysBefore, "Renewal in 3 days!", 2);
        scheduleNotification(MainActivity.this, oneDayBefore, "Renewal in 1 day!", 3);
    }
    private void scheduleNotification(Context context, Date notifyDate, String contentText, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("contentText", contentText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyDate.getTime(), pendingIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "InsuranceNotifier";
            String description = "Channel for PDF renewal reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("MyChannelId", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @SuppressLint("RestrictedApi")
    private Bitmap generatePdfThumbnail(Uri pdfUri) {
        try {
            // Copy the file to the app's cache directory
            File file = new File(getCacheDir(), getFileName(pdfUri));
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            PdfRenderer.Page page = pdfRenderer.openPage(0);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfThumbnail.setImageBitmap(bitmap);

            page.close();
            pdfRenderer.close();
            fileDescriptor.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            Log.e("PDF_THUMBNAIL_ERROR", "Error generating PDF thumbnail: " + e.getMessage());
        }
        return null;
    }
    private String gemini(String pdfContent)
    {
        GenerativeModel gm = new GenerativeModel( "gemini-1.5-flash",APIKEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText("What is the insurance due date/end date in the following text in the format \"yyyy-MM-dd\" :"+pdfContent+"\njust respond with date no paragraph")
                .build();


        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    date = resultText;
                    try {
                        saveToFirebase(fileName,date,thumbnail);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    etOutput.setText(resultText);
                    // gotResponse(resultText);
                    System.out.println(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace(System.out);
                }
            }, this.getMainExecutor());
        }
        return date;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}