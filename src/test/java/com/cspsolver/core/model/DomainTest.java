package com.cspsolver.core.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Domain class.
 */
class DomainTest {

    @Test
    void testRangeDomain() {
        Domain<Integer> domain = Domain.range(1, 5);

        assertEquals(5, domain.size());
        assertFalse(domain.isEmpty());

        assertTrue(domain.contains(1));
        assertTrue(domain.contains(3));
        assertTrue(domain.contains(5));
        assertFalse(domain.contains(0));
        assertFalse(domain.contains(6));
    }

    @Test
    void testSingletonDomain() {
        Domain<Integer> domain = Domain.singleton(42);

        assertEquals(1, domain.size());
        assertTrue(domain.isSingleton());
        assertTrue(domain.contains(42));
        assertEquals(42, domain.getFirst());
    }

    @Test
    void testOfDomain() {
        Domain<String> domain = Domain.of("red", "green", "blue");

        assertEquals(3, domain.size());
        assertTrue(domain.contains("red"));
        assertTrue(domain.contains("green"));
        assertTrue(domain.contains("blue"));
        assertFalse(domain.contains("yellow"));
    }

    @Test
    void testRemove() {
        Domain<Integer> domain = Domain.range(1, 5);

        assertTrue(domain.remove(3));
        assertEquals(4, domain.size());
        assertFalse(domain.contains(3));

        // Remove again should return false
        assertFalse(domain.remove(3));
        assertEquals(4, domain.size());

        // Remove non-existent value
        assertFalse(domain.remove(100));
    }

    @Test
    void testRestore() {
        Domain<Integer> domain = Domain.range(1, 5);

        domain.remove(3);
        assertFalse(domain.contains(3));

        assertTrue(domain.restore(3));
        assertTrue(domain.contains(3));
        assertEquals(5, domain.size());

        // Restore again should return false
        assertFalse(domain.restore(3));
    }

    @Test
    void testReduceTo() {
        Domain<Integer> domain = Domain.range(1, 5);

        domain.reduceTo(3);

        assertEquals(1, domain.size());
        assertTrue(domain.isSingleton());
        assertTrue(domain.contains(3));
        assertFalse(domain.contains(1));
        assertFalse(domain.contains(5));
    }

    @Test
    void testCheckpointAndRollback() {
        Domain<Integer> domain = Domain.range(1, 5);

        domain.checkpoint();
        assertEquals(5, domain.size());

        domain.remove(1);
        domain.remove(2);
        assertEquals(3, domain.size());

        domain.rollback();
        assertEquals(5, domain.size());
        assertTrue(domain.contains(1));
        assertTrue(domain.contains(2));
    }

    @Test
    void testNestedCheckpoints() {
        Domain<Integer> domain = Domain.range(1, 5);

        domain.checkpoint();  // Checkpoint 1
        domain.remove(1);
        assertEquals(4, domain.size());

        domain.checkpoint();  // Checkpoint 2
        domain.remove(2);
        assertEquals(3, domain.size());

        domain.rollback();  // Back to Checkpoint 2 state (before remove(2))
        assertEquals(4, domain.size());
        assertTrue(domain.contains(2));
        assertFalse(domain.contains(1));

        domain.rollback();  // Back to Checkpoint 1 state (original)
        assertEquals(5, domain.size());
        assertTrue(domain.contains(1));
    }

    @Test
    void testCopy() {
        Domain<Integer> domain = Domain.range(1, 5);
        domain.remove(3);

        Domain<Integer> copy = domain.copy();

        assertEquals(4, copy.size());
        assertFalse(copy.contains(3));

        // Modifying copy shouldn't affect original
        copy.remove(4);
        assertEquals(3, copy.size());
        assertEquals(4, domain.size());
        assertTrue(domain.contains(4));
    }

    @Test
    void testIterator() {
        Domain<Integer> domain = Domain.range(1, 5);
        domain.remove(3);

        List<Integer> values = domain.getValues();
        assertEquals(4, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertFalse(values.contains(3));
        assertTrue(values.contains(4));
        assertTrue(values.contains(5));
    }

    @Test
    void testIteratorRemove() {
        Domain<Integer> domain = Domain.range(1, 5);

        Iterator<Integer> it = domain.iterator();
        while (it.hasNext()) {
            Integer val = it.next();
            if (val % 2 == 0) {
                it.remove();
            }
        }

        assertEquals(3, domain.size());
        assertTrue(domain.contains(1));
        assertFalse(domain.contains(2));
        assertTrue(domain.contains(3));
        assertFalse(domain.contains(4));
        assertTrue(domain.contains(5));
    }

    @Test
    void testEmpty() {
        Domain<Integer> domain = Domain.range(1, 3);

        domain.remove(1);
        domain.remove(2);
        domain.remove(3);

        assertTrue(domain.isEmpty());
        assertEquals(0, domain.size());
    }

    @Test
    void testGetFirst() {
        Domain<Integer> domain = Domain.range(5, 10);
        // First value should be 5 (smallest in range)
        assertNotNull(domain.getFirst());

        domain.reduceTo(7);
        assertEquals(7, domain.getFirst());
    }

    @Test
    void testGetFirstOnEmpty() {
        Domain<Integer> domain = Domain.singleton(1);
        domain.remove(1);

        assertThrows(java.util.NoSuchElementException.class, domain::getFirst);
    }

    @Test
    void testEqualsAndHashCode() {
        Domain<Integer> d1 = Domain.range(1, 5);
        Domain<Integer> d2 = Domain.range(1, 5);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());

        d1.remove(3);
        assertNotEquals(d1, d2);
    }

    @Test
    void testToString() {
        Domain<Integer> domain = Domain.of(1, 2, 3);
        String str = domain.toString();
        assertTrue(str.startsWith("Domain"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }
}
