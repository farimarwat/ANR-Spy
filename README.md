### Android ANR Spy
Android ANR Spy is the most simplest library that helps android developers to detect ANRs. The ANRspy library helps in detecting and reporting Application Not Responding (ANR) issues in Android applications. It provides integration with Firebase Crashlytics for reporting and allows setting custom actions when an ANR is detected.

### Features
1. Detect ANR
2. Detect a function which is approaching ANR limit and send report to firebase prior to ANR occurrence.
3. Prevent from downranking your app on google play console by exiting app prior to anr occurance. 

### What is Android ANR (Application Not Responding)
when a developer do most heavy jobs on UI thread (more than 5 seconds usually) and UI thread still receieve more request/events for doing a task then Android system raises ANR message. This is extremely bad effect on your app and may lead to the failure of your business.
### Android ANR does matter
Google recommends/suggests your app on play store. If your app raises too many ANRs then your app will be ranked down

### Android ANR Durations
1. Normal on UI Thread in any activity = 5 secs
2. BroadCast = 10 sec
3. Service = 20 sec


<h4><a href="https://www.youtube.com/watch?v=329yhbNjaHg">Video Tutorial </a></h4>

**Note: Sample app is included in the project. Just clone the repo**

## Android ANR Spy Library
### Implement:

```
implementation("io.github.farimarwat:anrspy:2.1")
```
## Usage
```
// Initialize FirebaseCrashlytics
val crashlyticsInstance = FirebaseCrashlytics.getInstance()

// Create ANRSpyAgent
val anrSpyAgent = ANRSpyAgent.Builder(this)
    .setTimeOut(5000L) // Set timeout threshold (in milliseconds)
    .setTicker(200L) // Set the interval at which the main thread is checked (in milliseconds)
    .setFirebaseCrashLytics(crashlyticsInstance) // Set FirebaseCrashlytics instance
    .setAppAction(AppAction.AppActionExit) // Set the action to perform when an ANR is detected
    .build()
```

### Tips
#### Firebase Integration:
By setting up FirebaseCrashlytics, you will receive reports in the form of fatal exceptions (not ANR/crash). This means if any function is approaching the ANR limit, a report will be sent to Firebase. This can help in identifying potential ANR issues before they cause a crash.

#### Preventing ANR Reports to Google Play Console:
By setting an app action such as AppAction.AppActionExit, the library will prevent ANR from being reported to the Google Play Console by exiting the application before the ANR occurs.

## Change Log
**version 2.1**

- throwException method removed
- AppAction feature added


**version 2.0**

1. ANR detection with annotation method removed
2. ANR detection algorithm changed
3. A lot of extra functions removed

**version 1.3**
1. Get details of methods which is related to main app package that causes anrs via "onAnrDetected" extra paramater
2. A bug fixed (classnotfoundexception)

**version 1.2 (beta)**
1. Annotation added to trace a specific method for ANR
2. Store annotated methods report in firebase analytics

**version 1.0**
Initial release

## Support Me
If you want to donate then you are welcome to buy me a cup of tea via **PATREON** because this encourages me to give you more free stuff
and continue to  maintain this library

<a href="https://patreon.com/farimarwat">Buy Now!</a>

