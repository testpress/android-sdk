package `in`.testpress.database

import `in`.testpress.database.entities.RemoteKeys
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update


interface BaseDao <T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(obj: List<T>)

    @Update
    fun update(vararg obj: T)

    @Delete
    fun delete(vararg obj: T)
}