### Android ANR Spy
Android ANR Spy is the most simplest library that helps android developers to detect ANRs.

### What is Android ANR (Application Not Responding)
when a developer do most heavy jobs on UI thread (more than 5 seconds usually) and UI thread still receieve more request/events for doing a task then Android system raises ANR message. This is extremely bad effect on your app and may lead to the failure of your business.
### Android ANR does matter
Google recommends/suggests your app on play store. If your app raises too many ANRs then your app will be ranked down

### Android ANR Durations
1. Normal on UI Thread in any activity = 5 secs
2. BroadCast = 10 sec
3. Service = 20 sec

## Android ANR Spy Library
### Implement:

```
implementation("io.github.farimarwat:anrspy:1.2")
```
## Usage

### Step 1: Create a Callback Object
```
 //Anr Callback
    private var mCallback = object : ANRSpyListener {
        override fun onWait(ms: Long) {
		//Total blocking time of main thread. 
		//Can be used for doing any action e.g. if blocked time is more than 5 seconds then 
		//restart the app to avoid raising ANR message because it will lead to down rank your app.
        }

        override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {
		//To  investigate ANR via stackstrace if occured.
		//This method is deprecated and will  be removed in future
        }

        override fun onReportAvailable(methodList: List<MethodModel>) {
		//Get instant report about annotated methods if touches main thread more than target time
        }
        override fun onAnrDetected(details: String, stackTrace: Array<StackTraceElement>) {
		// Is triggered when ANR is detected
        }
    }
```
### Step 2: Create Instance
```
val anrSpyAgent = ANRSpyAgent.Builder()
            .setTimeOut(5000)
            .setSpyListener(mCallback)
            .setThrowException(false)
            .enableReportAnnotatedMethods(true)
            .setFirebaseInstance(firebaseinstance)
            .build()
```
### Step 3: Start Tracing
```
anrSpyAgent.start()
```

### Builder Methods

** setTimeOut(5000)**
Time limit to detect ANR

** setSpyListener()**
Sets ANRSpyListener/callback methods

**setThrowException(true)**
Convert possible ANR to crash to figure out the line where ANR may be possible and close the app if true. Default is false

**enableReportAnnotatedMethods(true)**
This will generate report for annotated methods that you want to trace any where in the app. If the specified methods touches main thread for more than target time (default 5 secs), it will trigger **onReportAvailable** method of the callback to get details about the function e.g. Thread Name, Elapsed Time on main thread and function
Note: If the annotated method is not running on main thread then there will  be no report generated. 

**setFirebaseInstance(firebaseinstance)**
To get logs similar to the mention above on firebase.
Just set the instance for firebase analytics and all events will be collected as usuall to other events.
All the events will be prefixed with: ANR_SPY_  to differenciate from other events on firebase

### Annotations
In case if any one want to trace a specific method to trace then there are two types of annotations available:

**1. @TraceClass(traceAllMethods = false)**
This annotatiion is applied to a class and takes one perameter. If the peramater traceAllMethods is set to true then all methods of the class will be traced on main thread. Default is true
**Note:** If **traceAllMethods** is set to **false** and there is no specific annotated method then there will be no report generated
Example: To trace all methods in MainActivity:
```
@TraceClass(traceAllMethods = true)
class MainActivity : AppCompatActivity() {
	.......
}
```

**2. @TraceMethod**
To trace a specific method on main thread for ANR
*Example: *
```
@TraceMethod
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
}
```
**Note:** If the method is not running on main thread then there will be no report generated

## Change Log
**version 1.2 (beta)**
1. Annotation added to trace a specific method for ANR
2. Store annotated methods report in firebase analytics

**version 1.0**
Initial release

