package pk.farimarwat.anrspy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pk.farimarwat.anrspy.ANRSpyConstants.EXTRA_DETAILS

class LoggerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logger)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadViews(intent)
    }

    fun loadViews(intent: Intent){
        val details = intent.getStringExtra(EXTRA_DETAILS)
        findViewById<TextView>(R.id.txtDetails)
            .text = details
        findViewById<Button>(R.id.btnClose)
            .setOnClickListener {
                finishAndRemoveTask()
                System.exit(0)
            }
    }
}