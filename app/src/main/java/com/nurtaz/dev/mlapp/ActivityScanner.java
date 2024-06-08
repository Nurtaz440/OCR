package com.nurtaz.dev.mlapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ActivityScanner extends AppCompatActivity {

    private ImageView captureImageView;
    private TextView resultTv;
    private Button snapBtn, detectBtn;
    private Bitmap imageViewBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureImageView = findViewById(R.id.ivCaptureImage);
        resultTv = findViewById(R.id.textViewResult);
        snapBtn = findViewById(R.id.btnSnap);
        detectBtn = findViewById(R.id.btnDetect);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (CheckSelfPermission()){
                  CaptureImage();
              }else {
                  RequestPermission();
              }
            }
        });
    }
    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageViewBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block:text.getTextBlocks() ){
                    String blockText = block.getText();
                    Point[] blockCorneroint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();

                    for (Text.Line line  : block.getLines()){
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();

                        for (Text.Element element : line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        //displaying results
                        resultTv.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Fail Detect Image",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean CheckSelfPermission(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA_SERVICE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void CaptureImage(){
        Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeImage.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeImage,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                CaptureImage();
            }else {
                Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageViewBitmap = (Bitmap) extras.get("data");
            captureImageView.setImageBitmap(imageViewBitmap);
        }
    }

    private void RequestPermission(){
        int PERMISSION_CODE = 100;
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.CAMERA
        },PERMISSION_CODE);
    }
}