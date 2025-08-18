package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        correct.addLast(1);
        correct.addLast(5);
        correct.addLast(1453);

        broken.addLast(1);
        broken.addLast(5);
        broken.addLast(1453);

        assertEquals(correct.size(), broken.size());
        assertEquals(correct.removeLast(), broken.removeLast());
        assertEquals(correct.removeLast(), broken.removeLast());
        assertEquals(correct.removeLast(), broken.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 50000;
        for (int i = 0; i < N; i++) {
            int operationNum = StdRandom.uniform(0, 4);
            if(operationNum == 0) {
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNum == 1) {
                int sizeL = L.size();
                int sizeB = B.size();
                assertEquals(sizeL, sizeB);
            } else if (operationNum == 2) {
                if (L.size() == 0 || B.size() == 0) {
                    continue;
                }
                int LastItemL = L.getLast();
                int LastItemB = B.getLast();
                assertEquals(LastItemL, LastItemB);
            } else {
                if (L.size() == 0 || B.size() == 0) {
                    continue;
                }
                int RemoveItemL = L.removeLast();
                int RemoveItemB = B.removeLast();
                assertEquals(RemoveItemL, RemoveItemB);
            }
        }
    }
}
