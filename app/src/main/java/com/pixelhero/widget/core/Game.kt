package com.pixelhero.widget.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Чистый Kotlin без Android API: можно тестировать на обычной JVM. */

class Rng(seed: Long) {
    private var s: Long = if (seed == 0L) 0x2545F4914F6CDD1DL else seed

    private fun nextLong(): Long {
        s = s xor (s shl 13)
        s = s xor (s ushr 7)
        s = s xor (s shl 17)
        return s
    }

    fun int(n: Int): Int = ((nextLong() ushr 1) % n).toInt()
    fun float(): Float = (nextLong() ushr 40).toInt() / 16777216f
}

enum class Phase { WALK, FIGHT }

enum class Upgrade(val title: String, val maxLevel: Int) {
    SWORD("Заточка оружия", 200),
    ARMOR("Закалка брони", 200),
    BOOTS("Сапоги", 20),
    LUCK("Удача", 25),
}

/** Редкость сундуков: индексы 0..4. Цвета без альфа-канала (0xRRGGBB). */
object Rarity {
    const val COUNT = 5
    val NAMES = arrayOf("Обычный", "Необычный", "Редкий", "Эпический", "Легендарный")
    val COLORS = intArrayOf(0xA58A5E, 0x4CD964, 0x4AA3FF, 0xC06BFF, 0xFFB02E)
}

class Monster(
    val type: Int,
    val boss: Boolean,
    val maxHp: Double,
    val atk: Double,
    val gold: Long,
    val xp: Long,
) {
    var hp: Double = maxHp
}

class Popup(var x: Float, var y: Float, val text: String, val color: Int, var ttl: Float)

/** Итог открытия сундуков: сколько открыто по редкостям, золото и бесплатные уровни по улучшениям. */
class ChestResult(val opened: IntArray, val gold: Long, val levels: IntArray, val items: List<Item> = emptyList(), val soldGold: Long = 0L) {
    val total: Int get() = opened.sum()
}

object Balance {
    const val KILLS_PER_STAGE = 10
    const val HERO_INTERVAL = 0.9f
    const val MON_INTERVAL = 1.2f
    const val BOSS_INTERVAL = 1.0f
    const val GAP = 1.5f
    /** Длительность анимации удара героя, секунд. */
    const val ATTACK_ANIM = 0.6f
    const val OFFLINE_CAP_SEC = 8 * 3600.0

    // [тип] -> множители (hp, atk)
    // типы: 0 слизь, 1 мышь, 2 скелет, 3 гоблин, 4 орк, 5 дракон(босс), 6 паук, 7 призрак, 8 скорпион, 9 голем,
    // 10 бес, 11 голем(босс), 12 демон(босс), 13 волк, 14 мумия, 15 гарпия
    val TYPE_HP = doubleArrayOf(1.0, 0.7, 1.1, 1.0, 1.6, 1.0, 0.8, 0.9, 1.0, 1.8, 1.0, 1.0, 1.0, 1.0, 1.3, 0.8)
    val TYPE_ATK = doubleArrayOf(0.8, 1.0, 1.1, 1.2, 1.3, 1.0, 1.1, 1.1, 1.2, 1.2, 1.3, 1.0, 1.0, 1.1, 1.0, 1.2)
    const val ZONES = 16
    const val STAGES_PER_ZONE = 4
    val ZONE_NAMES = arrayOf(
        "Лес", "Джунгли", "Пещера", "Шахты", "Пустошь", "Древний храм", "Болото", "Кладбище",
        "Замок", "Ледяные пики", "Небесные острова", "Вулкан", "Логово дракона", "Кристальная бездна", "Пустота", "Адские врата",
    )
    val ZONE_TYPES = arrayOf(
        intArrayOf(0, 13), intArrayOf(6, 13), intArrayOf(1, 6), intArrayOf(9, 3), intArrayOf(8, 3), intArrayOf(14, 8), intArrayOf(0, 7), intArrayOf(2, 7),
        intArrayOf(2, 4), intArrayOf(13, 9), intArrayOf(15, 1), intArrayOf(4, 10), intArrayOf(10, 8), intArrayOf(9, 7), intArrayOf(7, 10), intArrayOf(10, 4),
    )
    val BOSS_BY_ZONE = intArrayOf(11, 5, 11, 11, 5, 11, 5, 12, 12, 11, 5, 5, 5, 11, 12, 12)

