package edu.uga.cs.shopsync.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An ArrayList that also maintains a Set of its elements. This allows for constant time
 * {@link #contains(Object)} operations.
 *
 * @param <T> The type of elements in this list.
 */
public class ArraySetList<T> extends ArrayList<T> {

    private final Set<T> set = new HashSet<>();

    @Override
    public boolean contains(@Nullable Object o) {
        return set.contains(o);
    }

    @Override
    public boolean add(T t) {
        set.add(t);
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        set.add(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        set.addAll(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        set.addAll(c);
        return super.addAll(index, c);
    }

    @Override
    public T remove(int index) {
        T element = super.remove(index);
        set.remove(element);
        return element;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        set.remove(o);
        return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            set.remove(get(i));
        }
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        set.removeAll(c);
        return super.removeAll(c);
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super T> filter) {
        set.removeIf(filter);
        return super.removeIf(filter);
    }
}
