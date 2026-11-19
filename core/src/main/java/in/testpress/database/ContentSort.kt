package `in`.testpress.database

object ContentSort {
    const val GREENDAO_ORDER_BY = "T.'POSITION' IS NULL, T.'POSITION' ASC, T.'ORDER' ASC, T.'ID' ASC"
}
