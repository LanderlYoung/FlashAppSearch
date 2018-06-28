package io.github.landerlyoung.flashappsearch.search.model

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
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