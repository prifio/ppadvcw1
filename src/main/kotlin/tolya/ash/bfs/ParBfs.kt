package tolya.ash.bfs

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.math.min

class ParBfs(val neigh: List<Int>, val ptrs: List<Int>, val startNode: Int) {
    val n = ptrs.size - 1;

    //val inSet = List(n) { AtomicBoolean(false) }
    val inSet = AtomicIntegerArray(n)
    val ans = MutableList(n) { -1 }
    val mem = MutableList(N * N) { 0 }
    val mem2 = List(32) { MutableList(N * N) { 0 } }

    private suspend fun doBfsPar(front: List<Int>, fSize: Int, d: Int): Pair<List<Int>, Int> = coroutineScope {
        val cntBlocks = min(32, (fSize + 999) / 1000)
        val toFlatMap = (0 until cntBlocks).map { i ->
            async {
                val le = fSize * i / cntBlocks
                val r = fSize * (i + 1) / cntBlocks
                val newFrontPart = mem2[i]
                var sz = 0
                front.subList(le, r).forEach { v ->
                    neigh.subList(ptrs[v], ptrs[v + 1]).forEach { u ->
                        if (inSet.getAndIncrement(u) == 0) {
                            ans[u] = d + 1
                            newFrontPart[sz++] = u
                        }
                    }
                }
                newFrontPart to sz
            }
        }.map { it.await() }
        val psums = MutableList(cntBlocks + 1) { 0 }
        for (i in 0 until cntBlocks) {
            psums[i + 1] = psums[i] + toFlatMap[i].second
        }
        val newFront = mem // actually, newfront and front in same memory
        toFlatMap.mapIndexed { i, (newFrontPart, sz) ->
            launch {
                for (j in 0 until sz) {
                    newFront[psums[i] + j] = newFrontPart[j]
                }
            }
        }.forEach { it.join() }
        newFront to psums[cntBlocks]
    }

    suspend fun calcAns() {
        ans[startNode] = 0
        inSet.getAndIncrement(startNode)
        var front = listOf(startNode)
        var sz = 1
        var d = 0
        while (sz > 0) {
            val (newFront, newSize) = doBfsPar(front, sz, d)
            front = newFront
            sz = newSize
            ++d
        }
    }
}


