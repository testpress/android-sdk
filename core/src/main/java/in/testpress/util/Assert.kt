package `in`.testpress.util

class Assert {

    companion object {

        @JvmStatic
        fun assertNotNull(message: String, any: Any?) {
            require(any != null) { message }
        }

        @JvmStatic
        fun assertNotNullAndNotEmpty(message: String, string: String?) {
            require(!string.isNullOrEmpty()) { message }
        }
    }
}