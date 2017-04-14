# RxRegex

Reactive version of Android Regex from ```java.util.regex```.
This lib is useful if you work with large text.

The library ```java.util.regex``` has very narrow functionality. There is no way to stop the running process. You can not get the already processed part of the text - you should always wait for the full processing of the text. There is no way to get the percentage of processed text.

Features of this library:
* Canceling parse process.
* Get parse progress.
* Positions of matched parts of text.
* Positions of replaced parts of text.
* Callback for each parsed part.
* Support ```\n\r\t``` in replacement.

Exists two base classes: **RxRegex** - *Reactive version* and **Regex** - *Callback version*.

## Add dependencies

**Step 1.** Add the JitPack repository to your build file.
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
**Step 2.** Add the dependency
```gradle
dependencies {
    compile 'com.github.tepikin:RxRegex:0.1'
}
```

## How to use RxRegex

For create Observable use methods ```RxRegex.replace``` and ```RxRegex.find```. It's ```Disposable``` objects and you can use it for stop parsing process, you can use method ```dispose()``` or just unsubscribe from Observable and parsing stops automatically.
```java
Srting input = "Long text !12! for parsing !AB!";    // The character sequence to be matched
Srting regex = "!..!";                               // Regular expression
Srting replacement = "ABCD";                         // replacement text


// Simple usage
RxRegex.replace(input, regex, replacement)      
    .subscribe(replace -> log(replace.toString()));  // logs out: 
                                                     //    Long text   -> Long text
                                                     //    !12!        -> ABCD
                                                     //    for parsing -> for parsing
                                                     //    !AB!        -> ABCD


// Stop parsing progess by dispose()
Disposable disposable = RxRegex.replace(input, regex, replacement)
    .filter(RxRegex.OnAppend::isMatched)             // filter parts matched to regex
    .subscribe(replace -> log(replace.toString()));  // logs out:
                                                     //    !12! -> ABCD
                                                     //    !AB! -> ABCD
disposable.dispose();  // you can stop parsing at any time.


// Object in onNext() is RxRegex.onAppend. It's fields description below.
RxRegex.replace(input, regex, replacement)      
    .subscribe(replace -> {
        replace.getFromSrc()         // Start position of current part at original text
        replace.getToSrc()           // End position of current part at original text
        replace.getAppendSrc()       // Cuttent processed text part from original text
        replace.getFromDst()         // Start position of current part at replaced text
        replace.getToDst()           // End position of current part at replaced text
        replace.getAppendDst()       // Replaced text part
        replace.isMatched()          // Is current part matched to regex
        replace.getProgress()        // Current parsing progress (float from 0 - to 1)
        replace.getMatchedCount()    // Count of matched perts at this moment
    });      
    
    
// Use reactive with threads
RxRegex.replace("abcd", "bc", "BC", 0)
 .subscribeOn(Schedulers.computation())        // Parse in computation thread
 .observeOn(AndroidSchedulers.mainThread())    // Observe in Android mainThread
 .scan(new StringBuffer(), (stringBuffer, onAppend) -> stringBuffer.append(onAppend.getAppendDst())).skip(1)
 .last(new StringBuffer()).map(stringBuffer -> stringBuffer.toString())
 .subscribe((result) -> Log.i("",result));     // result == "aBCd"
```    

## How to use Non reactive version

Non reactive version work with class ```Regex```.
```java
// Simple usage
String result = Regex.replace("abcd", "bc", "BC");  // result = "aBCd" 

// Use listener
Regex.replace("abcd", "bc", "BC", 0, listener );    // calls Listener with args "a -> a", "bc -> BC", "d -> d"

// Use CancelationSignal
CancelationSignal cancelationSignal = new CancelationSignalImpl();
Regex.replace("abcd", "(bc)", "_$1_", 0, listener, cancelationSignal ); 
cancelationSignal.cancel();                         // cancelationSignal stop parsing process.
```
