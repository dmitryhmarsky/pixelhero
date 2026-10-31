package com.pixelhero.widget.core

/**
 * Пиксель-арт героев, монстров, сундука и надгробия лежит в SpriteData.kt (его рисует docs/sprite_gen).
 * Символ спрайта = (материал, тон): тон 0..4 это тень, полутень, база, свет, блик. Цвет материала задаёт палитра здесь,
 * поэтому один и тот же спрайт перекрашивается под класс, редкость снаряжения и зону. 'k' = контур, '.' = пусто.
 * Герои 56x48, монстры от 30x24 до 70x52, все смотрят ВПРАВО; монстры рисуются с flip.
 */
object Sprites {

    val HEROES = SpriteData.HEROES
    val MONSTERS = SpriteData.MONSTERS
    val CHEST = SpriteData.CHEST
    val GRAVE = SpriteData.GRAVE
    val ALL: List<Pair<String, Array<String>>> = SpriteData.ALL

    /** Кадры героя в SpriteData.HEROES[класс]. */
    const val F_IDLE = 0
    const val F_WALK_A = 1
    const val F_WALK_B = 2
    const val F_WINDUP = 3
    const val F_STRIKE = 4

    private const val OUTLINE = 0x12101C
    private val SHADOW = intArrayOf(0x1B, 0x1A, 0x3A)
    private val WARM = intArrayOf(0xFF, 0xF1, 0xD0)
    private val WHITE = intArrayOf(0xFF, 0xFF, 0xFF)

    private val IDX = IntArray(128) { -1 }.also { t -> SpriteData.ALPHA.forEachIndexed { i, ch -> t[ch.code] = i } }

    private fun mix(base: Int, to: IntArray, t: Float): Int {
        val r = Math.round(((base shr 16) and 0xFF) + (to[0] - ((base shr 16) and 0xFF)) * t)
        val g = Math.round(((base shr 8) and 0xFF) + (to[1] - ((base shr 8) and 0xFF)) * t)
        val b = Math.round((base and 0xFF) + (to[2] - (base and 0xFF)) * t)
        return rgb((r shl 16) or (g shl 8) or b)
    }

    /** Пять тонов материала: холодная тень, полутень, база, тёплый свет, блик. */
    fun ramp(base: Int): IntArray = intArrayOf(
        mix(base, SHADOW, 0.70f), mix(base, SHADOW, 0.38f), rgb(base), mix(base, WARM, 0.32f), mix(base, WHITE, 0.68f),
    )

    /** Таблица символ -> цвет (0 = прозрачно). tint != 0 заливает спрайт одним цветом (вспышка удара), контур остаётся. */
    private fun table(mats: IntArray, tint: Int): IntArray {
        val t = IntArray(128)
        t['k'.code] = rgb(OUTLINE)
        val ramps = Array(mats.size) { ramp(mats[it]) }
        for (i in 0 until minOf(SpriteData.ALPHA.length, mats.size * 5)) {
            t[SpriteData.ALPHA[i].code] = if (tint != 0) tint else ramps[i / 5][i % 5]
        }
        return t
    }

    private fun pal(t: IntArray): (Char) -> Int = { ch -> if (ch.code < t.size) t[ch.code] else 0 }

    // Материалы героя: 0 кожа, 1 сталь, 2 броня (тир), 3 клинок/оружие (тир), 4 цвет класса, 5 тёмный цвет класса,
    // 6 кожа-ремни, 7 золото, 8 волосы, 9 дерево, 10 свечение, 11 глаза.
    private val BLADE = intArrayOf(0xB8C4D0, 0xE8EEF5, 0xFFD24A, 0x6AF0FF, 0xD58BFF)
    private val ARMOR_T0 = intArrayOf(0x7388A0, 0x6C5AB0, 0x9A6A3A, 0x5A5666, 0xB4BCD4)   // по классам
    private val ARMOR_UP = intArrayOf(0, 0x5CA86A, 0x4F86D8, 0xA468E0, 0xE0B040)            // тиры 1..4
    private val CLASS_MAIN = intArrayOf(0xB83A32, 0x7A4AD8, 0x3FA34D, 0x444A58, 0xC8D0F0)
    private val CLASS_DARK = intArrayOf(0x5A2A30, 0x3C2A78, 0x2A5A34, 0x7A2A3A, 0x6A78C8)
    private val CLASS_GLOW = intArrayOf(0xFFD24A, 0x8AF0FF, 0xFFD24A, 0xFFD24A, 0xFFE9A0)
    private val CLASS_HAIR = intArrayOf(0x8A5A2B, 0xD8D8E8, 0x8A5A2B, 0x8A5A2B, 0x8A5A2B)

