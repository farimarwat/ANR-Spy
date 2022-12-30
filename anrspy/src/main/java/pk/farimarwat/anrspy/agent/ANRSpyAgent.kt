package pk.farimarwat.anrspy.agent

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

class ANRSpyAgent constructor(builder: Builder): Thread() {

    //Params
    private var mListener: ANRSpyListener? = null
    private var mShouldThrowException:Boolean = true
    private var TIME_OUT = 5000L
    private var mEnablePerformanceMatrix:Boolean = false

    //

    private val INTERVAL = 500L
    private val mHandler = Handler(Looper.getMainLooper())
    private var _timeWaited = 0L
    private val _mTesterWorker = Runnable {
        _timeWaited = 0L
    }
    init {
        this.mListener = builder.getSpyListener()
        this.mShouldThrowException = builder.getThrowException()
        this.TIME_OUT = builder.getTimeOout()
        this.mEnablePerformanceMatrix = builder.getPerformanceMatrix()

    }
    //Builder
    class Builder(){
        //Params
        private var mListener: ANRSpyListener? = null
        private var mShouldThrowException:Boolean = true
        private var TIME_OUT = 5000L
        private var mEnablePerformanceMatrix:Boolean = false


        //
        fun setSpyListener(listener: ANRSpyListener) = apply { this.mListener = listener }
        fun getSpyListener() = this.mListener

        fun setThrowException(throwexception:Boolean) = apply { this.mShouldThrowException = throwexception }
        fun getThrowException() = this.mShouldThrowException

        fun setTimeOut(timeout:Long) = apply { TIME_OUT = timeout }
        fun getTimeOout() = TIME_OUT

        fun enablePerformanceMatrix(enable:Boolean) = apply { mEnablePerformanceMatrix = enable}
        fun getPerformanceMatrix() = this.mEnablePerformanceMatrix



        fun build(): ANRSpyAgent = ANRSpyAgent(this)
    }
    //End builder

    override fun run() {
        while (!isInterrupted){
            _timeWaited += INTERVAL
            mListener?.onWait(_timeWaited)
            mHandler.post(_mTesterWorker)
            sleep(INTERVAL)
            if(_timeWaited > TIME_OUT){
                mListener?.onAnrDetected("$THREAD_TITLE Main thread blocked for: $_timeWaited ms",Looper.getMainLooper().thread.stackTrace)
                if(mShouldThrowException){
                    throwException(Looper.getMainLooper().thread.stackTrace)
                }
            }
            if(mEnablePerformanceMatrix){
                performanceMatrix()
            }
        }

    }

    private fun throwException(stackTrace: Array<StackTraceElement>) {
        throw ANRSpyException(THREAD_TITLE, stackTrace)
    }

    fun performanceMatrix(){
        val allstacktrace = getAllStackTraces()
        for(entity in allstacktrace){
            val thread = entity.key
            if(thread.name == "main"){
                Log.e(TAG,"[ ++ ANR Spy ++ ---- Start Performance Matrix ----]")
                for(element in entity.value){
                    Log.e(TAG,"ClassName: ${element.className} Method: ${element.methodName}")
                }
                Log.e(TAG,"[ ++ ANR Spy ++ ---- End Performance Matrix ----]")
                Log.e(TAG,"\n")
            }
        }

    }
}