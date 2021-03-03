package io.github.landerlyoung.flashappsearch.search.model

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-28
 * Time:   13:07
 * Life with Passion, Code with Creativity.
 * ```
 */

@Entity
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val pinyin: String?,
    val lastUpdated: Long
)

@Dao
interface AppInfoDao {
    @Query("select * from AppInfoEntity")
    fun allAppInfo(): List<AppInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllAppInfo(list: List<AppInfoEntity>)

    @Query("delete from AppInfoEntity")
    fun clearAll()

    @Delete
    fun delete(list: List<AppInfoEntity>)
}

@Database(
    entities = [AppInfoEntity::class],
    version = 1
)
abstract class AppInfoDataBase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao

    companion object {
        private const val DB_NAME = "app_info.db"

        fun createDb(context: Context) =
            Room.databaseBuilder(context, AppInfoDataBase::class.java, DB_NAME)
                .build()
    }
}