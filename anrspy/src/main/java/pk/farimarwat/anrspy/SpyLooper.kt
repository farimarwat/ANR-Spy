package pk.farimarwat.AnrSpy

import android.os.Looper
import android.os.Message
import android.os.MessageQueue
import android.util.Log
import java.lang.reflect.Field


class SpyLooper {
    val TAG = "Looper Spy"
    var messagesField: Field
    var nextField: Field
    var mainMessageQueue: MessageQueue

    init {
        try {
            val queueField = Looper::class.java.getDeclaredField("mQueue")
            queueField.isAccessible = true
            messagesField = MessageQueue::class.java.getDeclaredField("mMessages")
            messagesField.isAccessible = true
            nextField = Message::class.java.getDeclaredField("next")
            nextField.isAccessible = true
            val mainLooper = Looper.getMainLooper()
            mainMessageQueue = queueField[mainLooper] as MessageQueue
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun dumpQueue() {
        try {
            val nextMessage = messagesField[mainMessageQueue] as Message
            Log.d(TAG, "Begin dumping queue")
            dumpMessages(nextMessage)
            Log.d(TAG, "End dumping queue")
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IllegalAccessException::class)
    fun dumpMessages(message: Message?) {
        message?.let {
            Log.w(TAG,"Message: ${message}")
            val next = nextField[message] as Message?
            dumpMessages(next)
        }
    }
}