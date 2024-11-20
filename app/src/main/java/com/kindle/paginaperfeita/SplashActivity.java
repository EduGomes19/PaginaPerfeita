package com.kindle.paginaperfeita;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.kindle.paginaperfeita.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);  // Define o layout da splash screen

        // Cria um Handler para executar o redirecionamento após 2 segundos
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cria uma Intent para iniciar a MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finaliza a SplashActivity para que o usuário não volte para ela
            }
        }, 2000);  // 2000 milissegundos = 2 segundos
    }
}
