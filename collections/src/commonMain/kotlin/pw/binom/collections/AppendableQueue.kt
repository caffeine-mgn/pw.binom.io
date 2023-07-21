package pw.binom.collections

interface AppendableQueue<T> : Queue<T> {
  fun push(value: T)
}
