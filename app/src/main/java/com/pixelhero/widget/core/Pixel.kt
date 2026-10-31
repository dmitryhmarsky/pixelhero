package com.pixelhero.widget.core

/** Пиксельный холст в IntArray (ARGB). Без Android API: на Android массив превращается в Bitmap одной строкой. */

fun rgb(v: Int): Int = v or (0xFF shl 24)

fun lerpColor(a: Int, b: Int, t: Float): Int {
    val ar = (a shr 16) and 0xFF
    val ag = (a shr 8) and 0xFF
    val ab = a and 0xFF
    val br = (b shr 16) and 0xFF
    val bg = (b shr 8) and 0xFF
    val bb = b and 0xFF
    val r = (ar + (br - ar) * t).toInt()
    val g = (ag + (bg - ag) * t).toInt()
    val bl = (ab + (bb - ab) * t).toInt()
    return rgb((r shl 16) or (g shl 8) or bl)
}

class Canvas(val w: Int, val h: Int) {
    val px = IntArray(w * h)

    fun clear(c: Int) = px.fill(c)

    fun put(x: Int, y: Int, c: Int) {
        if (x in 0 until w && y in 0 until h) px[y * w + x] = c
    }

    fun rect(x: Int, y: Int, rw: Int, rh: Int, c: Int) {
        for (j in y until y + rh) for (i in x until x + rw) put(i, j, c)
    }

    fun sprite(art: Array<String>, x: Int, y: Int, flip: Boolean, pal: (Char) -> Int) {
        for (j in art.indices) {
            val row = art[j]
            val n = row.length
            for (i in 0 until n) {
                val ch = row[if (flip) n - 1 - i else i]
                if (ch == '.') continue
                val c = pal(ch)
                if (c != 0) put(x + i, y + j, c)
            }
        }
    }

    /**
     * Спрайт с объёмом: пиксели у верхней/левой границы подсвечиваются, у нижней/правой затемняются
     * (граница = прозрачность или обводка 'k'). Свет падает сверху слева.
     */
    fun spriteShaded(art: Array<String>, x: Int, y: Int, flip: Boolean, pal: (Char) -> Int) {
        val h = art.size
        val w = art[0].length
        val g = Array(h) { j -> CharArray(w) { i -> art[j][if (flip) w - 1 - i else i] } }
        fun solid(i: Int, j: Int): Boolean = i in 0 until w && j in 0 until h && g[j][i] != '.' && g[j][i] != 'k'
        for (j in 0 until h) for (i in 0 until w) {
            val ch = g[j][i]
            if (ch == '.') continue
            var c = pal(ch)
            if (c == 0) continue
            if (ch != 'k') {
                if (!solid(i, j + 1)) c = lerpColor(c, 0xFF000000.toInt(), 0.28f)
                else if (!solid(i, j - 1)) c = lerpColor(c, 0xFFFFFFFF.toInt(), 0.25f)
                else if (!solid(i + 1, j)) c = lerpColor(c, 0xFF000000.toInt(), 0.14f)
                else if (!solid(i - 1, j)) c = lerpColor(c, 0xFFFFFFFF.toInt(), 0.12f)
            }
            put(x + i, y + j, c)
        }
    }

    /** Мягкая тень-эллипс на земле (затемняет то, что уже нарисовано). */
    fun shadow(cx: Int, y: Int, halfW: Int) {
        for (i in -halfW..halfW) {
            val edge = Math.abs(i) > halfW - 2
            val yy = y
            if (cx + i in 0 until w && yy in 0 until h) {
                px[yy * w + cx + i] = lerpColor(px[yy * w + cx + i], 0xFF000000.toInt(), if (edge) 0.18f else 0.34f)
            }
            if (!edge && cx + i in 0 until w && yy + 1 in 0 until h) {
                px[(yy + 1) * w + cx + i] = lerpColor(px[(yy + 1) * w + cx + i], 0xFF000000.toInt(), 0.16f)
            }
        }
    }

    fun textWidth(s: String): Int = if (s.isEmpty()) 0 else s.length * 4 - 1

    fun text(s: String, x: Int, y: Int, color: Int, shadow: Boolean = true) {
        if (shadow) drawText(s, x + 1, y + 1, 0xFF000000.toInt())
        drawText(s, x, y, color)
    }

    private fun drawText(s: String, x: Int, y: Int, color: Int) {
        var cx = x
        for (ch in s) {
            val g = Font.glyph(ch)
            for (j in 0 until 5) for (i in 0 until 3) {
                if (g[j][i] == '#') put(cx + i, y + j, color)
            }
            cx += 4
        }
    }

    /** Крупный текст: каждый пиксель шрифта 3x5 превращается в квадрат k x k. */
    fun textS(s: String, x: Int, y: Int, color: Int, k: Int, shadow: Boolean = true) {
        if (shadow) drawTextS(s, x + k, y + k, 0xFF000000.toInt(), k)
        drawTextS(s, x, y, color, k)
    }

