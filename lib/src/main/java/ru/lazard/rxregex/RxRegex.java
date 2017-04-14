package ru.lazard.rxregex;

import io.reactivex.Observable;
import io.reactivex.functions.Cancellable;

/**
 * Created by Egor on 12.04.2017.
 */

public class RxRegex {

    public static Observable<OnAppend> find(String text, String regularExpression) {
        return find(text, regularExpression, 0);
    }

    public static Observable<OnAppend> find(String text, String regularExpression, int flags) {
        return Observable.create(emitter -> {
            CancellationSignalImpl cancellationSignal = new CancellationSignalImpl();
            emitter.setCancellable(cancellationSignal);
            Regex.find(text, regularExpression, flags,
                    (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                            emitter.onNext(new OnAppend(fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount))
                    ,cancellationSignal);
            emitter.onComplete();
        });
    }

    public static Observable<OnAppend> replace(String text, String regularExpression, String replacement) {
        return replace(text, regularExpression, replacement, 0);
    }


    public static Observable<OnAppend> replace(String text, String regularExpression, String replacement, int flags) {
        return Observable.create(emitter -> {
            CancellationSignalImpl cancellationSignal = new CancellationSignalImpl();
            emitter.setCancellable(cancellationSignal);
            Regex.replace(text, regularExpression, replacement, flags,
                    (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                        emitter.onNext(new OnAppend(fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount))
                    , cancellationSignal);
            emitter.onComplete();
        });
    }


    public static class OnAppend {

        private int fromSrc;
        private int toSrc;
        private String appendSrc;
        private int fromDst;
        private int toDst;
        private String appendDst;
        private boolean isMatched;
        private float progress;
        private int matchedCount;

        public OnAppend(int fromSrc, int toSrc, String appendSrc, int fromDst, int toDst, String appendDst, boolean isMatched, float progress, int matchedCount) {
            this.fromSrc = fromSrc;
            this.toSrc = toSrc;
            this.appendSrc = appendSrc;
            this.fromDst = fromDst;
            this.toDst = toDst;
            this.appendDst = appendDst;
            this.isMatched = isMatched;
            this.progress = progress;
            this.matchedCount = matchedCount;
        }

        public float getProgress() {
            return progress;
        }

        public int getMatchedCount() {
            return matchedCount;
        }

        public int getFromSrc() {
            return fromSrc;
        }

        public int getToSrc() {
            return toSrc;
        }

        public int getFromDst() {
            return fromDst;
        }

        public int getToDst() {
            return toDst;
        }

        public String getAppendDst() {
            return appendDst;
        }

        public String getAppendSrc() {
            return appendSrc;
        }

        public boolean isMatched() {
            return isMatched;
        }
    }


    private static class CancellationSignalImpl implements Regex.CancellationSignal, Cancellable {
        boolean isCanceled;

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void cancel() {
            isCanceled = true;
        }
    }
}