    // Классы героя: воин, маг, лучник, вор, паладин
    val CLASS_NAMES = arrayOf("Воин", "Маг", "Лучник", "Вор", "Паладин")
    val CLASS_DESC = arrayOf(
        "Крепкий: +15% здоровья",
        "Сильные удары: +30% урона, но бьёт медленнее и хрупок",
        "Быстрые выстрелы: бьёт чаще, но слабее",
        "Золото +15%, критические удары вдвое чаще",
        "Лечится на 6% здоровья после каждого убийства",
    )
    @JvmField val CLASS_HP = doubleArrayOf(1.15, 0.85, 0.95, 0.9, 1.1)
    @JvmField val CLASS_ATK = doubleArrayOf(1.0, 1.3, 0.9, 1.0, 0.95)
    @JvmField val CLASS_INTERVAL = doubleArrayOf(1.0, 1.15, 0.8, 1.0, 1.0)
    @JvmField val CLASS_GOLD = doubleArrayOf(1.0, 1.0, 1.0, 1.15, 1.0)
    @JvmField val CLASS_CRIT_ROLL = intArrayOf(10, 10, 10, 5, 10) // крит = 1 из N
    @JvmField val CLASS_HEAL_KILL = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.06)

    // Подобрано симуляцией жадного бота: 10 мин ~ ст.5, 1 ч ~ ст.12, 4 ч ~ ст.21, 8 ч ~ ст.27, 24 ч ~ ст.35.
    @JvmField var SWORD_GROW = 1.05   // множитель урона за уровень меча
    @JvmField var ARMOR_GROW = 1.05   // множитель здоровья за уровень брони
    @JvmField var COST_GROW = 1.14    // рост цены меча и брони за уровень
    @JvmField var MON_HP_GROW = 1.21  // рост здоровья монстров за стадию
    @JvmField var MON_ATK_GROW = 1.168 // рост урона монстров за стадию
    @JvmField var REWARD_GROW = 1.22  // рост золота и опыта за стадию
    @JvmField var COST_BASE = 4.0     // общий множитель цен апгрейдов
    @JvmField var GOLD_BASE = 1.5     // золото за обычного монстра на 1-й стадии
    @JvmField var XP_BASE = 1.875     // опыт за обычного монстра на 1-й стадии
    @JvmField var WALK_DIST = 140f    // минимальная дистанция до следующего монстра (пикселей)

    // Смерть и воскрешение
    @JvmField var FREE_RESPAWN_SEC = 45f // через сколько секунд герой воскреснет бесплатно (с откатом на стадию назад)
    @JvmField var BLESS_SEC = 60f        // благословение после воскрешения за рекламу: урон ×1.5
    @JvmField var SHIELD_SEC = 5f        // неуязвимость после воскрешения

    // Сундуки
    // Сундуки подобраны так, чтобы не ломать темп игры: бесплатные уровни растут по экспоненте, их мало.
    @JvmField var CHEST_DROP = 0.05                       // шанс сундука с обычного монстра (босс даёт всегда)
    val RARITY_WEIGHT = doubleArrayOf(600.0, 250.0, 110.0, 35.0, 5.0)
    val CHEST_GOLD = doubleArrayOf(1.0, 2.0, 6.0, 18.0, 60.0)    // золото = (золото монстра этой стадии) × множитель
    val CHEST_LEVELS = intArrayOf(0, 0, 0, 0, 0)                 // бесплатные уровни улучшений

    fun zoneOf(stage: Int): Int = ((stage - 1) / STAGES_PER_ZONE) % ZONES

    fun cost(u: Upgrade, level: Int): Long = (COST_BASE * when (u) {
        Upgrade.SWORD -> 20 * COST_GROW.pow(level)
        Upgrade.ARMOR -> 25 * COST_GROW.pow(level)
        Upgrade.BOOTS -> 40 * 1.25.pow(level)
        Upgrade.LUCK -> 50 * 1.30.pow(level)
    }).toLong()
}

