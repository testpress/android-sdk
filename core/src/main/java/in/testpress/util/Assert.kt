package `in`.testpress.util

class Assert {

    companion object {

        @JvmStatic
        fun assertNotNull(message: String, any: Any?){
            if (any == null){
                throw IllegalArgumentException(message);
            }
        }

        @JvmStatic
        fun assertNotNullAndNotEmpty(message: String, string: String?){
            if (string.isNullOrEmpty()) {
                throw IllegalArgumentException(message);
            }
        }

    }

}