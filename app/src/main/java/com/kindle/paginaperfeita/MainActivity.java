package com.kindle.paginaperfeita;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final int PICK_PDF_REQUEST = 2;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ImageView pdfImageView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfImageView = findViewById(R.id.pdf_image);
        gestureDetector = new GestureDetector(this, new GestureListener());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }
        findViewById(R.id.open_pdf_button).setOnClickListener(view -> openPdfFile());
    }
    private void openPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                if (uri != null) {
                    ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (fileDescriptor != null) {
                        pdfRenderer = new PdfRenderer(fileDescriptor);

                        if (pdfRenderer.getPageCount() > 0) {
                            showPage(0);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao abrir o PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showPage(int pageIndex) {
        if (pdfRenderer != null && pageIndex >= 0 && pageIndex < pdfRenderer.getPageCount()) {
            if (currentPage != null) {
                currentPage.close();
            }
            currentPage = pdfRenderer.openPage(pageIndex);
            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfImageView.setImageBitmap(bitmap);
        }
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            if (e2.getX() > e1.getX()) {
                showPreviousPage();
            }
            else if (e2.getX() < e1.getX()) {
                showNextPage();
            }
            return true;
        }
    }
    private void showPreviousPage() {
        if (pdfRenderer != null && currentPage != null) {
            int currentPageIndex = currentPage.getIndex();
            if (currentPageIndex > 0) {
                showPage(currentPageIndex - 1);
            }
        }
    }
    private void showNextPage() {
        if (pdfRenderer != null && currentPage != null) {
            int currentPageIndex = currentPage.getIndex();
            if (currentPageIndex < pdfRenderer.getPageCount() - 1) {
                showPage(currentPageIndex + 1);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
    }
}
