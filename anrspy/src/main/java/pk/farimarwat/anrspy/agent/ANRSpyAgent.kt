package pk.farimarwat.anrspy.agent

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.analytics.FirebaseAnalytics
import pk.farimarwat.anrspy.annotations.TraceClass
import pk.farimarwat.anrspy.annotations.TraceMethod
import pk.farimarwat.anrspy.models.MethodModel
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class ANRSpyAgent constructor(builder: Builder) : Thread() {
    private var mContext:Context
    //Params
    private var mListener: ANRSpyListener? = null
    private var mShouldThrowException: Boolean = false
    private var TIME_OUT = 5000L
    private var mReportAnnotatedMethods: Boolean = false
    private var mListAnnotatedMedhods = mutableListOf<String>()
    private var mReportMethods = mutableListOf<MethodModel>()
    private var mFirebaseInstance:FirebaseAnalytics? = null

    //

    private val INTERVAL = 500L
    private val mHandler = Handler(Looper.getMainLooper())
    private var _timeWaited = 0L
    private val _mTesterWorker = Runnable {
        _timeWaited = 0L
    }

    private val mIdleHandler = IdleHandler {
        mListener?.onReportAvailable(mReportMethods)
        mFirebaseInstance?.let {
            for(report in mReportMethods){
                val bundle = Bundle()
                bundle.putString("ANR_SPY_Method",report.name)
                bundle.putString("ANR_SPY_Thread",report.thread.name)
                bundle.putLong("ANR_SPY_Elapsed_Time",report.elapsedTime)
                it.logEvent("ANR_SPY_${report.name}",bundle)
            }
        }
        mReportMethods = mutableListOf()

        true
    }
    init {
        this.mListener = builder.getSpyListener()
        this.mShouldThrowException = builder.getThrowException()
        this.TIME_OUT = builder.getTimeOout()
        this.mReportAnnotatedMethods = builder.getReportAnnotatedMethods()
        this.mFirebaseInstance = builder.getFirebaseInstance()
        this.mContext = builder.mContext
        Looper.getMainLooper().queue.addIdleHandler(mIdleHandler)
    }

    //Builder
    class Builder(var mContext:Context) {
        //Params
        private var mListener: ANRSpyListener? = null
        private var mShouldThrowException: Boolean = false
        private var TIME_OUT = 5000L
        private var mEnableInstantReport: Boolean = false
        private var mFirebaseInstance:FirebaseAnalytics? = null


        //
        fun setSpyListener(listener: ANRSpyListener) = apply { this.mListener = listener }
        fun getSpyListener() = this.mListener

        fun setThrowException(throwexception: Boolean) =
            apply { this.mShouldThrowException = throwexception }

        fun getThrowException() = this.mShouldThrowException

        fun setTimeOut(timeout: Long) = apply { TIME_OUT = timeout }
        fun getTimeOout() = TIME_OUT

        fun enableReportAnnotatedMethods(enable: Boolean) = apply { mEnableInstantReport = enable }
        fun getReportAnnotatedMethods() = this.mEnableInstantReport

        fun setFirebaseInstance(instance:FirebaseAnalytics?) = apply { this.mFirebaseInstance = instance }
        fun getFirebaseInstance() = this.mFirebaseInstance


        fun build(): ANRSpyAgent = ANRSpyAgent(this)
    }
    //End builder

    override fun run() {
        while (!isInterrupted) {
            val stacktrace = Looper.getMainLooper().thread.stackTrace
            isResumeExists(stacktrace)
            _timeWaited += INTERVAL
            mListener?.onWait(_timeWaited)
            mHandler.post(_mTesterWorker)
            sleep(INTERVAL)
            if (_timeWaited > TIME_OUT) {
                val listPackageMethods = findPackagMethods(stacktrace)
                mListener?.onAnrDetected(
                    "$THREAD_TITLE Main thread blocked for: $_timeWaited ms",
                    stackTrace,
                    findPackagMethods(stacktrace)
                )
                if (mShouldThrowException) {
                    throwException(Looper.getMainLooper().thread.stackTrace)
                }
            }
            if (mReportAnnotatedMethods) {
                instantReport()
            }
        }

    }

    private fun isResumeExists(stacktrace: Array<StackTraceElement>) {
//        val allstacks = getAllStackTraces()
//        Log.w(TAG,"Threads------------")
//        for(entity in allstacks){
//            Log.e(TAG,"${entity.key}")
//        }
//        Log.w(TAG,"Threads------------\nn")
        Log.w(TAG,"Threads------------\n")
        for(item in stacktrace){
            Log.e(TAG,"${item}")
        }
        Log.w(TAG,"Threads------------\n")
    }

    private fun throwException(stackTrace: Array<StackTraceElement>) {
        throw ANRSpyException(THREAD_TITLE, stackTrace)
    }

    fun instantReport() {
        val stacktrace = Looper.getMainLooper().thread.stackTrace
        for(element in stacktrace){
            addAnnotatedMethods(element.className)
            val methodexists = mListAnnotatedMedhods.find {
                element.methodName.lowercase().startsWith(it.lowercase())
            }
            if (methodexists != null) {
                addMethod(methodexists,Looper.getMainLooper().thread)
                return
            }
        }
    }

    @Synchronized
    fun addMethod(methodName: String, thread: Thread) {
        val exists = mReportMethods
            .find {
                (it.name.lowercase() == methodName.lowercase())
            }
        if (exists != null) {
            for (item in mReportMethods) {
                if (item.name.lowercase() == exists.name.lowercase()) {
                    item.elapsedTime += INTERVAL
                    return
                }
            }
        } else {
            mReportMethods.add(
                MethodModel(
                    System.currentTimeMillis(), methodName, thread, 0
                )
            )
        }
    }

    fun addAnnotatedMethods(className:String){
        try{
            val clazz = Class.forName(className)
            val annotation = clazz.getAnnotation(TraceClass::class.java)
            annotation?.let {
                if(it.traceAllMethods){
                    for(m in clazz.declaredMethods){
                        val exists = mListAnnotatedMedhods.find {
                            it.lowercase() == m.name.lowercase()
                        }
                        if(exists == null){
                            mListAnnotatedMedhods.add(m.name)
                        }
                    }
                } else {
                    for(m in clazz.declaredMethods){
                        val annotation = m.getAnnotation(TraceMethod::class.java)
                        annotation?.let { tm ->
                            val exists = mListAnnotatedMedhods.find {
                                it.lowercase() == m.name.lowercase()
                            }
                            if(exists == null){
                                mListAnnotatedMedhods.add(m.name)
                            }
                        }
                    }
                }
            }
        }catch (ex:Exception){
            mListener?.onError(ex.message.toString())
        }
    }

    fun findPackagMethods(stacktrace: Array<StackTraceElement>):List<String>?{
        val list = mutableListOf<String>()
        val seq_strack = stacktrace.toList()
        val filtered = seq_strack.filter {
            it.className.lowercase().contains(mContext.packageName.lowercase())
        }
        if(filtered.isNotEmpty()){
            filtered.forEach { st ->
                val body = "Class: ${st.className} Method: ${st.methodName} LineNumber<${st.lineNumber}>(${st.fileName})"
                list.add(body)
            }
            return list
        }
        return null
    }

}