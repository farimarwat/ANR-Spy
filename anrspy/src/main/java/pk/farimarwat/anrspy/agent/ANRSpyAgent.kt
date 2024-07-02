package pk.farimarwat.anrspy.agent

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import pk.farimarwat.anrspy.ANRSpyConstants.EXTRA_DETAILS
import pk.farimarwat.anrspy.LoggerActivity
import pk.farimarwat.anrspy.models.AppAction
import pk.farimarwat.anrspy.models.MethodModel

/**
 * ANRSpyAgent is responsible for monitoring the main thread for Application Not Responding (ANR) incidents.
 * It periodically checks the main thread's stack trace and performs actions based on the specified appAction.
 *
 * @constructor Creates an instance of ANRSpyAgent with the specified builder.
 * @property mContext The context in which the agent operates.
 * @property TIME_OUT The timeout threshold for detecting ANR (in milliseconds).
 * @property mTicker The interval at which the main thread is checked (in milliseconds).
 * @property mPreviousMethod The name of the previous method detected in the stack trace.
 * @property mReported The name of the method reported for ANR.
 * @property mDuration The duration for which the method has been blocking the main thread.
 * @property appAction The action to perform when an ANR is detected.
 * @property mFirebaseCrashlytics An instance of FirebaseCrashlytics for logging exceptions.
 * @property listMethodsExcluded A list of method names to be excluded from ANR detection.
 */
@RequiresApi(Build.VERSION_CODES.M)
class ANRSpyAgent constructor(builder: Builder) : Thread() {
    private var mContext: Context
    private var TIME_OUT = 5000L
    private var mTicker: Long
    private var mPreviousMethod = ""
    private var mReported = ""
    private var mDuration = 0L
    private var appAction: AppAction? = null
    private var mFirebaseCrashlytics: FirebaseCrashlytics? = null
    private var listMethodsExcluded = mutableListOf("nativePollOnce")

    init {
        this.TIME_OUT = builder.getTimeOout()
        this.mFirebaseCrashlytics = builder.getFirebaseInstance()
        this.mContext = builder.mContext
        this.mTicker = builder.getTicker()
        this.appAction = builder.getAppAction()
    }

    /**
     * Builder class for constructing an instance of ANRSpyAgent.
     *
     * @param mContext The context in which the agent will operate.
     */
    class Builder(var mContext: Context) {
        private var TIME_OUT = 5000L
        private var mTicker = 200L
        private var mFirebaseCrashlytics: FirebaseCrashlytics? = null
        private var appAction: AppAction? = null

        /**
         * Sets the timeout threshold for detecting ANR.
         * @param timeout The timeout threshold (in milliseconds).
         * @return The Builder instance.
         */
        fun setTimeOut(timeout: Long) = apply { TIME_OUT = timeout }

        /**
         * Gets the timeout threshold for detecting ANR.
         * @return The timeout threshold (in milliseconds).
         */
        fun getTimeOout() = TIME_OUT

        /**
         * Sets the interval at which the main thread is checked.
         * @param ticker The interval (in milliseconds).
         * @return The Builder instance.
         */
        fun setTicker(ticker: Long) = apply { mTicker = ticker }

        /**
         * Gets the interval at which the main thread is checked.
         * @return The interval (in milliseconds).
         */
        fun getTicker() = this.mTicker

        /**
         * Sets the instance of FirebaseCrashlytics for logging exceptions.
         * @param instance The FirebaseCrashlytics instance.
         * @return The Builder instance.
         */
        fun setFirebaseCrashLytics(instance: FirebaseCrashlytics) =
            apply { this.mFirebaseCrashlytics = instance }

        /**
         * Gets the instance of FirebaseCrashlytics for logging exceptions.
         * @return The FirebaseCrashlytics instance.
         */
        fun getFirebaseInstance() = this.mFirebaseCrashlytics

        /**
         * Sets the action to perform when an ANR is detected.
         * @param appAction The action to perform.
         * @return The Builder instance.
         */
        fun setAppAction(appAction: AppAction) = apply { this.appAction = appAction }

        /**
         * Gets the action to perform when an ANR is detected.
         * @return The action to perform.
         */
        fun getAppAction() = this.appAction

        /**
         * Builds and returns an instance of ANRSpyAgent.
         * @return The constructed ANRSpyAgent instance.
         */
        fun build(): ANRSpyAgent = ANRSpyAgent(this)
    }

    override fun run() {
        while (!isInterrupted) {
            val stacktrace = Looper.getMainLooper().thread.stackTrace
            val method = stacktrace.firstOrNull()?.methodName ?: ""
            if (method != mPreviousMethod) {
                mPreviousMethod = method
                mDuration = 0
            } else {
                mDuration += mTicker
            }
            try {
                sleep(mTicker)
            } catch (ex: Exception) {
                Log.e(TAG, ex.toString())
            }
            if (mDuration > TIME_OUT && method !in listMethodsExcluded) {
                if (mReported != mPreviousMethod) {
                    mReported = mPreviousMethod
                    processANR(stacktrace)
                }
            }
        }
    }

    /**
     * Throws an ANRSpyException with the provided message and stack trace.
     * @param msg The exception message.
     * @param stackTrace The stack trace.
     */
    private fun throwException(msg: String, stackTrace: Array<StackTraceElement>) {
        throw ANRSpyException(msg, stackTrace)
    }

    /**
     * Processes the detected ANR by logging the stack trace and performing the specified action.
     * @param stackTrace The stack trace of the ANR.
     */
    fun processANR(stackTrace: Array<StackTraceElement>) {
        var msg = "Method: ${stackTrace.firstOrNull()?.methodName}"
        msg += " is blocking main thread for at least ${mDuration} ms"
        mFirebaseCrashlytics?.recordException(ANRSpyException(msg, stackTrace))
        logStackTrace(TAG, stackTrace)
        when (appAction) {
            AppAction.AppActionExit -> {
                val details = GlobalHelper.getLog(stackTrace)
                mContext.startActivity(
                    Intent(mContext, LoggerActivity::class.java).apply {
                        putExtra(EXTRA_DETAILS, details)
                    }
                )
                System.exit(0)
            }
            AppAction.AppActionRestart -> {
                GlobalHelper.restartApp(mContext)
            }
            AppAction.AppActionException -> {
                throwException(msg, stackTrace)
            }
            null -> {
                // No action specified
            }
        }
    }

    /**
     * Logs the stack trace to the specified tag.
     * @param tag The tag for logging.
     * @param stackTrace The stack trace to log.
     */
    fun logStackTrace(tag: String, stackTrace: Array<StackTraceElement>) {
        var log = ""
        stackTrace.forEach { element ->
            log += element.toString() + "\n"
        }
        Log.i(tag, log)
    }

    companion object {
        private const val TAG = "ANRSpyAgent"
    }
}
