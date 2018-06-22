import java.io.*
import java.nio.file.Files.lines
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
a: ["a", "\u0101", "\u00e1", "\u01ce", "\u00e0", "a"],
e: ["e", "\u0113", "\u00e9", "\u011b", "\u00e8", "e"],
i: ["i", "\u012b", "\u00ed", "\u01d0", "\u00ec", "i"],
o: ["o", "\u014d", "\u00f3", "\u01d2", "\u00f2", "o"],
u: ["u", "\u016b", "\u00fa", "\u01d4", "\u00f9", "u"],
v: ["\u00fc", "\u01d6", "\u01d8", "\u01da", "\u01dc", "\u00fc"]
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
        BufferedReader(FileReader(readingPath)).lines()
                .map { line: String ->
                    line.split("\\s".toRegex())
                }
                .filter {
                    // U+343A	kHanyuPinyin	10124.030:yín,zhòng
                    it.size >= 3 && it[1] == "kHanyuPinyin"
                }.map {
                    it[2].substring(it[2].indexOf(':')).split(',')
                            .map { it.trim() }
                            .map { pinyin ->
                                Triple(parseUnicode(it[0]), pinyin, toAsciiPinyin(pinyin))
                            }
                }.flatMap {
                    it.stream()
                }

fun runSqlite3(): OutputStream {
    return ProcessBuilder(listOf("/usr/bin/sqlite3", "pinyin-all.db"))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
            .outputStream
}

OutputStreamWriter(runSqlite3(), Charsets.UTF_8).use {

    it.apply {
        write("""
CREATE TABLE han_pinyin
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    hanzi TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    asciiPinyin TEXT NOT NULL
);
        """)
        write(""" CREATE INDEX han_index ON han_pinyin (hanzi); """)
        flush()

        parseFile(args[0]).forEach {
            write("""
            INSERT INTO han_pinyin (hanzi, pinyin, asciiPinyin)
            VALUES ('${it.first}', '${it.second}', '${it.third}'); """)
            flush()
        }
        write(".exit")
    }

}
