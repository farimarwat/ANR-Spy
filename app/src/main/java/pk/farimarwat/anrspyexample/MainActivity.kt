package pk.farimarwat.anrspyexample

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pk.farimarwat.anrspy.agent.ANRSpyAgent
import pk.farimarwat.anrspy.agent.ANRSpyListener
import pk.farimarwat.anrspy.agent.TAG
import pk.farimarwat.anrspy.annotations.TraceClass
import pk.farimarwat.anrspy.annotations.TraceMethod
import pk.farimarwat.anrspy.models.MethodModel

import pk.farimarwat.anrspyexample.databinding.ActivityMainBinding

@TraceClass(traceAllMethods = false)
class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var mReceiver:AirPlanMode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val firebaseinstance = FirebaseAnalytics.getInstance(this)
        mReceiver = AirPlanMode()
        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            registerReceiver(mReceiver,it)
        }
        val anrSpyAgent = ANRSpyAgent.Builder()
            .setSpyListener(object : ANRSpyListener {
                override fun onWait(ms: Long) {
                    //Log.e(TAG,"Waited: $ms")
                }

                override fun onReportAvailable(methodList: List<MethodModel>) {
                    if(methodList.isNotEmpty()){
                        Log.e(TAG,"Methods-------")
                        for(item in methodList){
                            Log.e(TAG,"Method: ${item.name} ElapsedTime: ${item.elapsedTime} Thread: ${item.thread.name}")
                        }
                        Log.e(TAG,"End Methods ----\n")
                    }

                }
                override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {

                }

                override fun onAnrDetected(details: String, stackTrace: Array<StackTraceElement>) {

                }
            })
            .setThrowException(false)
            .setTimeOut(5000)
            .enablePerformanceMatrix(true)
            .setFirebaseInstance(firebaseinstance)
            .build()
        anrSpyAgent.start()
        initGui()
    }
    fun initGui(){
        binding.btnMain.setOnClickListener {
            myLoop()
        }
        binding.btnService.setOnClickListener {
            startService(Intent(this,MyService::class.java))
        }
    }
    @TraceMethod
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