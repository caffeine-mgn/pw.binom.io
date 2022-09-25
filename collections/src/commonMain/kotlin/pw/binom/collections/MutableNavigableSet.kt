package pw.binom.collections

interface MutableNavigableSet<E> : MutableSortedSet<E> {

    /**
     * Returns the greatest element in this set strictly less than the
     * given element, or `null` if there is no such element.
     *
     * @param e the value to match
     * @return the greatest element less than `e`,
     * or `null` if there is no such element
     * @throws ClassCastException if the specified element cannot be
     * compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     * and this set does not permit null elements
     */
    fun lower(e: E): E

    /**
     * Returns the greatest element in this set less than or equal to
     * the given element, or `null` if there is no such element.
     *
     * @param e the value to match
     * @return the greatest element less than or equal to `e`,
     * or `null` if there is no such element
     * @throws ClassCastException if the specified element cannot be
     * compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     * and this set does not permit null elements
     */
    fun floor(e: E): E

    /**
     * Returns the least element in this set greater than or equal to
     * the given element, or `null` if there is no such element.
     *
     * @param e the value to match
     * @return the least element greater than or equal to `e`,
     * or `null` if there is no such element
     * @throws ClassCastException if the specified element cannot be
     * compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     * and this set does not permit null elements
     */
    fun ceiling(e: E): E

    /**
     * Returns the least element in this set strictly greater than the
     * given element, or `null` if there is no such element.
     *
     * @param e the value to match
     * @return the least element greater than `e`,
     * or `null` if there is no such element
     * @throws ClassCastException if the specified element cannot be
     * compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     * and this set does not permit null elements
     */
    fun higher(e: E): E

    /**
     * Retrieves and removes the first (lowest) element,
     * or returns `null` if this set is empty.
     *
     * @return the first element, or `null` if this set is empty
     */
    fun pollFirst(): E

    /**
     * Retrieves and removes the last (highest) element,
     * or returns `null` if this set is empty.
     *
     * @return the last element, or `null` if this set is empty
     */
    fun pollLast(): E

    /**
     * Returns an iterator over the elements in this set, in ascending order.
     *
     * @return an iterator over the elements in this set, in ascending order
     */
    override fun iterator(): MutableIterator<E>

    /**
     * Returns a reverse order view of the elements contained in this set.
     * The descending set is backed by this set, so changes to the set are
     * reflected in the descending set, and vice-versa.  If either set is
     * modified while an iteration over either set is in progress (except
     * through the iterator's own `remove` operation), the results of
     * the iteration are undefined.
     *
     *
     * The returned set has an ordering equivalent to
     * [Collections.reverseOrder]`(comparator())`.
     * The expression `s.descendingSet().descendingSet()` returns a
     * view of `s` essentially equivalent to `s`.
     *
     * @return a reverse order view of this set
     */
    fun descendingSet(): NavigableSet<E>?

    /**
     * Returns an iterator over the elements in this set, in descending order.
     * Equivalent in effect to `descendingSet().iterator()`.
     *
     * @return an iterator over the elements in this set, in descending order
     */
    fun descendingIterator(): Iterator<E>?

    /**
     * Returns a view of the portion of this set whose elements range from
     * `fromElement` to `toElement`.  If `fromElement` and
     * `toElement` are equal, the returned set is empty unless `fromInclusive` and `toInclusive` are both true.  The returned set
     * is backed by this set, so changes in the returned set are reflected in
     * this set, and vice-versa.  The returned set supports all optional set
     * operations that this set supports.
     *
     *
     * The returned set will throw an `IllegalArgumentException`
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param fromInclusive `true` if the low endpoint
     * is to be included in the returned view
     * @param toElement high endpoint of the returned set
     * @param toInclusive `true` if the high endpoint
     * is to be included in the returned view
     * @return a view of the portion of this set whose elements range from
     * `fromElement`, inclusive, to `toElement`, exclusive
     * @throws ClassCastException if `fromElement` and
     * `toElement` cannot be compared to one another using this
     * set's comparator (or, if the set has no comparator, using
     * natural ordering).  Implementations may, but are not required
     * to, throw this exception if `fromElement` or
     * `toElement` cannot be compared to elements currently in
     * the set.
     * @throws NullPointerException if `fromElement` or
     * `toElement` is null and this set does
     * not permit null elements
     * @throws IllegalArgumentException if `fromElement` is
     * greater than `toElement`; or if this set itself
     * has a restricted range, and `fromElement` or
     * `toElement` lies outside the bounds of the range.
     */
    fun subSet(
        fromElement: E,
        fromInclusive: Boolean,
        toElement: E,
        toInclusive: Boolean
    ): NavigableSet<E>?

    /**
     * Returns a view of the portion of this set whose elements are less than
     * (or equal to, if `inclusive` is true) `toElement`.  The
     * returned set is backed by this set, so changes in the returned set are
     * reflected in this set, and vice-versa.  The returned set supports all
     * optional set operations that this set supports.
     *
     *
     * The returned set will throw an `IllegalArgumentException`
     * on an attempt to insert an element outside its range.
     *
     * @param toElement high endpoint of the returned set
     * @param inclusive `true` if the high endpoint
     * is to be included in the returned view
     * @return a view of the portion of this set whose elements are less than
     * (or equal to, if `inclusive` is true) `toElement`
     * @throws ClassCastException if `toElement` is not compatible
     * with this set's comparator (or, if the set has no comparator,
     * if `toElement` does not implement [Comparable]).
     * Implementations may, but are not required to, throw this
     * exception if `toElement` cannot be compared to elements
     * currently in the set.
     * @throws NullPointerException if `toElement` is null and
     * this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     * restricted range, and `toElement` lies outside the
     * bounds of the range
     */
    fun headSet(toElement: E, inclusive: Boolean): NavigableSet<E>?

    /**
     * Returns a view of the portion of this set whose elements are greater
     * than (or equal to, if `inclusive` is true) `fromElement`.
     * The returned set is backed by this set, so changes in the returned set
     * are reflected in this set, and vice-versa.  The returned set supports
     * all optional set operations that this set supports.
     *
     *
     * The returned set will throw an `IllegalArgumentException`
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param inclusive `true` if the low endpoint
     * is to be included in the returned view
     * @return a view of the portion of this set whose elements are greater
     * than or equal to `fromElement`
     * @throws ClassCastException if `fromElement` is not compatible
     * with this set's comparator (or, if the set has no comparator,
     * if `fromElement` does not implement [Comparable]).
     * Implementations may, but are not required to, throw this
     * exception if `fromElement` cannot be compared to elements
     * currently in the set.
     * @throws NullPointerException if `fromElement` is null
     * and this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     * restricted range, and `fromElement` lies outside the
     * bounds of the range
     */
    fun tailSet(fromElement: E, inclusive: Boolean): NavigableSet<E>?

    /**
     * {@inheritDoc}
     *
     *
     * Equivalent to `subSet(fromElement, true, toElement, false)`.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    fun subSet(fromElement: E, toElement: E): MutableSortedSet<E>?

    /**
     * {@inheritDoc}
     *
     *
     * Equivalent to `headSet(toElement, false)`.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    fun headSet(toElement: E): MutableSortedSet<E>?

    /**
     * {@inheritDoc}
     *
     *
     * Equivalent to `tailSet(fromElement, true)`.
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    fun tailSet(fromElement: E): SortedSet<E>?
}
