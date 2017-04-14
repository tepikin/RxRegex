package ru.lazard.rxregex;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RegexUnitTest {


    @Test
    public void testReplace() throws Exception {
        String result = Regex.replace("abcd", "bc", "BC", 0);
        assertEquals(result, "aBCd");
    }

    @Test
    public void testRepeats() throws Exception {
        String result = Regex.replace("abcaad", "a", "BC", 0);
        assertEquals(result, "BCbcBCBCd");
    }

    @Test
    public void testReplaceShort() throws Exception {
        String result = Regex.replace("abcd", "123", "BC");
        assertEquals(result, "abcd");
    }

    @Test
    public void testReplaceGroup() throws Exception {
        String result = Regex.replace("abcd", "(bc)", "_$1_\\n\\r\\t\n\r\t", 0);
        assertEquals(result, "a_bc_\n\r\t\n\r\td");
    }

    @Test
    public void testReplaceListener() throws Exception {
        String calls[] = new String[3];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Regex.replace("abcd", "(bc)", "_$1_", 0, (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                calls[atomicInteger.getAndIncrement()] = appendDst);
        assertArrayEquals(calls, new String[]{"a", "_bc_", "d"});
    }

    @Test
    public void testCancellationSignal() throws Exception {
        String calls[] = new String[3];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        CancellationSignalImpl cancellationSignal = new CancellationSignalImpl();
        cancellationSignal.cancel();
        Regex.replace("abcd", "(bc)", "_$1_", 0, (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                calls[atomicInteger.getAndIncrement()] = appendDst, cancellationSignal);
        assertArrayEquals(calls, new String[3]);
    }
    @Test
    public void testCancellationSignalMiddle() throws Exception {
        String calls[] = new String[3];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        CancellationSignalImpl cancellationSignal = new CancellationSignalImpl();

        Regex.replace("abcd", "(bc)", "_$1_", 0, (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
        {calls[atomicInteger.getAndIncrement()] = appendDst;
            cancellationSignal.cancel();}, cancellationSignal);
        assertArrayEquals(calls, new String[]{"a",null,null});
    }

    @Test
    public void testReplaceListenerShort() throws Exception {
        String calls[] = new String[3];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Regex.replace("abcd", "(bc)", "_$1_", (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) ->
                calls[atomicInteger.getAndIncrement()] = appendDst);
        assertArrayEquals(calls, new String[]{"a", "_bc_", "d"});
    }

    @Test
    public void testFindListener() throws Exception {
        String calls[] = new String[1];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Regex.find("abcd", "(bc)", 0, (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) -> {
            if (isMatched) calls[atomicInteger.getAndIncrement()] = appendDst;
        });
        assertArrayEquals(calls, new String[]{"bc"});
    }

    @Test
    public void testFindListenerShort() throws Exception {
        String calls[] = new String[1];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Regex.find("abcd", "(bc)", (fromSrc, toSrc, appendSrc, fromDst, toDst, appendDst, isMatched, progress, matchedCount) -> {
            if (isMatched) calls[atomicInteger.getAndIncrement()] = appendDst;
        });
        assertArrayEquals(calls, new String[]{"bc"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionEmpty() throws Exception {
        Regex.replace("abcd", "", "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionMatching() throws Exception {
        Regex.replace("abcd", "()", "1");
    }

    private class CancellationSignalImpl implements Regex.CancellationSignal{
        boolean isCanceled;
        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void cancel() {
            isCanceled=true;
        }
    };
}