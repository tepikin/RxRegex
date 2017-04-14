package ru.lazard.rxregex;

import io.reactivex.Observable;

/**
 * Class for work with Regex in Reactive style.
 * <p>
 * For create Observable use methods <code>RxRegex.replace</code> and <code>RxRegex.find</code>.
 * It's <code>Disposable</code> objects and you can use it for stop parsing process,
 * you can use method <code>dispose()</code> or just unsubscribe from Observable and parsing stops automatically.
 * <pre>{@code
 * RxRegex.replace("Long text !12! for parsing !AB!", "!..!", "ABCD")
 * .subscribe(replace -> log(replace.toString()));  // logs out:
 *                                                  //    Long text   -> Long text
 *                                                  //    !12!        -> ABCD
 *                                                  //    for parsing -> for parsing
 *                                                  //    !AB!        -> ABCD
 * }</pre>
 */

public class RxRegex {
    /**
     * Create Observable for find parts matched to regex, without replace.
     *
     * @param text  The character sequence to be matched
     * @param regex The regular expression
     * @throws IllegalArgumentException               If bit values other than those corresponding to the defined
     *                                                match flags are set in <tt>flags</tt>
     * @throws java.util.regex.PatternSyntaxException If the expression's syntax is invalid
     */
    public static Observable<OnAppend> find(String text, String regex) {
        return find(text, regex, 0);
    }

    /**
     * Create Observable for find parts matched to regex, without replace.
     *
     * @param regex The regular expression
     * @param flags Match flags, a bit mask that may include
     *              {@link java.util.regex.Pattern#CASE_INSENSITIVE}, {@link java.util.regex.Pattern#MULTILINE}, {@link java.util.regex.Pattern#DOTALL},
     *              {@link java.util.regex.Pattern#UNICODE_CASE}, {@link java.util.regex.Pattern#CANON_EQ}, {@link java.util.regex.Pattern#UNIX_LINES},
     *              {@link java.util.regex.Pattern#LITERAL}, {@link java.util.regex.Pattern#UNICODE_CHARACTER_CLASS}
     *              and {@link java.util.regex.Pattern#COMMENTS}
     * @throws IllegalArgumentException               If bit values other than those corresponding to the defined
     *                                                match flags are set in <tt>flags</tt>
     * @throws java.util.regex.PatternSyntaxException If the expression's syntax is invalid
     */
    public static Observable<OnAppend> find(String text, String regex, int flags) {
        return Observable.create(emitter -> {
            Regex.CancellationSignalImpl cancellationSignal = new Regex.CancellationSignalImpl();
            emitter.setCancellable(cancellationSignal);
            Regex.find(text, regex, flags,
                    (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                            emitter.onNext(new OnAppend(fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount))
                    , cancellationSignal);
            emitter.onComplete();
        });
    }

    /**
     * Create Observable for Regex replace process.
     *
     * @param text        The character sequence to be matched
     * @param regex       The regular expression
     * @param replacement Replacement text. Support groups $0-$9 and \n \r \t chars.
     * @throws IllegalArgumentException               If bit values other than those corresponding to the defined
     *                                                match flags are set in <tt>flags</tt>
     * @throws java.util.regex.PatternSyntaxException If the expression's syntax is invalid
     */
    public static Observable<OnAppend> replace(String text, String regex, String replacement) {
        return replace(text, regex, replacement, 0);
    }

    /**
     * Create Observable for Regex replace process.
     *
     * @param text        The character sequence to be matched
     * @param regex       The regular expression
     * @param replacement Replacement text. Support groups $0-$9 and \n \r \t chars.
     * @param flags       Match flags, a bit mask that may include
     *                    {@link java.util.regex.Pattern#CASE_INSENSITIVE}, {@link java.util.regex.Pattern#MULTILINE}, {@link java.util.regex.Pattern#DOTALL},
     *                    {@link java.util.regex.Pattern#UNICODE_CASE}, {@link java.util.regex.Pattern#CANON_EQ}, {@link java.util.regex.Pattern#UNIX_LINES},
     *                    {@link java.util.regex.Pattern#LITERAL}, {@link java.util.regex.Pattern#UNICODE_CHARACTER_CLASS}
     *                    and {@link java.util.regex.Pattern#COMMENTS}
     * @throws IllegalArgumentException               If bit values other than those corresponding to the defined
     *                                                match flags are set in <tt>flags</tt>
     * @throws java.util.regex.PatternSyntaxException If the expression's syntax is invalid
     */
    public static Observable<OnAppend> replace(String text, String regex, String replacement, int flags) {
        return Observable.create(emitter -> {
            Regex.CancellationSignalImpl cancellationSignal = new Regex.CancellationSignalImpl();
            emitter.setCancellable(cancellationSignal);
            Regex.replace(text, regex, replacement, flags,
                    (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                            emitter.onNext(new OnAppend(fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount))
                    , cancellationSignal);
            emitter.onComplete();
        });
    }

    /**
     * Class received in OnNext() method of Observer. Contains info about current parsed text part.
     * <p>
     * <pre>{@code
     * fromSrc      -> Start position of current part at original text
     * toSrc        -> End position of current part at original text
     * appendSrc    -> Current processed text part from original text
     * fromDst      -> Start position of current part at replaced text
     * toDst        -> End position of current part at replaced text
     * appendDst    -> Replaced text part
     * isMatched    -> Is current part matched to regex
     * progress     -> Current parsing progress (float from 0 - to 1)
     * matchedCount -> Count of matched perts at this moment
     * }</pre>
     */
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

        /**
         * Class contains info about current parsed text part.
         *
         * @param fromSrc      Start position of current part at original text
         * @param toSrc        End position of current part at original text
         * @param appendSrc    Current processed text part from original text
         * @param fromDst      Start position of current part at replaced text
         * @param toDst        End position of current part at replaced text
         * @param appendDst    Replaced text part
         * @param isMatched    Is current part matched to regex
         * @param progress     Current parsing progress (float from 0 - to 1)
         * @param matchedCount Count of matched perts at this moment
         */
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

        /**
         * @return Current parsing progress (float from 0 - to 1)
         */
        public float getProgress() {
            return progress;
        }

        /**
         * @return Count of matched perts at this moment
         */
        public int getMatchedCount() {
            return matchedCount;
        }

        /**
         * @return Start position of current part at original text
         */
        public int getFromSrc() {
            return fromSrc;
        }

        /**
         * @return End position of current part at original text
         */
        public int getToSrc() {
            return toSrc;
        }

        /**
         * @return Start position of current part at replaced text
         */
        public int getFromDst() {
            return fromDst;
        }

        /**
         * @return End position of current part at replaced text
         */
        public int getToDst() {
            return toDst;
        }

        /**
         * @return Replaced text part
         */
        public String getAppendDst() {
            return appendDst;
        }

        /**
         * @return Current processed text part from original text
         */
        public String getAppendSrc() {
            return appendSrc;
        }

        /**
         * @return Is current part matched to regex
         */
        public boolean isMatched() {
            return isMatched;
        }

        @Override
        public String toString() {
            return appendSrc + " -> " + appendDst;
        }
    }


}


