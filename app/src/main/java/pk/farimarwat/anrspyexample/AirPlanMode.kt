package pk.farimarwat.anrspyexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import pk.farimarwat.AnrSpy.TAG

class AirPlanMode:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        for(i in 0..10){
            Log.i(TAG,"Number: $i")
            Thread.sleep(1000)
        }
    }
}