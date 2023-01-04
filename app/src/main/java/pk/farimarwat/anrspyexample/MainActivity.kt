package pk.farimarwat.anrspyexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import pk.farimarwat.anrspy.agent.ANRSpyAgent

import pk.farimarwat.anrspy.agent.ANRSpyListener
import pk.farimarwat.anrspy.agent.TAG
import pk.farimarwat.anrspy.annotations.TraceClass
import pk.farimarwat.anrspy.annotations.TraceMethod
import pk.farimarwat.anrspy.models.MethodModel

import pk.farimarwat.anrspyexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var mReceiver:AirPlanMode

    //Anr Callback
    private var mCallback = object : ANRSpyListener {
        override fun onWait(ms: Long) {

        }
        override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {

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
        override fun onAnrDetected(
            details: String,
            stackTrace: Array<StackTraceElement>,
            packageMethods: List<String>?
        ) {
           packageMethods?.let {
               Log.e(TAG,"-----ANR Detected-------")
               it.forEach {
                   Log.e(TAG,it)
               }
               Log.e(TAG,"-------------------")
           }
        }
    }

    //End
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Firebase Analytics Instance
        val firebaseinstance = FirebaseAnalytics.getInstance(this)

        mReceiver = AirPlanMode()

        val anrSpyAgent = ANRSpyAgent.Builder(this  )
            .setTimeOut(5000)
            .setSpyListener(mCallback)
            .setThrowException(false)
            .enableReportAnnotatedMethods(true)
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