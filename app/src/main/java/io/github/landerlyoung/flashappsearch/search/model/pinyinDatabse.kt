package io.github.landerlyoung.flashappsearch.search.model

import android.arch.persistence.room.*
import android.content.Context
import android.support.annotation.WorkerThread

/**
 * <pre>
 * Author: taylorcyang@tencent.com
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
@Entity(tableName = "hanzi2pinyin")
data class PinyinEntity(val hanzi: String, val pinyin: String) {
    @PrimaryKey
    @ColumnInfo()
    var id: Int = 0
}

@Dao
interface PinyinDao {
    @Query("SELECT * from hanzi2pinyin where hanzi == :hanzi")
    fun queryPinyin(hanzi: String): List<PinyinEntity>
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
            val pinyinFile = context.getDatabasePath(PinyinDataBase.DB_NAME)
            if (!pinyinFile.exists()) {
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