class GameState(seed: Long = 12345L) {
    var level = 1
    var xp = 0L
    var gold = 0L
    var stage = 1
    var kills = 0
    var totalKills = 0L
    var deaths = 0
    var sword = 0
    var armor = 0
    var boots = 0
    var luck = 0
    var heroClass = 0

    // снаряжение: рюкзак и надетое для каждого класса отдельно
    val inventory = ArrayList<Item>()
    val equipped = Array(5) { arrayOfNulls<Item>(Gear.SLOTS) }
    private val gear = DoubleArray(Stat.COUNT)
    var hp = 0.0
    var lastMs = 0L

    // смерть и воскрешение
    var dead = false
    var deadTimer = 0f
    var blessing = 0f
    var shield = 0f

    // сундуки в инвентаре по редкостям
    val chests = IntArray(Rarity.COUNT)
    var dropRarity = -1
    var dropTimer = 0f
    var dropScroll = 0f

    val rng = Rng(seed)
    var phase = Phase.WALK
    var monster: Monster? = null
    var dist = 60f
    var heroTimer = 0f
    var monTimer = 0f
    var anim = 0f
    var scroll = 0f
    var flashHero = 0f
    var flashMon = 0f
    var attackAnim = 0f
    val popups = ArrayList<Popup>()

    init {
        hp = maxHp()
    }

    // ---- характеристики ----
    fun attack(): Double = (3.0 + level) * Balance.SWORD_GROW.pow(sword) * Balance.CLASS_ATK[heroClass] * (1.0 + gear[Stat.ATK] / 100.0) * (if (blessing > 0) 1.5 else 1.0)
    fun maxHp(): Double = (30.0 + level * 5.0) * Balance.ARMOR_GROW.pow(armor) * Balance.CLASS_HP[heroClass] * (1.0 + gear[Stat.HP] / 100.0)
    fun defense(): Double = armor * 0.3
    fun walkSpeed(): Float = 14f + boots * 1.5f
    fun goldMul(): Double = (1.0 + luck * 0.12) * Balance.CLASS_GOLD[heroClass] * (1.0 + gear[Stat.GOLD] / 100.0)
    fun xpNeed(): Long = (10 * 1.3.pow(level - 1)).toLong().coerceAtLeast(10)
    fun swordTier(): Int = equipped[heroClass][Slot.WEAPON.ordinal]?.rarity ?: 0
    fun armorTier(): Int = equipped[heroClass][Slot.ARMOR.ordinal]?.rarity ?: 0

    fun levelOf(u: Upgrade): Int = when (u) {
        Upgrade.SWORD -> sword
        Upgrade.ARMOR -> armor
        Upgrade.BOOTS -> boots
        Upgrade.LUCK -> luck
    }

    fun upgradeCost(u: Upgrade): Long = Balance.cost(u, levelOf(u))
    fun canBuy(u: Upgrade): Boolean = levelOf(u) < u.maxLevel && gold >= upgradeCost(u)

    fun buy(u: Upgrade): Boolean {
        if (!canBuy(u)) return false
        gold -= upgradeCost(u)
        applyLevel(u)
        return true
    }

    private fun applyLevel(u: Upgrade) {
        when (u) {
            Upgrade.SWORD -> sword++
            Upgrade.ARMOR -> {
                armor++
                hp = min(maxHp(), hp + 10.0)
            }
            Upgrade.BOOTS -> boots++
            Upgrade.LUCK -> luck++
        }
    }

