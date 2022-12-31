package pk.farimarwat.anrspy.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TraceClass(
    val traceAllMethods:Boolean = true
)
