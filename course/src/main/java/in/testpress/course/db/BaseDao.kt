package `in`.testpress.course.db

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update

interface BaseDao <T> {
    @Insert
    fun insert(vararg obj: T)

    @Update
    fun update(vararg obj: T)

    @Delete
    fun delete(obj: T)
}