    // ---- снаряжение ----
    /** Пересчитывает суммарные бонусы надетых предметов текущего класса. */
    fun recalcGear() {
        gear.fill(0.0)
        for (it in equipped[heroClass]) if (it != null) for (i in 0 until Stat.COUNT) gear[i] += it.stats[i] / 10.0
        hp = min(hp, maxHp())
    }

    fun gearBonus(stat: Int): Double = gear[stat]

    fun canUse(item: Item): Boolean = item.cls < 0 || item.cls == heroClass

    fun worn(slot: Slot): Item? = equipped[heroClass][slot.ordinal]

    /** Надевает предмет из рюкзака; прежний предмет возвращается в рюкзак. */
    fun equip(item: Item): Boolean {
        if (!inventory.contains(item) || !canUse(item)) return false
        val old = equipped[heroClass][item.slot.ordinal]
        equipped[heroClass][item.slot.ordinal] = item
        inventory.remove(item)
        if (old != null) inventory.add(old)
        recalcGear()
        return true
    }

    fun unequip(slot: Slot): Boolean {
        val old = equipped[heroClass][slot.ordinal] ?: return false
        if (inventory.size >= Gear.INV_CAP) return false
        equipped[heroClass][slot.ordinal] = null
        inventory.add(old)
        recalcGear()
        return true
    }

    fun sell(item: Item): Long {
        if (!inventory.remove(item)) return 0
        val v = item.sellValue()
        gold += v
        return v
    }

    /** Надевает в каждый слот лучший по силе предмет из рюкзака. Возвращает число замен. */
    fun autoEquip(): Int {
        var n = 0
        for (slot in Slot.values()) {
            val cur = worn(slot)
            val best = inventory.filter { it.slot == slot && canUse(it) }.maxByOrNull { it.power() }
            if (best != null && (cur == null || best.power() > cur.power())) {
                equip(best)
                n++
            }
        }
        return n
    }

    /** Продаёт всё из рюкзака, что не лучше надетого (для своего класса) и не подходит другим классам с пустым слотом. */
    fun sellJunk(): Long {
        var sum = 0L
        for (it in inventory.toList()) {
            val keep = if (it.cls < 0) {
                // универсальный предмет полезен, если лучше надетого хотя бы у одного класса
                (0 until 5).any { c -> val cur = equipped[c][it.slot.ordinal]; cur == null || it.power() > cur.power() }
            } else {
                val cur = equipped[it.cls][it.slot.ordinal]
                cur == null || it.power() > cur.power()
            }
            if (!keep) sum += sell(it)
        }
        return sum
    }

    /** Смена класса: у каждого класса свой комплект снаряжения. */
    fun switchClass(cls: Int) {
        heroClass = cls.coerceIn(0, 4)
        recalcGear()
    }

    // ---- сундуки ----
    fun chestTotal(): Int = chests.sum()

    /** Индекс высшей редкости среди имеющихся сундуков или -1. */
    fun bestChest(): Int {
        for (r in Rarity.COUNT - 1 downTo 0) if (chests[r] > 0) return r
        return -1
    }

    /** Бросок редкости. Удача слегка повышает шансы на редкое. У босса два броска, берётся лучший. */
    fun rollRarity(boss: Boolean): Int {
        fun once(): Int {
            val w = Balance.RARITY_WEIGHT.copyOf()
            w[2] *= 1.0 + luck * 0.03
            w[3] *= 1.0 + luck * 0.04
            w[4] *= 1.0 + luck * 0.05
            var total = 0.0
            for (x in w) total += x
            var roll = rng.float() * total
            for (i in w.indices) {
                roll -= w[i]
                if (roll < 0) return i
            }
            return 0
        }
        val a = once()
        return if (boss) max(a, once()) else a
    }

    private fun grantLevel(): Upgrade? {
        val pool = ArrayList<Upgrade>()
        for (u in Upgrade.values()) {
            if (levelOf(u) >= u.maxLevel) continue
            repeat(if (u == Upgrade.SWORD || u == Upgrade.ARMOR) 3 else 1) { pool.add(u) }
        }
        if (pool.isEmpty()) return null
        val u = pool[rng.int(pool.size)]
        applyLevel(u)
        return u
    }

