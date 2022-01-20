package tolya.ash.bfs

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

const val N = 400
val executor = Executors.newFixedThreadPool(4)


fun main() {
    val neigh = MutableList(N * N * N * 6) { 0 }
    val ptrs = MutableList(N * N * N + 1) { 0 }
    for (i in 0 until N) {
        for (j in 0 until N) {
            for (k in 0 until N) {
                val num = i * N * N + j * N + k
                var curPtr = ptrs[num]
                if (i > 0) {
                    neigh[curPtr++] = num - N * N
                }
                if (i < N - 1) {
                    neigh[curPtr++] = num + N * N
                }
                if (j > 0) {
                    neigh[curPtr++] = num - N
                }
                if (j < N - 1) {
                    neigh[curPtr++] = num + N
                }
                if (k > 0) {
                    neigh[curPtr++] = num - 1
                }
                if (k < N - 1) {
                    neigh[curPtr++] = num + 1
                }
                ptrs[num + 1] = curPtr
            }
        }
    }

    repeat(10) {
        val t1 = run {
            val parSolver = ParBfs(neigh, ptrs, 0)
            val res = measureTimeMillis {
                runBlocking(executor.asCoroutineDispatcher()) {
                    parSolver.calcAns()
                }
            }
            for (i in 0 until (N * N * N)) {
                val ans = i / N / N + i / N % N + i % N
                check(parSolver.ans[i] == ans) { "$i $ans ${parSolver.ans[i]}"}
            }
            res
        }
        val t2 = run {
            val seqSolver = SeqBfs(neigh, ptrs, 0)
            val res = measureTimeMillis {
                seqSolver.calcAns()
            }
//        for (i in 0 until (N * N * N)) {
//            val ans = i / N / N + i / N % N + i % N
//            check(seqSolver.ans[i] == ans)
//        }
            res
        }
        println("$t1 $t2 ${t2.toDouble() / t1}")
    }
    executor.shutdown()
}
