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
Srting input = "Long test for regex parsing 'abcd'"; // The character sequence to be matched
Srting regex = "a(.*)d"; // Regular expression
Srting replacement = "A$1D"; // replacement text
int flags = 0; // Match flags, a bit mask that may include (CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS, COMMENTS)

RxRegex.replace(input, regex, replacement);
RxRegex.replace(input, regex, replacement, flags);     
RxRegex.find(input, regex, replacement);
RxRegex.find(input, regex, replacement, flags);
```

##Samples

Reactive usage:
```java
RxRegex.replace("abcd", "bc", "BC", 0)  // create Observable 
 .subscribe(onAppend -> Log.i("",onAppend.getAppendDst()));   // write to log "a", "BC", "d"
```

Use reactive with threads:
```java
RxRegex.replace("abcd", "bc", "BC", 0)
 .subscribeOn(Schedulers.computation())
 .observeOn(AndroidSchedulers.mainThread())
 .scan(new StringBuffer(), (stringBuffer, onAppend) -> stringBuffer.append(onAppend.getAppendDst())).skip(1)
 .last(new StringBuffer()).map(stringBuffer -> stringBuffer.toString())
 .subscribe((result) -> Log.i("",result)); // result == "aBCd"
```

Non reactive usage:
Just replace and get result.
```java
Regex.replace("abcd", "(bc)", "_$1_\\n\\r\\t\n\r\t", 0);  //result == "a_bc_\n\r\t\n\r\td"
```

Get result part by part to listener.
```java
Regex.replace("abcd", "(bc)", "_$1_", 0, listener );  // calls Listener.append()  with args "a", "_bc_", "d"
```

Ability to cancel the process. Use ```CancelationSignal```
```java
cancelationSignal.cancel();
Regex.replace("abcd", "(bc)", "_$1_", 0, listener, cancelationSignal );  // listener never called
```
