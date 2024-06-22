package pk.farimarwat.anrspy.agent

import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import pk.farimarwat.anrspy.models.MethodModel

@RequiresApi(Build.VERSION_CODES.M)
class ANRSpyAgent constructor(builder: Builder) : Thread() {
    private var mContext: Context

    //Params
    private var mShouldThrowException: Boolean = false
    private var TIME_OUT = 5000L
    private var mTicker: Long
    private var mPreviousMethod = ""
    private var mReported = ""
    private var mDuration = 0L

    private var mFirebaseCrashlytics: FirebaseCrashlytics? = null
    private var listMethodsExcluded = mutableListOf("nativePollOnce")

    init {
        this.mShouldThrowException = builder.getThrowException()
        this.TIME_OUT = builder.getTimeOout()
        this.mFirebaseCrashlytics = builder.getFirebaseInstance()
        this.mContext = builder.mContext
        this.mTicker = builder.getTicker()
    }

    //Builder
    class Builder(var mContext: Context) {
        //Params
        private var mShouldThrowException: Boolean = false
        private var TIME_OUT = 5000L
        private var mTicker = 200L
        private var mFirebaseCrashlytics: FirebaseCrashlytics? = null


        fun setThrowException(throwexception: Boolean) =
            apply { this.mShouldThrowException = throwexception }

        fun getThrowException() = this.mShouldThrowException

        fun setTimeOut(timeout: Long) = apply { TIME_OUT = timeout }
        fun getTimeOout() = TIME_OUT

        fun setTicker(ticker: Long) = apply { mTicker = ticker }

        fun getTicker() = this.mTicker


        fun setFirebaseCrashLytics(instance: FirebaseCrashlytics) =
            apply { this.mFirebaseCrashlytics = instance }

        fun getFirebaseInstance() = this.mFirebaseCrashlytics


        fun build(): ANRSpyAgent = ANRSpyAgent(this)
    }
    //End builder

    override fun run() {
        while (!isInterrupted) {
            val stacktrace = Looper.getMainLooper().thread.stackTrace
            val method = stacktrace.firstOrNull()?.methodName ?: ""
            if(method != mPreviousMethod){
                mPreviousMethod = method
                mDuration = 0
            } else {
                mDuration += mTicker
            }
            try {
                sleep(mTicker)
            }catch (ex:Exception){
                Log.e(TAG,ex.toString())
            }
            if(mDuration >  TIME_OUT && method !in listMethodsExcluded){
               if(mReported != mPreviousMethod){
                   mReported = mPreviousMethod
                   processANR(stacktrace)
               }
            }
        }

    }


    private fun throwException(msg:String,stackTrace: Array<StackTraceElement>) {
        throw ANRSpyException(msg, stackTrace)
    }

    fun processANR(stackTrace: Array<StackTraceElement>){
        var msg = "Method: ${stackTrace.firstOrNull()?.methodName}"
        msg += " is blocking main thread for at least ${mDuration} ms"
        mFirebaseCrashlytics?.recordException(ANRSpyException(msg,stackTrace))
        if(mShouldThrowException){
            throwException(msg, stackTrace)
        } else {
           logStackTrace(TAG,stackTrace)
        }
    }

    fun logStackTrace(tag: String, stackTrace: Array<StackTraceElement>) {
        var log = ""
        stackTrace.forEach { element ->
            log += element.toString()+"\n"
        }
        Log.i(tag, log)
    }

}