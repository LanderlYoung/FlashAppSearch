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
import androidx.annotation.Keep
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.github.landerlyoung.flashappsearch.search.repo.PinyinSequence

/*
 * ```
 * Author: landerlyoung@gmail.com
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
        val pinyin: PinyinSequence?,
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
@TypeConverters(Converters::class)
abstract class AppInfoDataBase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao

    companion object {
        private const val DB_NAME = "app_info.db"

        fun createDb(context: Context) =
                Room.databaseBuilder(context, AppInfoDataBase::class.java, DB_NAME)
                        .build()
    }
}

@Keep
class Converters {
    @TypeConverter
    fun serializePinyinSequence(it: PinyinSequence): String =
        it.serializeToString()

    @TypeConverter
    fun deserializePinyinSequence(string: String): PinyinSequence =
            PinyinSequence.deserializeFromString(string)
}