    /** Открывает все сундуки. [doubleGold] = true удваивает золото (награда за просмотр рекламы). */
    fun openChests(doubleGold: Boolean): ChestResult {
        val opened = chests.copyOf()
        val levels = IntArray(Upgrade.values().size)
        var goldSum = 0L
        var soldGold = 0L
        val items = ArrayList<Item>()
        val base = max(1.0, Balance.GOLD_BASE * Balance.REWARD_GROW.pow(stage - 1.0))
        for (r in 0 until Rarity.COUNT) {
            val n = chests[r]
            if (n <= 0) continue
            val g = base * Balance.CHEST_GOLD[r] * n * (if (doubleGold) 2.0 else 1.0)
            goldSum += max(n.toLong(), g.toLong())
            repeat(n * Balance.CHEST_LEVELS[r]) { grantLevel()?.let { levels[it.ordinal]++ } }
            // из каждого сундука выпадает предмет его редкости; за рекламу предметов вдвое больше
            repeat(n * (if (doubleGold) 2 else 1)) {
                // 3 из 4 предметов, привязанных к классу, выпадают для текущего класса, остальные для случайного
                val cls = if (rng.int(4) != 0) heroClass else rng.int(5)
                val drop = Gear.roll(rng, r, cls, stage)
                if (inventory.size < Gear.INV_CAP) {
                    inventory.add(drop)
                    items.add(drop)
                } else {
                    soldGold += drop.sellValue()
                }
            }
            chests[r] = 0
        }
        gold += goldSum + soldGold
        return ChestResult(opened, goldSum, levels, items, soldGold)
    }

    // ---- смерть и воскрешение ----
    /** Воскрешение за просмотр рекламы: полное здоровье на той же стадии, неуязвимость и благословение. */
    fun reviveWithAd(): Boolean {
        if (!dead) return false
        dead = false
        hp = maxHp()
        blessing = Balance.BLESS_SEC
        shield = Balance.SHIELD_SEC
        monster = null
        phase = Phase.WALK
        return true
    }

    /** Бесплатное воскрешение после ожидания: откат на стадию назад. */
    private fun freeRespawn() {
        dead = false
        stage = max(1, stage - 1)
        kills = 0
        hp = maxHp()
        shield = Balance.SHIELD_SEC
        monster = null
        phase = Phase.WALK
    }

    // ---- симуляция ----
    private fun spawn() {
        val boss = kills == Balance.KILLS_PER_STAGE - 1
        val zone = Balance.zoneOf(stage)
        val type = if (boss) Balance.BOSS_BY_ZONE[zone] else Balance.ZONE_TYPES[zone][rng.int(2)]
        val s = (stage - 1).toDouble()
        val bossMul = if (boss) 4.0 else 1.0
        val hp = 12.0 * Balance.MON_HP_GROW.pow(s) * Balance.TYPE_HP[type] * bossMul
        val atk = 2.2 * Balance.MON_ATK_GROW.pow(s) * Balance.TYPE_ATK[type] * (if (boss) 1.6 else 1.0)
        val gold = (Balance.GOLD_BASE * Balance.REWARD_GROW.pow(s) * (if (boss) 5.0 else 1.0)).toLong().coerceAtLeast(1)
        val xp = (Balance.XP_BASE * Balance.REWARD_GROW.pow(s) * (if (boss) 3.0 else 1.0)).toLong().coerceAtLeast(1)
        monster = Monster(type, boss, hp, atk, gold, xp)
        dist = Balance.WALK_DIST + rng.float() * 20f
    }

    private fun pop(x: Float, y: Float, text: String, color: Int, quiet: Boolean) {
        if (quiet || popups.size > 8) return
        popups.add(Popup(x, y, text, color, 0.9f))
    }

