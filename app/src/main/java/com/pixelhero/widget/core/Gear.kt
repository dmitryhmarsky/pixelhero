package com.pixelhero.widget.core

import kotlin.math.max
import kotlin.math.pow

/** Слоты снаряжения. Оружие и броня привязаны к классу героя, остальное подходит всем. */
enum class Slot(val title: String, val classLocked: Boolean) {
    WEAPON("Оружие", true),
    HELM("Шлем", false),
    ARMOR("Броня", true),
    BOOTS("Сапоги", false),
    RING("Кольцо", false),
    AMULET("Амулет", false),
}

object Stat {
    const val ATK = 0
    const val HP = 1
    const val ASPD = 2
    const val GOLD = 3
    const val CRIT = 4
    const val COUNT = 5
    val NAMES = arrayOf("урона", "здоровья", "скорости атаки", "золота", "шанса крита")
    /** Вклад стата в «силу» предмета при сравнении. */
    val POWER_W = doubleArrayOf(1.0, 0.6, 1.0, 0.25, 0.8)
    /** Относительная величина стата как побочного свойства. */
    val AFFIX_W = doubleArrayOf(0.5, 0.5, 0.25, 0.5, 0.2)
}

/**
 * Предмет. [stats] в десятых долях процента (125 = +12.5%). [cls] = -1 для универсальных предметов.
 * Название собирается из слота, класса, редкости и [suffix].
 */
class Item(val slot: Slot, val rarity: Int, val ilvl: Int, val cls: Int, val suffix: Int, val stats: IntArray) {

    fun name(): String {
        val base = when (slot) {
            Slot.WEAPON -> Gear.WEAPON_NAMES[cls.coerceIn(0, 4)][rarity]
            Slot.ARMOR -> Gear.ARMOR_NAMES[cls.coerceIn(0, 4)][rarity]
            Slot.HELM -> Gear.HELM_NAMES[rarity]
            Slot.BOOTS -> Gear.BOOTS_NAMES[rarity]
            Slot.RING -> Gear.RING_NAMES[rarity]
            Slot.AMULET -> Gear.AMULET_NAMES[rarity]
        }
        return if (rarity >= 2) base + " «" + Gear.SUFFIXES[suffix % Gear.SUFFIXES.size] + "»" else base
    }

    fun power(): Double {
        var p = 0.0
        for (i in 0 until Stat.COUNT) p += stats[i] / 10.0 * Stat.POWER_W[i]
        return p
    }

    fun sellValue(): Long {
        val base = max(1.0, Balance.GOLD_BASE * Balance.REWARD_GROW.pow(ilvl - 1.0))
        return max(1L, (base * Gear.SELL_MUL[rarity]).toLong())
    }

    /** Строки «+12.5% урона» только для ненулевых статов. */
    fun statLines(): List<String> {
        val out = ArrayList<String>()
        for (i in 0 until Stat.COUNT) if (stats[i] > 0) out.add("+" + "%.1f".format(stats[i] / 10.0) + "% " + Stat.NAMES[i])
        return out
    }

    fun serialize(): String = "${slot.ordinal}.$rarity.$ilvl.${cls + 1}.$suffix." + stats.joinToString(".")

    companion object {
        fun parse(s: String): Item? {
            return try {
                val p = s.split(".")
                if (p.size != 5 + Stat.COUNT) return null
                val slot = Slot.values()[p[0].toInt()]
                val r = p[1].toInt()
                if (r !in 0 until Rarity.COUNT) return null
                val st = IntArray(Stat.COUNT) { p[5 + it].toInt().coerceIn(0, 100_000) }
                Item(slot, r, p[2].toInt().coerceAtLeast(1), p[3].toInt() - 1, p[4].toInt().coerceAtLeast(0), st)
            } catch (e: Exception) {
                null
            }
        }
    }
}

object Gear {
    const val SLOTS = 6
    val SUFFIXES = arrayOf("Бури", "Рассвета", "Тени", "Дракона", "Вечности", "Охотника", "Титана", "Тишины")

