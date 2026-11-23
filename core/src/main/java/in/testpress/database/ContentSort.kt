package `in`.testpress.database

object ContentSort {
    /** Sorts by API position (NULLs last; rows without a position sort last), then by order, then id. */
    const val GREENDAO_ORDER_BY =
        "T.\"POSITION\" IS NULL, T.\"POSITION\" ASC, T.\"ORDER\" ASC, T.\"ID\" ASC"
}
