package com.pixelhero.widget.core

/**
 * Пиксель-арт, нарисованный текстом ('.' = прозрачно). Герои 16x16 (5 классов, см. SpriteData),
 * монстры 14x14, боссы 22x18. Все смотрят ВПРАВО; монстры рисуются с flip.
 * Спрайты героев и монстров генерирует gen.py (лежит в docs/), здесь палитры, сундук и надгробие.
 */
object Sprites {

    val HEROES = SpriteData.HEROES
    val MONSTERS = SpriteData.MONSTERS

    /** Сундук 9x7. 'c' и 'C' красятся цветом редкости, 'y' замок. */
    val CHEST = arrayOf(
        ".kkkkkkk.",
        "kCcccccCk",
        "kccccccck",
        "kkkkykkkk",
        "kcccyccck",
        "kccccccck",
        "kkkkkkkkk",
    )

    /** Надгробие 10x9 вместо героя, когда он погиб. */
    val GRAVE = arrayOf(
        "...kkkk...",
        "..kggggk..",
        ".kgggkggk.",
        ".kggkkkgk.",
        ".kgggkggk.",
        ".kgggkggk.",
        ".kggggggk.",
        ".kggggggk.",
        "kkkkkkkkkk",
    )

    val ALL: List<Pair<String, Array<String>>> = SpriteData.ALL + listOf("CHEST" to CHEST, "GRAVE" to GRAVE)

    /** Палитра сундука по редкости (0..4). */
    fun chestPalette(rarity: Int): (Char) -> Int {
        val main = rgb(Rarity.COLORS[rarity.coerceIn(0, Rarity.COUNT - 1)])
        val dark = lerpColor(main, rgb(0x000000), 0.35f)
        return { ch ->
            when (ch) {
                'k' -> rgb(0x1B1B2F)
                'c' -> main
                'C' -> dark
                'y' -> rgb(0xFFE08A)
                else -> 0
            }
        }
    }

    private val BLADE = intArrayOf(0xB8C0C8, 0xE8EEF5, 0xFFD24A, 0x6AF0FF, 0xD58BFF)
    private val ARMOR_MAIN = intArrayOf(0x3A6FD8, 0x3FA34D, 0xD24A4A, 0xE6B83A, 0xB46AFF)
    private val ARMOR_DARK = intArrayOf(0x274A9A, 0x277A38, 0x8A2020, 0xA07C1C, 0x7A3AC0)

    // Класс героя: воин, маг, лучник, вор, паладин
    private val CLASS_MAIN = intArrayOf(0xC0392B, 0x7A4AD8, 0x3FA34D, 0x444A58, 0xE6E6F5)
    private val CLASS_DARK = intArrayOf(0x7A1E16, 0x4B2A9A, 0x277A38, 0x2A2D38, 0xA0A8D0)
    private val CLASS_GLOW = intArrayOf(0xFFD24A, 0x8AF0FF, 0xFFD24A, 0xFFD24A, 0xFFE9A0)

    /** Палитра героя: класс задаёт цвета плаща, шапки и свечения; тир меча и брони окрашивает оружие и броню. */
    fun heroPalette(cls: Int, swordTier: Int, armorTier: Int, tint: Int): (Char) -> Int {
        val c = cls.coerceIn(0, 4)
        return { ch ->
            if (tint != 0 && ch != 'k') tint else when (ch) {
                'k' -> rgb(0x1B1B2F)
                's' -> rgb(0xF2C9A0)
                'g' -> rgb(0x9AA5B1)
                'w' -> rgb(0xF2F5FA)
                'b' -> rgb(ARMOR_MAIN[armorTier])
                'B' -> rgb(ARMOR_DARK[armorTier])
                'x' -> rgb(BLADE[swordTier])
                'h' -> rgb(0x8A5A2B)
                'y' -> rgb(0xFFD24A)
                'r' -> rgb(0xD83A3A)
                'c' -> rgb(CLASS_MAIN[c])
                'C' -> rgb(CLASS_DARK[c])
                'o' -> rgb(CLASS_GLOW[c])
                else -> 0
            }
        }
    }

    /** [hue] != 0 переставляет цветовые каналы: так один спрайт босса даёт разные цвета в разных зонах. */
    fun monsterPalette(tint: Int, hue: Int = 0): (Char) -> Int = { ch ->
        if (tint != 0 && ch != 'k') tint else {
            val c = when (ch) {
                'k' -> rgb(0x1B1B2F)
                'G' -> rgb(0x4CAF50)
                'D' -> rgb(0x2E7D32)
                'p' -> rgb(0x8E5BD6)
                'W' -> rgb(0xE9E2CF)
                'w' -> rgb(0xFFFFFF)
                'r' -> rgb(0xD83A3A)
                'R' -> rgb(0x8A1F1F)
                'y' -> rgb(0xF5D33A)
                'n' -> rgb(0x8A5A2B)
                'N' -> rgb(0x5A3A1A)
                'g' -> rgb(0x9AA5B1)
                'P' -> rgb(0x5B3A8A)
                'h' -> rgb(0xDDE8FF)
                'H' -> rgb(0xA8B8E0)
                'B' -> rgb(0x2A5FB0)
                'o' -> rgb(0xF08A2A)
                'O' -> rgb(0xA84F12)
                'S' -> rgb(0x8A8F9C)
                's' -> rgb(0x5A5F6C)
                'l' -> rgb(0xFFD84A)
                'm' -> rgb(0xE04A6A)
                'M' -> rgb(0x9A2040)
                else -> 0
            }
            if (c == 0 || hue == 0 || ch == 'k' || ch == 'l' || ch == 'w') c else permute(c, hue)
        }
    }

    private fun permute(c: Int, hue: Int): Int {
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        return when (hue) {
            1 -> rgb((b shl 16) or (r shl 8) or g)
            2 -> rgb((g shl 16) or (b shl 8) or r)
            3 -> rgb((r shl 16) or (b shl 8) or g)
            else -> rgb((g shl 16) or (r shl 8) or b)
        }
    }
}