    val WEAPON_NAMES = arrayOf(
        arrayOf("Ржавый меч", "Стальной меч", "Рунный клинок", "Меч берсерка", "Клинок героя"),
        arrayOf("Кривой посох", "Дубовый посох", "Посох чародея", "Жезл архимага", "Посох звёзд"),
        arrayOf("Короткий лук", "Охотничий лук", "Составной лук", "Лук следопыта", "Лук ветра"),
        arrayOf("Тупой кинжал", "Стальной кинжал", "Парные клинки", "Кинжалы тени", "Клыки ночи"),
        arrayOf("Дубина", "Боевой молот", "Рунный молот", "Молот правосудия", "Молот рассвета"),
    )
    val ARMOR_NAMES = arrayOf(
        arrayOf("Тряпичная куртка", "Кольчуга", "Латный нагрудник", "Доспех берсерка", "Броня героя"),
        arrayOf("Рваная мантия", "Шерстяная мантия", "Мантия чародея", "Мантия архимага", "Одеяние звёзд"),
        arrayOf("Потёртая куртка", "Кожаный доспех", "Куртка следопыта", "Доспех охотника", "Плащ ветра"),
        arrayOf("Старый плащ", "Кожаный плащ", "Плащ скрытности", "Плащ теней", "Покров ночи"),
        arrayOf("Ржавые латы", "Стальные латы", "Священные латы", "Доспех крестоносца", "Броня рассвета"),
    )
    val HELM_NAMES = arrayOf("Кожаная шапка", "Стальной шлем", "Рунный шлем", "Шлем титана", "Корона вечности")
    val BOOTS_NAMES = arrayOf("Старые сапоги", "Кожаные сапоги", "Сапоги странника", "Сапоги вихря", "Сапоги бури")
    val RING_NAMES = arrayOf("Медное кольцо", "Серебряное кольцо", "Кольцо силы", "Кольцо мага", "Кольцо вечности")
    val AMULET_NAMES = arrayOf("Костяной талисман", "Амулет удачи", "Амулет охотника", "Амулет дракона", "Сердце звезды")

    @JvmField val BASE = doubleArrayOf(0.45, 0.75, 1.2, 1.8, 2.7)        // основной стат на 1-й стадии, %
    @JvmField var SCALE = 0.3                                          // рост за стадию (+30% базы за каждую стадию)
    val AFFIX_COUNT = intArrayOf(0, 1, 2, 2, 3)
    val SELL_MUL = doubleArrayOf(0.125, 0.25, 0.75, 2.25, 7.5)
    const val INV_CAP = 40

    private fun primary(slot: Slot): Int = when (slot) {
        Slot.WEAPON -> Stat.ATK
        Slot.HELM, Slot.ARMOR -> Stat.HP
        Slot.BOOTS -> Stat.ASPD
        Slot.RING -> Stat.CRIT
        Slot.AMULET -> Stat.GOLD
    }

    private fun slotMul(slot: Slot): Double = when (slot) {
        Slot.WEAPON -> 1.0
        Slot.ARMOR -> 1.0
        Slot.HELM -> 0.6
        Slot.BOOTS -> 0.5
        Slot.RING -> 0.4
        Slot.AMULET -> 1.0
    }

    /** Создаёт предмет заданной редкости. Привязанные к классу слоты получают [cls]. */
    fun roll(rng: Rng, rarity: Int, cls: Int, stage: Int): Item {
        val slot = Slot.values()[rng.int(Slot.values().size)]
        val itemCls = if (slot.classLocked) cls else -1
        val scale = 1.0 + SCALE * (stage - 1)
        val stats = IntArray(Stat.COUNT)
        val main = primary(slot)
        stats[main] = (BASE[rarity] * scale * slotMul(slot) * (0.85 + 0.3 * rng.float()) * 10).toInt().coerceAtLeast(1)
        repeat(AFFIX_COUNT[rarity]) {
            var s = rng.int(Stat.COUNT)
            if (s == main) s = (s + 1 + rng.int(Stat.COUNT - 1)) % Stat.COUNT
            val v = BASE[rarity] * scale * Stat.AFFIX_W[s] * (0.7 + 0.6 * rng.float()) * 10
            stats[s] += v.toInt().coerceAtLeast(1)
        }
        return Item(slot, rarity, stage, itemCls, rng.int(SUFFIXES.size), stats)
    }
}
