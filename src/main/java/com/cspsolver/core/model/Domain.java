package com.cspsolver.core.model;

import java.util.*;

/**
 * Represents the domain of possible values for a CSP variable.
 * Uses BitSet for O(1) membership operations and efficient iteration.
 * Supports checkpoint/rollback for backtracking search.
 *
 * @param <T> the type of values in the domain
 */
public class Domain<T> implements Iterable<T> {

    private final List<T> values;           // Original values indexed for O(1) access
    private final Map<T, Integer> valueIndex; // Value to index mapping
    private BitSet activeMask;              // Current active values
    private int size;                       // Cached size for O(1) access

    // Stack of saved states for backtracking
    private final Deque<BitSet> checkpoints = new ArrayDeque<>();

    /**
     * Creates a domain with the given values.
     */
    public Domain(Collection<T> values) {
        this.values = new ArrayList<>(values);
        this.valueIndex = new HashMap<>();
        for (int i = 0; i < this.values.size(); i++) {
            this.valueIndex.put(this.values.get(i), i);
        }
        this.activeMask = new BitSet(this.values.size());
        this.activeMask.set(0, this.values.size());
        this.size = this.values.size();
    }

    /**
     * Private constructor for copying.
     */
    private Domain(List<T> values, Map<T, Integer> valueIndex, BitSet activeMask, int size) {
        this.values = values; // Share the immutable value list
        this.valueIndex = valueIndex; // Share the immutable index map
        this.activeMask = (BitSet) activeMask.clone();
        this.size = size;
    }

    /**
     * Creates an integer range domain [min, max].
     */
    @SuppressWarnings("unchecked")
    public static Domain<Integer> range(int min, int max) {
        List<Integer> vals = new ArrayList<>(max - min + 1);
        for (int i = min; i <= max; i++) {
            vals.add(i);
        }
        return new Domain<>(vals);
    }

    /**
     * Creates a singleton domain with a single value.
     */
    public static <T> Domain<T> singleton(T value) {
        return new Domain<>(Collections.singletonList(value));
    }

    /**
     * Creates a domain from varargs.
     */
    @SafeVarargs
    public static <T> Domain<T> of(T... values) {
        return new Domain<>(Arrays.asList(values));
    }

    /**
     * Returns the number of active values in the domain.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the domain is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if the domain contains a single value.
     */
    public boolean isSingleton() {
        return size == 1;
    }

    /**
     * Returns true if the value is currently in the domain.
     */
    public boolean contains(T value) {
        Integer idx = valueIndex.get(value);
        return idx != null && activeMask.get(idx);
    }

    /**
     * Removes a value from the domain.
     * @return true if the value was present and removed
     */
    public boolean remove(T value) {
        Integer idx = valueIndex.get(value);
        if (idx != null && activeMask.get(idx)) {
            activeMask.clear(idx);
            size--;
            return true;
        }
        return false;
    }

    /**
     * Restores a previously removed value to the domain.
     * @return true if the value was restored
     */
    public boolean restore(T value) {
        Integer idx = valueIndex.get(value);
        if (idx != null && !activeMask.get(idx)) {
            activeMask.set(idx);
            size++;
            return true;
        }
        return false;
    }

    /**
     * Reduces the domain to a single value.
     */
    public void reduceTo(T value) {
        Integer idx = valueIndex.get(value);
        if (idx == null) {
            throw new IllegalArgumentException("Value not in original domain: " + value);
        }
        activeMask.clear();
        activeMask.set(idx);
        size = 1;
    }

    /**
     * Gets the first (or only) value in the domain.
     * Useful for singleton domains.
     */
    public T getFirst() {
        int idx = activeMask.nextSetBit(0);
        if (idx < 0) {
            throw new NoSuchElementException("Domain is empty");
        }
        return values.get(idx);
    }

    /**
     * Gets all active values as a list.
     */
    public List<T> getValues() {
        List<T> result = new ArrayList<>(size);
        for (int i = activeMask.nextSetBit(0); i >= 0; i = activeMask.nextSetBit(i + 1)) {
            result.add(values.get(i));
        }
        return result;
    }

    /**
     * Saves the current state for later rollback.
     */
    public void checkpoint() {
        checkpoints.push((BitSet) activeMask.clone());
    }

    /**
     * Restores the most recently saved state.
     */
    public void rollback() {
        if (checkpoints.isEmpty()) {
            throw new IllegalStateException("No checkpoint to rollback to");
        }
        activeMask = checkpoints.pop();
        size = activeMask.cardinality();
    }

    /**
     * Discards the most recent checkpoint without rolling back.
     */
    public void commit() {
        if (!checkpoints.isEmpty()) {
            checkpoints.pop();
        }
    }

    /**
     * Returns the number of saved checkpoints.
     */
    public int getCheckpointDepth() {
        return checkpoints.size();
    }

    /**
     * Creates a shallow copy of this domain (shares value list, copies BitSet).
     */
    public Domain<T> copy() {
        return new Domain<>(values, valueIndex, activeMask, size);
    }

    /**
     * Clears all checkpoints.
     */
    public void clearCheckpoints() {
        checkpoints.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return new DomainIterator();
    }

    private class DomainIterator implements Iterator<T> {
        private int currentIndex = -1;
        private int nextIndex;

        DomainIterator() {
            nextIndex = activeMask.nextSetBit(0);
        }

        @Override
        public boolean hasNext() {
            return nextIndex >= 0;
        }

        @Override
        public T next() {
            if (nextIndex < 0) {
                throw new NoSuchElementException();
            }
            currentIndex = nextIndex;
            nextIndex = activeMask.nextSetBit(nextIndex + 1);
            return values.get(currentIndex);
        }

        @Override
        public void remove() {
            if (currentIndex < 0) {
                throw new IllegalStateException();
            }
            if (activeMask.get(currentIndex)) {
                activeMask.clear(currentIndex);
                size--;
            }
        }
    }

    @Override
    public String toString() {
        return "Domain" + getValues();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domain<?> domain)) return false;
        return Objects.equals(getValues(), domain.getValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValues());
    }
}
