package pk.farimarwat.anrspy.agent

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.util.Log
import androidx.annotation.RequiresApi
import pk.farimarwat.anrspy.models.MethodModel

@RequiresApi(Build.VERSION_CODES.M)
class ANRSpyAgent constructor(builder: Builder): Thread() {

    //Params
    private var mListener: ANRSpyListener? = null
    private var mShouldThrowException:Boolean = true
    private var TIME_OUT = 5000L
    private var mEnablePerformanceMatrix:Boolean = false
    private var mListAnnotatedMedhods = mutableListOf<String>()
    private var mListAnnotatedClasses = mutableListOf<String>()

    private var mReportMethods = mutableListOf<MethodModel>()
    private var mLiveThreads = mutableListOf<Thread>()

    //

    private val INTERVAL = 500L
    private val mHandler = Handler(Looper.getMainLooper())
    private var _timeWaited = 0L
    private val _mTesterWorker = Runnable {
        _timeWaited = 0L
    }

    private val mIdleHandler = object :IdleHandler{
        override fun queueIdle(): Boolean {
            mReportMethods = mutableListOf()
            return true
        }

    }
    init {
        this.mListener = builder.getSpyListener()
        this.mShouldThrowException = builder.getThrowException()
        this.TIME_OUT = builder.getTimeOout()
        this.mEnablePerformanceMatrix = builder.getPerformanceMatrix()
        mListAnnotatedMedhods.add("myLoop")
        mListAnnotatedMedhods.add("initGui")
        mListAnnotatedMedhods.add("onStartCommand")
        Looper.getMainLooper().queue.addIdleHandler(mIdleHandler)
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
                for(element in entity.value){
                    val methodexists = mListAnnotatedMedhods.find {
                        element.methodName.lowercase().startsWith(it.lowercase())
                    }
                    if(methodexists != null){
                        addMethod(methodexists,thread)
                    }
                }
            }
            dumpMethods(mReportMethods)
        }
    }

    @Synchronized
    fun addMethod(methodName:String, thread:Thread){
        val exists = mReportMethods
            .find {
                (it.name.lowercase() == methodName.lowercase())
        }
        if(exists != null){
            for(item in mReportMethods){
                if(item.name.lowercase() == exists.name.lowercase()){
                    item.elapsedTime += INTERVAL
                    return
                }
            }
        } else {
            mReportMethods.add(
                MethodModel(
                    System.currentTimeMillis(),methodName,thread,0
                )
            )
        }
    }

    fun dumpMethods(list:List<MethodModel>){
       if(list.isNotEmpty()){
           Log.e(TAG,"Methods-------")
           for(item in list){
               Log.e(TAG,"Method: ${item.name} ElapsedTime: ${item.elapsedTime} Thread: ${item.thread.name}")
           }
           Log.e(TAG,"End Methods ----\n")
       }
    }
}