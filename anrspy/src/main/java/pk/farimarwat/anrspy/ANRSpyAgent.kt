package pk.farimarwat.AnrSpy

import android.os.Handler
import android.os.Looper

class ANRSpyAgent constructor(builder:Builder): Thread() {

    //Params
    private var mListener:ANRSpyListener? = null
    private var mShouldThrowException:Boolean = true
    private var TIME_OUT = 5000L
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
    }
    //Builder
    class Builder(){
        //Params
        private var mListener:ANRSpyListener? = null
        private var mShouldThrowException:Boolean = true
        private var TIME_OUT = 5000L
        //
        fun setSpyListener(listener: ANRSpyListener) = apply { this.mListener = listener }
        fun getSpyListener() = this.mListener

        fun setThrowException(throwexception:Boolean) = apply { this.mShouldThrowException = throwexception }
        fun getThrowException() = this.mShouldThrowException

        fun setTimeOut(timeout:Long) = apply { TIME_OUT = timeout }
        fun getTimeOout() = TIME_OUT

        fun build():ANRSpyAgent = ANRSpyAgent(this)
    }
    //End builder

    override fun run() {
        while (!isInterrupted){
            _timeWaited += INTERVAL
            mListener?.onWait(_timeWaited)
            mHandler.post(_mTesterWorker)
            sleep(INTERVAL)
            if(_timeWaited > TIME_OUT){
                mListener?.onAnrDetected(THREAD_TITLE+" Main thread blocked for: $_timeWaited ms",Looper.getMainLooper().thread.stackTrace)
                if(mShouldThrowException){
                    throwException(Looper.getMainLooper().thread.stackTrace)
                }
            }
        }

    }

    private fun throwException(stackTrace: Array<StackTraceElement>) {
        throw ANRSpyException(THREAD_TITLE, stackTrace)
    }
}