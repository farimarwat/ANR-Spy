package pk.farimarwat.anrspy.models

data class MethodModel(
    val id:Long,
    val name:String,
    val thread:Thread,
    var elapsedTime:Long
)
