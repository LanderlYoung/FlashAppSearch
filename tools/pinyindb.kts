import java.io.*
import java.nio.file.Files.lines
import java.util.stream.Stream
import javax.xml.stream.events.Characters
import kotlin.system.exitProcess
import kotlin.text.*

// parse file downloaded from
// https://www.unicode.org/Public/UCD/latest/ucd/Unihan.zip

/**
 * like
 * U+2B6E2
 */
fun parseUnicode(unicode: String): String {
    val hex = unicode.substring("U+".length)
    val codePoint = hex.toInt(16)
    return String(intArrayOf(codePoint), 0, 1)
}

/**
a: ["a", "\u0101", "\u00e1", "\u01ce", "\u00e0"],
e: ["e", "\u0113", "\u00e9", "\u011b", "\u00e8"],
i: ["i", "\u012b", "\u00ed", "\u01d0", "\u00ec"],
o: ["o", "\u014d", "\u00f3", "\u01d2", "\u00f2"],
u: ["u", "\u016b", "\u00fa", "\u01d4", "\u00f9"],
v: ["\u00fc", "\u01d6", "\u01d8", "\u01da", "\u01dc"]
};
 */
val toneMarks: Map<Char, String> = mutableMapOf<Char, String>().apply {
    listOf('\u0101', '\u00e1', '\u01ce', '\u00e0').forEachIndexed { i, c ->
        put(c, "a${i + 1}")
    }
    listOf('\u0113', '\u00e9', '\u011b', '\u00e8').forEachIndexed { i, c ->
        put(c, "e${i + 1}")
    }
    listOf('\u012b', '\u00ed', '\u01d0', '\u00ec').forEachIndexed { i, c ->
        put(c, "i${i + 1}")
    }
    listOf('\u014d', '\u00f3', '\u01d2', '\u00f2').forEachIndexed { i, c ->
        put(c, "o${i + 1}")
    }
    listOf('\u016b', '\u00fa', '\u01d4', '\u00f9').forEachIndexed { i, c ->
        put(c, "u${i + 1}")
    }
    listOf('\u01d6', '\u01d8', '\u01da', '\u01dc').forEachIndexed { i, c ->
        put(c, "v${i + 1}")
    }
}

fun toAsciiPinyin(pinyin: String) =
        StringBuilder(pinyin.length + 1).apply {
            var tone: Char? = null
            pinyin.forEach {
                toneMarks[it]?.also {
                    append(it[0])
                    tone = it[1]
                } ?: append(it)
            }
            tone?.let {
                append(it)
            }
        }.toString()

/**
 * @param readingPath path for Unihan_Readings.txt
 * @return (肥, féi, fei2)
 */
fun parseFile(readingPath: String) =
        BufferedReader(FileReader(readingPath)).lineSequence()
                .map { line: String ->
                    line.split("\\s".toRegex())
                }
                .filter {
                    // U+343A	kHanyuPinyin	10124.030:yín,zhòng
                    // U+9577	kHanyuPinlu	zhǎng(1879) cháng(1179)
                    it.size >= 3 && (it[1] == "kHanyuPinlu" || it[1] == "kHanyuPinyin")
                }.map {
                    val hanzi = it[0]
                    if (it[1] == "kHanyuPinlu") {
                        it.subList(2, it.size).asSequence().map {
                            val pinyin = it.substring(0, it.indexOf('('))
                            Triple(parseUnicode(hanzi), pinyin, toAsciiPinyin(pinyin))
                        }
                    } else {
                        it[2].substring(it[2].indexOf(':') + 1)
                                .split(',')
                                .asSequence()
                                .map { it.trim() }
                                .map { pinyin ->
                                    Triple(parseUnicode(hanzi), pinyin, toAsciiPinyin(pinyin))
                                }
                    }
                }.flatMap {
                    it.asSequence()
                }.distinct()


fun runSqlite3(dbFile: File): OutputStreamWriter {
    dbFile.delete()
    val process = ProcessBuilder(listOf("/usr/bin/sqlite3", dbFile.absolutePath))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
    return OutputStreamWriter(process.outputStream, Charsets.UTF_8)
}

fun createPinyinDb(source: Sequence<Triple<String, String, String>>, dbFile: File) {
    runSqlite3(dbFile).use {
        it.apply {
            write("""
                CREATE TABLE hanzi2pinyin
                (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    hanzi TEXT NOT NULL,
                    pinyin TEXT NOT NULL
                );
            """.trimMargin())
            // write(""" CREATE INDEX hanzi_index ON hanzi2pinyin (hanzi); """)

            var count = 0
            source.forEach {
                write("""
            INSERT INTO hanzi2pinyin (hanzi,  pinyin)
            VALUES ('${it.first}', '${it.third}'); """)

                count++
            }
            write(".exit")

            println("createDb ${dbFile.name} $count records")
        }
    }
}

// frequently used 3500 simplified/traditional
// downloaded from https://github.com/kaienfr/Font/blob/master/learnfiles/chinese%E7%AE%80%E7%B9%81%E5%B8%B8%E7%94%A8%E5%AD%97%E8%A1%A8.txt
fun frequentlyUsedHanzi() =
        BufferedReader(FileReader(args[1])).lineSequence()
                .flatMap { string ->
                    mutableListOf<String>().also {
                        for (i in 0 until string.length) {
                            if (i + 1 < string.length && Character.isSurrogatePair(string[i], string[i + 1])) {
                                it.add(string.substring(i, i + 2))
                            } else {
                                it.add(string.substring(i, i + 1))
                            }
                        }
                    }.asSequence()
                }
                .toCollection(hashSetOf())

if ("debug" in args) {
    parseFile(args[0]).forEach { println(it) }
    frequentlyUsedHanzi().apply {
        println(this)
        println(this.size)
    }
    exitProcess(0)
}

// All hanzi
createPinyinDb(parseFile(args[0]), File("build/pinyin-all.db"))

createPinyinDb(frequentlyUsedHanzi().let { fre ->
    parseFile(args[0]).filter {
        it.first in fre
    }
}, File("build/pinyin-frequently-3500.db"))