    private fun die(quiet: Boolean) {
        deaths++
        dead = true
        deadTimer = Balance.FREE_RESPAWN_SEC
        monster = null
        phase = Phase.WALK
        blessing = 0f
        shield = 0f
        pop(14f, 10f, "RIP", 0xFFFF5050.toInt(), quiet)
    }

    private fun reward(m: Monster, quiet: Boolean) {
        val g = max(1L, (m.gold * goldMul()).toLong())
        gold += g
        xp += m.xp
        totalKills++
        pop(60f, 14f, "+" + Fmt.num(g), 0xFFFFD84A.toInt(), quiet)
        while (xp >= xpNeed()) {
            xp -= xpNeed()
            level++
            hp = min(maxHp(), hp + maxHp() * 0.3)
            pop(14f, 10f, "LV" + level, 0xFF7CFFB2.toInt(), quiet)
        }
        val dropChance = min(0.2, Balance.CHEST_DROP + luck * 0.004)
        if (m.boss || rng.float() < dropChance) {
            val r = rollRarity(m.boss)
            chests[r]++
            dropRarity = r
            dropTimer = 3f
            dropScroll = scroll
        }
        hp = min(maxHp(), hp + maxHp() * Balance.CLASS_HEAL_KILL[heroClass])
        if (m.boss) {
            stage++
            kills = 0
        } else {
            kills++
        }
    }

    fun step(h: Float, quiet: Boolean = false) {
        anim += h
        if (flashHero > 0) flashHero -= h
        if (flashMon > 0) flashMon -= h
        if (attackAnim > 0) attackAnim -= h
        if (blessing > 0) blessing -= h
        if (shield > 0) shield -= h
        if (dropTimer > 0) dropTimer -= h
        if (!quiet && popups.isNotEmpty()) {
            val it = popups.iterator()
            while (it.hasNext()) {
                val p = it.next()
                p.ttl -= h
                p.y -= 6f * h
                if (p.ttl <= 0) it.remove()
            }
        }
        if (dead) {
            deadTimer -= h
            if (deadTimer <= 0) freeRespawn()
            return
        }
        val m = monster ?: run { spawn(); monster!! }
        if (phase == Phase.WALK) {
            hp = min(maxHp(), hp + maxHp() * 0.02 * h)
            val d = walkSpeed() * h
            dist -= d
            scroll += d
            if (dist <= Balance.GAP) {
                dist = Balance.GAP
                phase = Phase.FIGHT
                heroTimer = 0.35f
                monTimer = 0.9f
            }
            return
        }
        heroTimer -= h
        monTimer -= h
        if (heroTimer <= 0) {
            heroTimer += (Balance.HERO_INTERVAL * Balance.CLASS_INTERVAL[heroClass] / (1.0 + min(gear[Stat.ASPD], 60.0) / 100.0)).toFloat()
            attackAnim = Balance.ATTACK_ANIM
            val crit = rng.float() < 1.0 / Balance.CLASS_CRIT_ROLL[heroClass] + gear[Stat.CRIT] / 100.0
            val dmg = attack() * (if (crit) 2.0 else 1.0) * (0.9 + 0.2 * rng.float())
            m.hp -= dmg
            flashMon = 0.12f
            pop(100f, 16f, Fmt.num(dmg.toLong().coerceAtLeast(1)), if (crit) 0xFFFFFFFF.toInt() else 0xFFFF8A3D.toInt(), quiet)
            if (m.hp <= 0) {
                reward(m, quiet)
                monster = null
                phase = Phase.WALK
                return
            }
        }
        if (monTimer <= 0) {
            monTimer += if (m.boss) Balance.BOSS_INTERVAL else Balance.MON_INTERVAL
            if (shield <= 0) {
                val dmg = max(1.0, m.atk * (0.9 + 0.2 * rng.float()) - defense())
                hp -= dmg
                flashHero = 0.12f
                pop(22f, 16f, Fmt.num(dmg.toLong().coerceAtLeast(1)), 0xFFFF5050.toInt(), quiet)
                if (hp <= 0) die(quiet)
            }
        }
    }

