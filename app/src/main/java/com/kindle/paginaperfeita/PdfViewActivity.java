package com.kindle.paginaperfeita;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PdfViewActivity extends AppCompatActivity {

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ImageView pdfImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        pdfImageView = findViewById(R.id.pdf_image);

        Uri pdfUri = getIntent().getData();
        if (pdfUri != null) {
            openPdf(pdfUri);
        } else {
            Toast.makeText(this, "Erro ao abrir o PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openPdf(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
                if (pdfRenderer.getPageCount() > 0) {
                    showPage(0); // Exibe a primeira página
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar o PDF", Toast.LENGTH_SHORT).show();
            finish();
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
