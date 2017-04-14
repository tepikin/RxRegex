"# RxRegex" 
Samples

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
