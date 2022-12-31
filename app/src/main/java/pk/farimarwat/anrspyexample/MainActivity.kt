package pk.farimarwat.anrspyexample

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pk.farimarwat.anrspy.agent.ANRSpyAgent
import pk.farimarwat.anrspy.agent.ANRSpyListener
import pk.farimarwat.anrspy.agent.TAG

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

                }

                override fun onAnrDetected(details: String, stackTrace: Array<StackTraceElement>) {

                }
            })
            .setThrowException(false)
            .setTimeOut(5000)
            .enablePerformanceMatrix(true)
            .build()
        anrSpyAgent.start()
        initGui()
    }
    fun initGui(){
        CoroutineScope(Dispatchers.IO).launch{
            myLoop()
        }
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
    fun myLoop(){
        for(i in 0..10){
            Thread.sleep(1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}