package pk.farimarwat.anrspy.agent

class ANRSpyException(title:String, stacktrace:Array<StackTraceElement>):Throwable(title) {
    init {
        stackTrace = stacktrace
    }
}