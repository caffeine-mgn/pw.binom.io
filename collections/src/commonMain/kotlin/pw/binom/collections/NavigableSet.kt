package pw.binom.collections

interface NavigableSet<E> : SortedSet<E> {
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
  fun lower(e: E): E?

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
  fun floor(e: E): E?

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
  fun ceiling(e: E): E?

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
  fun higher(e: E): E?

  /**
   * Retrieves and removes the first (lowest) element,
   * or returns `null` if this set is empty.
   *
   * @return the first element, or `null` if this set is empty
   */
  fun pollFirst(): E?

  /**
   * Retrieves and removes the last (highest) element,
   * or returns `null` if this set is empty.
   *
   * @return the last element, or `null` if this set is empty
   */
  fun pollLast(): E?
}
