package com.example.acase.UI;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.acase.R;
import com.example.acase.UC.BigStringConvolizer;
import com.example.acase.databinding.ActivityUploadPdfactivityBinding;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;



import java.io.IOException;
import java.io.InputStream;


/**
 * PDF icon directs you to File storage of the phone. Upon selection, you will be redirected
 * to this page again. Upload button records the pdf.
 */
public class UploadPDFActivity extends AppCompatActivity {

    ActivityUploadPdfactivityBinding b;
    private static final int PICK_PDF_REQUEST_CODE = 123;
    BigStringConvolizer convolizer = BigStringConvolizer.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUploadPdfactivityBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        b.btnCancel.setOnClickListener(e -> {
            Intent intent = new Intent(UploadPDFActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        b.imgChoosePdf.setOnClickListener(e -> {
            launchFilePicker();
        });
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String filename = getFileName(uri);
                if (uri != null ) {
                    try {
                        String pdfText = readPdfAsString(uri);
                        // Use the pdfText string as needed
                        Log.d(TAG, "onActivityResult: " + pdfText);
                        b.txtPdfName.setText(filename);

                        b.btnUpload.setOnClickListener(e -> {
                            Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show();
                            convolizer.sendFile(pdfText);
                            Toast.makeText(this, "Finish!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UploadPDFActivity.this, ChatActivity.class);
                            startActivity(intent);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error reading PDF file", Toast.LENGTH_SHORT).show();
                    }
                }
                else Log.d(TAG, "onActivityResult: uri null");

            }
            else Log.d(TAG, "onActivityResult: data null");
        }


    }

    private String readPdfAsString(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        PdfReader reader = new PdfReader(inputStream);
        PdfDocument document = new PdfDocument(reader);

        StringBuilder text = new StringBuilder();
        for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
            text.append(PdfTextExtractor.getTextFromPage(document.getPage(pageNum)));
        }

        document.close();
        reader.close();
        inputStream.close();

        return text.toString();
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }




}