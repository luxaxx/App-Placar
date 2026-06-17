package br.com.senai.placarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.senai.placarapp.aplicativoplacar.PlacarApp
import br.com.senai.placarapp.ui.theme.PlacarAppTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlacarAppTheme {
                PlacarApp()
            }
        }
    }
}
