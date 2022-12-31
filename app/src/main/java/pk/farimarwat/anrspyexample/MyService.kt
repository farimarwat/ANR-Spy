package pk.farimarwat.anrspyexample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import pk.farimarwat.anrspy.agent.TAG
import pk.farimarwat.anrspy.annotations.TraceClass
import pk.farimarwat.anrspy.annotations.TraceMethod

@TraceClass(traceAllMethods = false)
class MyService:Service() {
    @TraceMethod
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        for(i in 0..10){
            Log.i(TAG,"Number: $i")
            Thread.sleep(1000)
        }
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}