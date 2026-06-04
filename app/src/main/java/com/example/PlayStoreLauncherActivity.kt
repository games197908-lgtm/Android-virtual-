package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity

class PlayStoreLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
            if (intent != null) {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/games?hl=pt_BR"))
                storeIntent.setPackage("com.android.vending")
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(storeIntent)
            } else {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=apps"))
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(marketIntent)
            }
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://play.google.com/store/games%3Fhl%3Dpt_BR&ved=2ahUKEwiwhdHF0OWUAxWvt5UCHX8XDfwQFnoECBEQAQ&sqi=2&usg=AOvVaw3b2UlTlYQsc0DHI_2B_jbL"))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Erro ao abrir a Play Store: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        // Finish this wrapper activity immediately so only the Play Store is shown
        finish()
    }
}
