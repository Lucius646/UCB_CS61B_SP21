package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void testDeque() {
        StudentArrayDeque<Integer> st = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ad = new ArrayDequeSolution<>();

        int n = 5000;
        String str = "Test begins:\n";

        for (int i = 0; i < n; i++) {
            int operator = StdRandom.uniform(0, 4);
            if (operator == 0) {
                // addFirst method
                int num = StdRandom.uniform(0, 100);
                st.addFirst(num);
                ad.addFirst(num);
                str += "addFirst(" + num + ")\n";
            } else if (operator == 1) {
                // addLast method
                int num = StdRandom.uniform(0, 100);
                st.addLast(num);
                ad.addLast(num);
                str += "addLast(" + num + ")\n";
            } else if (operator == 2 && !st.isEmpty()) {
                // removeFirst method
                str += "removeFirst()\n";
                Integer expected = ad.removeFirst();
                Integer actual = st.removeFirst();
                assertEquals(str, expected, actual);
            } else if (operator == 3 && !st.isEmpty()) {
                // removeLast method
                str += "removeLast()\n";
                Integer expected = ad.removeLast();
                Integer actual = st.removeLast();
                assertEquals(str, expected, actual);
            }
        }
    }
}