    /** Продвигает игру на [seconds] секунд. Для офлайн-прогресса передавай quiet = true. */
    fun advance(seconds: Double, quiet: Boolean) {
        var left = min(seconds, Balance.OFFLINE_CAP_SEC)
        val h = if (quiet) 0.25 else 0.1
        while (left > 1e-6) {
            val s = min(h, left)
            step(s.toFloat(), quiet)
            left -= s
        }
    }

    /** Продвигает игру до момента [nowMs] по часам (идемпотентно: можно звать из сервиса и из Activity). */
    fun advanceTo(nowMs: Long) {
        if (lastMs == 0L) {
            lastMs = nowMs
            return
        }
        val dt = (nowMs - lastMs) / 1000.0
        if (dt <= 0) return
        lastMs = nowMs
        advance(dt, quiet = dt > 2.0)
    }

    // ---- сохранение ----
    fun serialize(): String {
        val base = listOf<Any>("v4", level, xp, gold, stage, kills, totalKills, deaths, sword, armor, boots, luck, hp, lastMs)
        val extra = listOf<Any>(chests[0], chests[1], chests[2], chests[3], chests[4], if (dead) 1 else 0, deadTimer, blessing, heroClass)
        val inv = inventory.joinToString(";") { it.serialize() }
        val eq = equipped.joinToString(";") { row -> row.joinToString(",") { it?.serialize() ?: "_" } }
        return (base + extra).joinToString("|") + "|" + inv + "|" + eq
    }

    companion object {
        fun deserialize(s: String?, seed: Long): GameState? {
            if (s.isNullOrEmpty()) return null
            return try {
                val p = s.split("|")
                if (p[0] != "v1" && p[0] != "v2" && p[0] != "v3" && p[0] != "v4") return null
                GameState(seed).apply {
                    level = p[1].toInt()
                    xp = p[2].toLong()
                    gold = p[3].toLong()
                    stage = p[4].toInt()
                    kills = p[5].toInt()
                    totalKills = p[6].toLong()
                    deaths = p[7].toInt()
                    sword = p[8].toInt()
                    armor = p[9].toInt()
                    boots = p[10].toInt()
                    luck = p[11].toInt()
                    hp = min(p[12].toDouble(), maxHp())
                    lastMs = p[13].toLong()
                    if (p[0] == "v2" || p[0] == "v3" || p[0] == "v4") {
                        for (i in 0 until Rarity.COUNT) chests[i] = p[14 + i].toInt()
                        dead = p[19] == "1"
                        deadTimer = p[20].toFloat()
                        blessing = p[21].toFloat()
                    }
                    if (p[0] == "v3" || p[0] == "v4") heroClass = p[22].toInt().coerceIn(0, 4)
                    if (p[0] == "v4") {
                        for (t in p[23].split(";")) if (t.isNotEmpty()) Item.parse(t)?.let { if (inventory.size < Gear.INV_CAP) inventory.add(it) }
                        val rows = p[24].split(";")
                        for ((c, row) in rows.withIndex()) {
                            if (c >= 5) break
                            for ((i, t) in row.split(",").withIndex()) {
                                if (i >= Gear.SLOTS || t == "_") continue
                                val it = Item.parse(t)
                                if (it != null && it.slot.ordinal == i) equipped[c][i] = it
                            }
                        }
                        recalcGear()
                        hp = min(p[12].toDouble(), maxHp())
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

object Fmt {
    fun num(n: Long): String = when {
        n < 1000 -> n.toString()
        n < 10_000 -> "%d.%dK".format(n / 1000, (n % 1000) / 100)
        n < 1_000_000 -> "${n / 1000}K"
        n < 10_000_000 -> "%d.%dM".format(n / 1_000_000, (n % 1_000_000) / 100_000)
        n < 1_000_000_000 -> "${n / 1_000_000}M"
        else -> "${n / 1_000_000_000}B"
    }
}
