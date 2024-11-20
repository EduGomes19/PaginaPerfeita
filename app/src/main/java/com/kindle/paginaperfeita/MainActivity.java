package com.kindle.paginaperfeita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final int PICK_PDF_REQUEST = 2;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ImageView pdfImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfImageView = findViewById(R.id.pdf_image);

        // Verifica se a permissão de leitura foi concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita a permissão se necessário
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }

        // Botão para abrir o PDF
        findViewById(R.id.open_pdf_button).setOnClickListener(view -> openPdfFile());

        // Botões para navegação nas páginas do PDF
        findViewById(R.id.prev_page_button).setOnClickListener(view -> showPreviousPage());
        findViewById(R.id.next_page_button).setOnClickListener(view -> showNextPage());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Função para abrir o arquivo PDF usando a Storage Access Framework (SAF)
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
                // Obtém o URI do PDF selecionado
                Uri uri = data.getData();
                if (uri != null) {
                    // Solicita permissão para acessar o arquivo selecionado
                    ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (fileDescriptor != null) {
                        pdfRenderer = new PdfRenderer(fileDescriptor);

                        // Verifica se o PDF contém páginas
                        if (pdfRenderer.getPageCount() > 0) {
                            showPage(0); // Mostra a primeira página
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao abrir o PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Função para mostrar uma página específica
    private void showPage(int pageIndex) {
        if (pdfRenderer != null && pageIndex >= 0 && pageIndex < pdfRenderer.getPageCount()) {
            // Fecha a página atual, se houver
            if (currentPage != null) {
                currentPage.close();
            }

            // Abre a nova página
            currentPage = pdfRenderer.openPage(pageIndex);

            // Cria um bitmap para renderizar a página
            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // Exibe o bitmap no ImageView
            pdfImageView.setImageBitmap(bitmap);
        }
    }

    // Função para mostrar a página anterior
    private void showPreviousPage() {
        if (pdfRenderer != null && currentPage != null) {
            int currentPageIndex = currentPage.getIndex();
            if (currentPageIndex > 0) {
                showPage(currentPageIndex - 1);
            }
        }
    }

    // Função para mostrar a próxima página
    private void showNextPage() {
        if (pdfRenderer != null && currentPage != null) {
            int currentPageIndex = currentPage.getIndex();
            if (currentPageIndex < pdfRenderer.getPageCount() - 1) {
                showPage(currentPageIndex + 1);
            }
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
