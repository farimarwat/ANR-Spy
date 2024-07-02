package pk.farimarwat.anrspy.models

/**
 * Represents different actions that can be performed when an ANR (Application Not Responding) is detected.
 */
sealed class AppAction {
    /**
     * Exit the application when an ANR is detected.
     */
    object AppActionExit : AppAction()

    /**
     * Restart the application when an ANR is detected.
     */
    object AppActionRestart : AppAction()

    /**
     * Throw an exception when an ANR is detected.
     */
    object AppActionException : AppAction()
}
