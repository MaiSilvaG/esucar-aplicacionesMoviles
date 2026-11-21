//cXyVNka3PfeD37Sh contraseña base de datos
//
package com.example.esucar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.esucar.data.Sesion
import com.example.esucar.ui.view.navigation.AppNavHost
import com.example.esucar.ui.theme.EsucarTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sesion.init(this)
        enableEdgeToEdge()
        setContent {
            EsucarTheme{
                AppNavHost()
            }
        }
    }
}

