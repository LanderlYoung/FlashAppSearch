package io.github.landerlyoung.flashappsearch.search.model

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * <pre>
 * Author: landerlyoung@gmail.com
 * Date:   2018-06-23
 * Time:   11:52
 * Life with Passion, Code with Creativity.
 * </pre>
 */

/*
 *
 * ```sql
 * CREATE TABLE hanzi2pinyin
 * (
 * id INTEGER PRIMARY KEY AUTOINCREMENT,
 * hanzi TEXT NOT NULL,
 * pinyin TEXT NOT NULL
 * );
 * ```
 */
@Entity(
    tableName = "hanzi2pinyin",
    indices = [Index(name = "hanzi_index", value = ["hanzi"])]
)
data class PinyinEntity(
    @PrimaryKey
    var id: Int = 0,
    val hanzi: String,
    val pinyin: String
)

@Dao
interface PinyinDao {
    @Query("SELECT pinyin from hanzi2pinyin where hanzi == :hanzi")
    fun queryPinyin(hanzi: String): List<String>
}

@Database(
        entities = [PinyinEntity::class],
        version = 1
)
abstract class PinyinDataBase : RoomDatabase() {
    abstract fun pinyinDao(): PinyinDao

    companion object {
        private const val DB_NAME = "pinyin.db"

        @WorkerThread
        fun createDb(context: Context): PinyinDataBase {
            val packageUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            val pinyinFile = context.getDatabasePath(DB_NAME)
            if (!pinyinFile.exists() || pinyinFile.lastModified() < packageUpdateTime) {
                pinyinFile.delete()
                // copy asset to file
                context.resources.assets.open("pinyin-frequently-3500.db").use { input ->
                    pinyinFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            return Room.databaseBuilder(context, PinyinDataBase::class.java, DB_NAME)
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
        }
    }
}