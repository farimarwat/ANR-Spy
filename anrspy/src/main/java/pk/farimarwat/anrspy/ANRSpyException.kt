package pk.farimarwat.AnrSpy

class ANRSpyException(title:String, stacktrace:Array<StackTraceElement>):Throwable(title) {
    init {
        stackTrace = stacktrace
    }
}