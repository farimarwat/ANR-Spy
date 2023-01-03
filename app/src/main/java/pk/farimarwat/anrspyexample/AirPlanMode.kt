package pk.farimarwat.anrspyexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

const val TAG = "ANR SPY"
class AirPlanMode:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        for(i in 0..10){
            Log.i(TAG,"Number: $i")
            Thread.sleep(1000)
        }
    }
}