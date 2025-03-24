package `in`.testpress.store.util

fun generateRandom10CharString(): String {
    return (1..10)
        .map { ('a'..'z').toList() + ('0'..'9').toList() }
        .map { it.random() }
        .joinToString("")
}