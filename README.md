### Android ANR Spy
This is the most simplest library that helps android developers to detect ANRs.

### What is ANR (Application Not Responding)
when a developer do most heavy jobs on UI thread (more than 5 seconds usually) and UI thread still receieve more request/events for doing a task then Android system raises ANR message. This is extremely bad effect on your app and may lead to the failure of your business.
### ANR does matter
Google recommends/suggests your app on play store. If your app raises too many ANRs then your app will be ranked down

### ANR Durations
1. Normal on UI Thread in any activity = 5 secs
2. BroadCast = 10 sec
3. Service = 20 sec

## Usage

### Implement:
Step 1:
```
allprojects {
	repositories {
	...
	maven { url 'https://jitpack.io' }
	}
}
```

Step 2:

```
implementation 'com.github.farimarwat:ANR-Spy:1.0'
```

Now Build anrSpyAgent and do start.

#### Note: Sample app is included in the project
1. To Test ANR press "Main button" on main activity
2. To Test ANR in broadcast "Change Plan Mode"
3. To Test ANR in service, press Service button in the project
```
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mReceiver = AirPlanMode()
        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            registerReceiver(mReceiver,it)
        }
        val anrSpyAgent = ANRSpyAgent.Builder()
            .setSpyListener(object : ANRSpyListener {
                override fun onWait(ms: Long) {
                    //Log.e(TAG,"Waited: $ms")
                }

                override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {
                    Log.e(TAG,"Stack:\n ${stackstrace}")
                }

                override fun onAnrDetected(details: String, stackTrace: Array<StackTraceElement>) {
                    Log.e(TAG,details)
                    Log.e(TAG,"${stackTrace}")
                }
            })
            .setThrowException(true)
            .setTimeOut(5000)
            .build()
        anrSpyAgent.start()
        initGui()
    }
```
