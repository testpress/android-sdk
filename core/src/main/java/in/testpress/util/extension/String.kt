package `in`.testpress.util.extension

fun String?.isNotNullAndNotEmpty() = this != null && this.isNotEmpty()

fun List<String>.validateHttpAndHttpsUrls(): List<String> {
    return this.filter { it.startsWith("http://") || it.startsWith("https://") }
}