    private val heroCache = HashMap<Int, (Char) -> Int>()

    /** Палитра героя: класс красит одежду и свечение, тир оружия красит клинок/навершие, тир брони красит доспех. */
    fun heroPalette(cls: Int, swordTier: Int, armorTier: Int, tint: Int): (Char) -> Int {
        val c = cls.coerceIn(0, 4)
        val sw = swordTier.coerceIn(0, 4)
        val ar = armorTier.coerceIn(0, 4)
        val key = ((c * 5 + sw) * 5 + ar) * 2 + (if (tint != 0) 1 else 0)
        if (tint != 0) return pal(table(IntArray(12), tint))
        return heroCache.getOrPut(key) {
            val armor = if (ar == 0) ARMOR_T0[c] else ARMOR_UP[ar]
            pal(table(intArrayOf(
                0xF0C8A0, 0x7388A0, armor, BLADE[sw], CLASS_MAIN[c], CLASS_DARK[c], 0x7A5030, 0xE0B040,
                CLASS_HAIR[c], 0x6A4A2A, CLASS_GLOW[c], 0x4AA8FF,
            ), 0))
        }
    }

    private val monCache = HashMap<Int, (Char) -> Int>()

    /** [hue] != 0 переставляет цветовые каналы: один спрайт босса даёт разные цвета в разных зонах. */
    fun monsterPalette(type: Int, tint: Int = 0, hue: Int = 0): (Char) -> Int {
        val ty = type.coerceIn(0, SpriteData.MONSTER_MATS.size - 1)
        if (tint != 0) return pal(table(IntArray(12), tint))
        return monCache.getOrPut(ty * 8 + hue) {
            val mats = SpriteData.MONSTER_MATS[ty]
            pal(table(if (hue == 0) mats else IntArray(mats.size) { permute(mats[it], hue) }, 0))
        }
    }

    private fun permute(c: Int, hue: Int): Int {
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        return when (hue) {
            1 -> (b shl 16) or (r shl 8) or g
            2 -> (g shl 16) or (b shl 8) or r
            3 -> (r shl 16) or (b shl 8) or g
            else -> (g shl 16) or (r shl 8) or b
        }
    }

    /** Палитра сундука по редкости (0..4): материалы 0 корпус, 1 оковка, 2 замок, 3 тёмный. */
    fun chestPalette(rarity: Int): (Char) -> Int {
        val r = rarity.coerceIn(0, Rarity.COUNT - 1)
        val main = Rarity.COLORS[r]
        return pal(table(intArrayOf(main, if (r >= 3) 0xE0B040 else 0x8A8F9C, 0xE8C050, 0x2A2438), 0))
    }

    private val warriorTables = HashMap<Int, IntArray>()

    /** Палитра воина, нарисованного вручную (WarriorData): цвета берутся из картинки, tint даёт вспышку удара. */
    fun warriorPalette(tint: Int): (Char) -> Int {
        val t = warriorTables.getOrPut(if (tint != 0) tint else 0) {
            val a = IntArray(1280)
            for (i in WarriorData.CHARS.indices) a[WarriorData.CHARS[i].code] = if (tint != 0) tint else rgb(WarriorData.PALETTE[i])
            a
        }
        return pal(t)
    }

    private val gravePal = pal(table(intArrayOf(0x8A90A0, 0x4A5060, 0x6A4A30, 0x3F8A44), 0))
    fun gravePalette(): (Char) -> Int = gravePal
}