    fun textWidthS(s: String, k: Int): Int = if (s.isEmpty()) 0 else (s.length * 4 - 1) * k

    private fun drawTextS(s: String, x: Int, y: Int, color: Int, k: Int) {
        var cx = x
        for (ch in s) {
            val g = Font.glyph(ch)
            for (j in 0 until 5) for (i in 0 until 3) {
                if (g[j][i] == '#') rect(cx + i * k, y + j * k, k, k, color)
            }
            cx += 4 * k
        }
    }

    /** Мягкая овальная тень на земле (затемняет то, что уже нарисовано). */
    fun shadowE(cx: Int, cy: Int, hw: Int, hh: Int, strength: Float = 0.34f) {
        for (j in -hh..hh) for (i in -hw..hw) {
            val a = i.toFloat() / (hw + 0.5f)
            val b = j.toFloat() / (hh + 0.5f)
            val d = a * a + b * b
            if (d > 1f) continue
            val x = cx + i
            val y = cy + j
            if (x in 0 until w && y in 0 until h) {
                px[y * w + x] = lerpColor(px[y * w + x], 0xFF000000.toInt(), if (d > 0.6f) strength * 0.55f else strength)
            }
        }
    }

    /** Затемняет картинку: factor 1.0 = без изменений, 0.0 = чёрный. */
    fun dim(factor: Float) {
        for (i in px.indices) {
            val c = px[i]
            val r = (((c shr 16) and 0xFF) * factor).toInt()
            val g = (((c shr 8) and 0xFF) * factor).toInt()
            val b = ((c and 0xFF) * factor).toInt()
            px[i] = (c and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }
    }

    /** Увеличение целым коэффициентом без сглаживания (чёткие пиксели). */
    fun scaled(s: Int): IntArray {
        if (s == 1) return px.copyOf()
        val ow = w * s
        val out = IntArray(ow * h * s)
        for (y in 0 until h) {
            val base = y * s * ow
            for (x in 0 until w) {
                val c = px[y * w + x]
                val bx = base + x * s
                for (i in 0 until s) out[bx + i] = c
            }
            for (j in 1 until s) System.arraycopy(out, base, out, base + j * ow, ow)
        }
        return out
    }
}

object Font {
    private val blank = arrayOf("...", "...", "...", "...", "...")
    private val map: Map<Char, Array<String>> = mapOf(
        '0' to arrayOf("###", "#.#", "#.#", "#.#", "###"),
        '1' to arrayOf(".#.", "##.", ".#.", ".#.", "###"),
        '2' to arrayOf("###", "..#", "###", "#..", "###"),
        '3' to arrayOf("###", "..#", "###", "..#", "###"),
        '4' to arrayOf("#.#", "#.#", "###", "..#", "..#"),
        '5' to arrayOf("###", "#..", "###", "..#", "###"),
        '6' to arrayOf("###", "#..", "###", "#.#", "###"),
        '7' to arrayOf("###", "..#", "..#", "..#", "..#"),
        '8' to arrayOf("###", "#.#", "###", "#.#", "###"),
        '9' to arrayOf("###", "#.#", "###", "..#", "###"),
        'L' to arrayOf("#..", "#..", "#..", "#..", "###"),
        'V' to arrayOf("#.#", "#.#", "#.#", "#.#", ".#."),
        'G' to arrayOf("###", "#..", "#.#", "#.#", "###"),
        'S' to arrayOf("###", "#..", "###", "..#", "###"),
        'T' to arrayOf("###", ".#.", ".#.", ".#.", ".#."),
        'K' to arrayOf("#.#", "#.#", "##.", "#.#", "#.#"),
        'M' to arrayOf("#.#", "###", "###", "#.#", "#.#"),
        'B' to arrayOf("##.", "#.#", "##.", "#.#", "##."),
        'R' to arrayOf("##.", "#.#", "##.", "#.#", "#.#"),
        'I' to arrayOf("###", ".#.", ".#.", ".#.", "###"),
        'P' to arrayOf("##.", "#.#", "##.", "#..", "#.."),
        '/' to arrayOf("..#", "..#", ".#.", "#..", "#.."),
        '+' to arrayOf("...", ".#.", "###", ".#.", "..."),
        '-' to arrayOf("...", "...", "###", "...", "..."),
        '.' to arrayOf("...", "...", "...", "...", ".#."),
        'A' to arrayOf(".#.", "#.#", "###", "#.#", "#.#"),
        'E' to arrayOf("###", "#..", "##.", "#..", "###"),
        'D' to arrayOf("##.", "#.#", "#.#", "#.#", "##."),
        'C' to arrayOf("###", "#..", "#..", "#..", "###"),
        'O' to arrayOf("###", "#.#", "#.#", "#.#", "###"),
        'X' to arrayOf("#.#", "#.#", ".#.", "#.#", "#.#"),
        ':' to arrayOf("...", ".#.", "...", ".#.", "..."),
        '!' to arrayOf(".#.", ".#.", ".#.", "...", ".#."),
    )

    fun glyph(c: Char): Array<String> = map[c] ?: blank
}
