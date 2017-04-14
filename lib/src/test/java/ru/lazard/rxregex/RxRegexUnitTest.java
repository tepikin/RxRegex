package ru.lazard.rxregex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RxRegexUnitTest {


    @Test
    public void testRxCancellation() throws Exception {
        RxRegex.replace("abcd", "bc", "BC", 0)
                .scan(new StringBuffer(), (stringBuffer, onAppend) -> stringBuffer.append(onAppend.getAppendDst())).skip(1)
                .take(1)
                .last(new StringBuffer()).map(stringBuffer -> stringBuffer.toString())
                .subscribe((string) -> assertEquals(string, "a"));
    }


    @Test
    public void testRxReplace() throws Exception {
        RxRegex.replace("abcd", "bc", "BC", 0)
                .scan(new StringBuffer(), (stringBuffer, onAppend) -> stringBuffer.append(onAppend.getAppendDst())).skip(1)
                .last(new StringBuffer()).map(stringBuffer -> stringBuffer.toString())
                .subscribe((string) -> assertEquals(string, "aBCd"));
    }


    @Test
    public void testRxReplaceShort() throws Exception {
        RxRegex.replace("abcd", "bc", "BC")
                .scan(new StringBuffer(), (stringBuffer, onAppend) -> stringBuffer.append(onAppend.getAppendDst())).skip(1)
                .last(new StringBuffer()).map(stringBuffer -> stringBuffer.toString())
                .subscribe((string) -> assertEquals(string, "aBCd"));
    }

    @Test
    public void testRxFind() throws Exception {
        RxRegex.find("abcd", "bc", 0)
                .filter(RxRegex.OnAppend::isMatched)
                .subscribe((onAppend) -> {
                    assertEquals(onAppend.getAppendDst(), "bc");
                    assertEquals(onAppend.getFromDst(), 1);
                    assertEquals(onAppend.getToDst(), 3);
                    assertEquals(onAppend.getFromSrc(), 1);
                    assertEquals(onAppend.getToSrc(), 3);
                    assertEquals(onAppend.isMatched(), true);
                    assertEquals(onAppend.getProgress(), 1f * 3 / 4, 0.01f);
                    assertEquals(onAppend.getMatchedCount(), 1);
                });
    }

    @Test
    public void testRxFindShort() throws Exception {
        RxRegex.find("abcd", "bc")
                .filter(RxRegex.OnAppend::isMatched)
                .subscribe((onAppend) -> {
                    assertEquals(onAppend.getAppendDst(), "bc");
                    assertEquals(onAppend.getAppendSrc(), "bc");
                    assertEquals(onAppend.getFromDst(), 1);
                    assertEquals(onAppend.getToDst(), 3);
                    assertEquals(onAppend.getFromSrc(), 1);
                    assertEquals(onAppend.getToSrc(), 3);
                    assertEquals(onAppend.isMatched(), true);
                    assertEquals(onAppend.getProgress(), 1f * 3 / 4, 0.001f);
                    assertEquals(onAppend.getMatchedCount(), 1);
                });
    }

    @Test
    public void testRxError() throws Exception {
        RxRegex.find("abcd", "")
                .subscribe(onAppend -> {
                }, error -> assertEquals(true, error instanceof IllegalArgumentException));
    }
}