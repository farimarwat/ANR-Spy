package pk.farimarwat.anrspy.models

import androidx.annotation.Keep

@Keep
data class MethodModel(
    val id:Long,
    val name:String,
    val thread:Thread,
    var elapsedTime:Long
)
