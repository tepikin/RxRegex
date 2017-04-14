package ru.lazard.rxregex;


import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Egor on 12.04.2017.
 */

public class Regex {

    private String mText;
    private String mRegularExpression;
    private String mReplacement;
    private Listener mListener;
    private int mFlags;
    private int mMatchedCount;
    private CancellationSignal mCancellationSignal;

    private Regex(@NonNull String text,@NonNull String regularExpression,@NonNull String replacement, int flags,@NonNull Listener listener,@NonNull CancellationSignal cancellationSignal) {
        this.mText = text;
        this.mRegularExpression = regularExpression;
        this.mReplacement = replacement;
        this.mFlags = flags;
        this.mListener = listener;
        this.mCancellationSignal = cancellationSignal;
    }

    public static void find(@NonNull String text,@NonNull String regularExpression,@NonNull Listener listener) {
        find(text, regularExpression, 0, listener);
    }

    public static void find(@NonNull String text,@NonNull String regularExpression, int flags,@NonNull Listener listener) {
        find(text, regularExpression, flags, listener,new CancellationSignalFake());
    }
    public static void find(@NonNull String text,@NonNull String regularExpression, int flags,@NonNull Listener listener,@NonNull CancellationSignal cancellationSignal ) {
        replace(text, regularExpression, "$0", flags, listener,cancellationSignal);
    }

    public static String replace(@NonNull String text,@NonNull String regularExpression,@NonNull String replaceText) {
        return replace(text, regularExpression, replaceText, 0);
    }

    public static String replace(@NonNull String text,@NonNull String regularExpression,@NonNull String replaceText, int flags) {
        final StringBuffer buffer = new StringBuffer();
        replace(text, regularExpression, replaceText, flags,
                (int fromSrc, int toSrc, String textSrc, int fromDst, int toDst, String textDst, boolean isMatched, float progress, int matchedCount) ->
                        buffer.append(textDst));
        return buffer.toString();
    }

    public static void replace(@NonNull String text,@NonNull String regularExpression,@NonNull String replaceText,@NonNull Listener listener) {
        replace(text, regularExpression, replaceText, 0, listener);
    }

    public static void replace(@NonNull String text,@NonNull  String regularExpression,@NonNull  String replaceText, int flags,@NonNull Listener listener) {
        replace(text, regularExpression, replaceText, 0, listener,new CancellationSignalFake());
    }

    public static void replace(@NonNull String text,@NonNull  String regularExpression,@NonNull  String replaceText, int flags,@NonNull Listener listener,@NonNull CancellationSignal cancellationSignal) {
        new Regex(text, regularExpression, replaceText, flags, listener,cancellationSignal).start();
    }

    private void start() {
        if (mCancellationSignal.isCanceled())return;

        mMatchedCount = 0;
        Pattern pattern = Pattern.compile(this.mRegularExpression, mFlags);
        Matcher matcher = pattern.matcher(mText);
        StringBuffer bufferEvaluated = new StringBuffer();
        int textLength = mText.length();
        int appendPos = 0;
        int dstLength = 0;

        matcher.reset();
        while (matcher.find()) {
            if (mCancellationSignal.isCanceled())return;
            if (matcher.start() == matcher.end() && matcher.start() != 0 && matcher.end() != textLength)
                throw new IllegalArgumentException("Too short replace text in regularExpression");

            mMatchedCount++;

            String substring = mText.substring(appendPos, matcher.start());
            mListener.append(appendPos, matcher.start(), substring, dstLength, dstLength + substring.length(), substring, false, (float) matcher.start() / textLength, mMatchedCount);
            dstLength += substring.length();

            if (mCancellationSignal.isCanceled())return;

            bufferEvaluated.delete(0, bufferEvaluated.length());
            appendEvaluated(bufferEvaluated, mReplacement, matcher);

            if (mCancellationSignal.isCanceled())return;

            String substringSrc = mText.substring(matcher.start(), matcher.end());
            String substringDst = bufferEvaluated.toString();
            mListener.append(matcher.start(), matcher.end(), substringSrc, dstLength, dstLength + substringDst.length(), substringDst, true, (float) matcher.end() / textLength, mMatchedCount);
            dstLength += substringDst.length();

            appendPos = matcher.end();
        }
        if (mCancellationSignal.isCanceled())return;
        if (appendPos < matcher.regionEnd()) {
            String substring = mText.substring(appendPos, matcher.regionEnd());
            mListener.append(appendPos, matcher.regionEnd(), substring, dstLength, dstLength + substring.length(), substring, false, (float) matcher.regionEnd() / textLength, mMatchedCount);
        }
    }

    /**
     * Internal helper method to append a given string to a given string buffer.
     * If the string contains any references to groups, these are replaced by
     * the corresponding group'mReplacement contents.
     *
     * @param buffer      the string buffer.
     * @param replaceText the string to append.
     */
    private void appendEvaluated(StringBuffer buffer, String replaceText, Matcher matcher) {
        boolean escape = false;
        boolean dollar = false;

        for (int i = 0; i < replaceText.length(); i++) {
            char c = replaceText.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar) {
                buffer.append(matcher.group(c - '0'));
                dollar = false;
            } else if (c == 'n' && escape) {
                buffer.append("\n");
                dollar = false;
                escape = false;
            } else if (c == 'r' && escape) {
                buffer.append("\r");
                dollar = false;
                escape = false;
            } else if (c == 't' && escape) {
                buffer.append("\t");
                dollar = false;
                escape = false;
            } else if (escape) {
                buffer.append("\\" + c);
                dollar = false;
                escape = false;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
            }
        }
        if (escape) {
            throw new ArrayIndexOutOfBoundsException(replaceText.length());
        }
    }

    public interface Listener {
        void append(int fromSrc, int toSrc, String appendSrc,
                    int fromDst, int toDst, String appendDst,
                    boolean isMatched, @FloatRange(from = 0, to = 1) float progress, int matchedCount);
    }

    public interface CancellationSignal {

        boolean isCanceled();

        void cancel();
    }

    private static class CancellationSignalFake implements CancellationSignal {

        public boolean isCanceled() {
           return false;
        }

        public void cancel() {
            throw new UnsupportedOperationException("Fake realization of CancellationSignal");
        }
    }
}
