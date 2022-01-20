package tolya.ash.bfs

class SeqBfs(val neigh: List<Int>, val ptrs: List<Int>, val startNode: Int) {
    val n = ptrs.size - 1;
    val ans = MutableList(n) { -1 }

    fun calcAns() {
        ans[startNode] = 0
        val q = ArrayDeque<Int>()
        q.addLast(startNode)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            val newD = ans[v] + 1
            neigh.subList(ptrs[v], ptrs[v + 1]).forEach { u ->
                if (ans[u] == -1) {
                    ans[u] = newD
                    q.addLast(u)
                }
            }
        }
    }
}
