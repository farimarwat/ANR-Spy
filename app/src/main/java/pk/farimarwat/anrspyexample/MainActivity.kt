package pk.farimarwat.anrspyexample

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import pk.farimarwat.AnrSpy.ANRSpyAgent
import pk.farimarwat.AnrSpy.ANRSpyListener
import pk.farimarwat.AnrSpy.TAG

import pk.farimarwat.anrspyexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var mReceiver:AirPlanMode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mReceiver = AirPlanMode()
        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            registerReceiver(mReceiver,it)
        }
        val anrSpyAgent = ANRSpyAgent.Builder()
            .setSpyListener(object : ANRSpyListener {
                override fun onWait(ms: Long) {
                    //Log.e(TAG,"Waited: $ms")
                }

                override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {
                    Log.e(TAG,"Stack:\n ${stackstrace}")
                }

                override fun onAnrDetected(details: String, stackTrace: Array<StackTraceElement>) {
                    Log.e(TAG,details)
                    Log.e(TAG,"${stackTrace}")
                }
            })
            .setThrowException(true)
            .setTimeOut(5000)
            .build()
        anrSpyAgent.start()
        initGui()
    }
    fun initGui(){
        binding.btnMain.setOnClickListener {
            for(i in 1..10){
                Log.e(TAG,"Number: $i")
                Thread.sleep(1000)
            }
        }
        binding.btnService.setOnClickListener {
            startService(Intent(this,MyService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}