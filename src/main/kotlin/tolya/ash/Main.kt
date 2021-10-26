package tolya.ash

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun twoPtrs(le: Int, r: Int, lst: MutableList<Int>): Pair<Int, Int> {
    val ind = Random.nextInt(le, r)
    val x = lst[ind]
    var i = le
    var j = r - 1
    while (i <= j) {
        while (i <= j && lst[i] < x) ++i
        while (i <= j && lst[j] > x) --j
        if (i <= j) {
            val c = lst[i]
            lst[i] = lst[j]
            lst[j] = c
            ++i
            --j
        }
    }
    return (j + 1) to i
}

fun seqSort(le: Int, r: Int, lst: MutableList<Int>) {
    if (r - le <= 1) return
    val (p1, p2) = twoPtrs(le, r, lst)
    seqSort(le, p1, lst)
    seqSort(p2, r, lst)
}

val executor = Executors.newFixedThreadPool(4)

fun parSort(le: Int, r: Int, lst: MutableList<Int>, d: Int): CompletableFuture<*> {
    if (r - le <= 1) return CompletableFuture.completedFuture(Unit)
    if (d == 0) {
        seqSort(le, r, lst)
        return CompletableFuture.completedFuture(Unit)
    }
    val (p1, p2) = twoPtrs(le, r, lst)
    val fLe = CompletableFuture
        .supplyAsync({ parSort(le, p1, lst, d - 1) }, executor)
        .thenCompose { it }
    val fR = parSort(p2, r, lst, d - 1)
    return fR.thenCombine(fLe) { _, _ -> }
}

suspend fun parSort2(le: Int, r: Int, lst: MutableList<Int>, d: Int): Unit = coroutineScope {
    //check(Thread.currentThread().name.startsWith("pool-"))
    if (r - le <= 1) return@coroutineScope
    if (d == 0) {
        seqSort(le, r, lst)
        return@coroutineScope
    }
    val (p1, p2) = twoPtrs(le, r, lst)

    launch {
        parSort2(le, p1, lst, d - 1)
    }
    parSort2(p2, r, lst, d - 1)
}

fun main1() {
    val n = 1_00_000_000
    val d = 5 // log log n
    val lst1 = MutableList(n) { Random.nextInt(-1_000_000_000, 1_000_000_000) }
    val lst2 = lst1.toMutableList()
    val lst3 = lst1.toMutableList()
    //val lst4 = lst1.toMutableList().also { it.sort() }
    val t1 = measureTimeMillis {
        seqSort(0, n, lst1)
    }
    println(t1)
    val t2 = measureTimeMillis {
        parSort(0, n, lst2, d).get()
    }
    println(t2)
    val t3 = measureTimeMillis {
        runBlocking(executor.asCoroutineDispatcher()) {
            parSort2(0, n, lst3, d)
        }
    }
    println(t3)

    println(t1.toDouble() / t2)
    println(t1.toDouble() / t3)
//    for (i in 0 until n) {
//        check(lst1[i] == lst4[i])
//        check(lst2[i] == lst4[i])
//        check(lst3[i] == lst4[i])
//    }
}

fun main() {
    for (i in 0 until 5) {
        main1()
    }
    executor.shutdown()
}
