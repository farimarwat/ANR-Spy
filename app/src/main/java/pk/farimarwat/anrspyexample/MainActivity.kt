package pk.farimarwat.anrspyexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import com.google.firebase.crashlytics.FirebaseCrashlytics
import pk.farimarwat.anrspy.agent.ANRSpyAgent
import pk.farimarwat.anrspy.models.AppAction

import pk.farimarwat.anrspyexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var mReceiver:AirPlanMode

    //Anr Callback

    //End
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.startMethodTracing("my_profiler_data");
        setContentView(binding.root)

        //Firebase Analytics Instance
        mReceiver = AirPlanMode()

        val crashlytics = FirebaseCrashlytics.getInstance()
        val anrSpyAgent = ANRSpyAgent.Builder(this )
            .setTimeOut(3000) //How much time you consider it as anr
            .setTicker(100) // check after this each interval in ms
            .setFirebaseCrashLytics(crashlytics) // if set, it will log as non-fatel log in firebase
            .setAppAction(AppAction.AppActionExit)
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

    override fun onResume() {
        super.onResume()
        Debug.stopMethodTracing();
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}