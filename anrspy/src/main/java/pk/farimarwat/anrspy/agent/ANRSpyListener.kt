package pk.farimarwat.anrspy.agent

import pk.farimarwat.anrspy.models.MethodModel

interface ANRSpyListener {
    fun onWait(ms:Long){}
     @Deprecated("This callback will be removed in future")
    fun onAnrStackTrace(stackstrace:Array<StackTraceElement>){}
    fun onAnrDetected(
        details: String,
        stackTrace: Array<StackTraceElement>,
        packageMethods: List<String>?
    ){}
    fun onReportAvailable(methodList:List<MethodModel>){}
    fun onError(error:String){}
}