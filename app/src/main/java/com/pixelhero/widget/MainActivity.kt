package com.pixelhero.widget

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import com.pixelhero.widget.core.Gear
import com.pixelhero.widget.core.Item
import com.pixelhero.widget.core.Slot
import com.pixelhero.widget.core.Balance
import com.pixelhero.widget.core.Fmt
import com.pixelhero.widget.core.Rarity
import com.pixelhero.widget.core.Upgrade

/** Экран управления: живой предпросмотр, статистика и покупка улучшений. UI строится кодом, без XML. */
class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var preview: ImageView
    private lateinit var stats: TextView
    private val buttons = LinkedHashMap<Upgrade, Button>()
    private lateinit var deadCard: LinearLayout
    private lateinit var deadText: TextView
    private lateinit var reviveBtn: Button
    private lateinit var chestText: TextView
    private lateinit var openBtn: Button
    private lateinit var openAdBtn: Button
    private var adBusy = false
    private val slotViews = ArrayList<TextView>()
    private val fpsButtons = LinkedHashMap<Int, Button>()
    private lateinit var packBtn: Button

    private fun rarityColor(r: Int): Int = Rarity.COLORS[r.coerceIn(0, Rarity.COUNT - 1)] or (0xFF shl 24)

    private fun colored(text: String, color: Int): SpannableStringBuilder {
        val sb = SpannableStringBuilder(text)
        sb.setSpan(ForegroundColorSpan(color), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    private fun itemLabel(it: Item): CharSequence {
        val sb = SpannableStringBuilder()
        sb.append(colored("[" + Rarity.NAMES[it.rarity].take(3) + "] " + it.name(), rarityColor(it.rarity)))
        sb.append("  ур.${it.ilvl}\n   " + it.statLines().joinToString(", "))
        return sb
    }

    /** Карточка предмета: бонусы, сравнение с надетым и действия. */
    private fun showItem(item: Item, isWorn: Boolean) {
        val g = Engine.get(this)
        val sb = StringBuilder()
        sb.append("${item.slot.title}, ур.${item.ilvl}\n")
        if (item.cls >= 0) sb.append("Класс: ${Balance.CLASS_NAMES[item.cls]}\n")
        sb.append("\n").append(item.statLines().joinToString("\n"))
        if (!isWorn) {
            val cur = g.worn(item.slot)
            if (cur != null && g.canUse(item)) {
                val d = item.power() - cur.power()
                sb.append("\n\nПротив надетого: " + (if (d >= 0) "+" else "") + "%.1f".format(d) + " силы")
            } else if (cur == null && g.canUse(item)) sb.append("\n\nСлот пуст")
            if (!g.canUse(item)) sb.append("\n\nПодходит классу «${Balance.CLASS_NAMES[item.cls]}»")
        }
        val b = AlertDialog.Builder(this).setTitle(colored(item.name(), rarityColor(item.rarity))).setMessage(sb.toString())
        if (isWorn) {
            b.setPositiveButton("Снять") { _, _ ->
                if (!g.unequip(item.slot)) toast("Рюкзак полон") else { Engine.save(this); render() }
            }
        } else {
            if (g.canUse(item)) b.setPositiveButton("Надеть") { _, _ -> g.equip(item); Engine.save(this); render() }
            b.setNeutralButton("Продать +${Fmt.num(item.sellValue())} G") { _, _ -> g.sell(item); Engine.save(this); render() }
        }
        b.setNegativeButton("Закрыть", null).show()
    }

    private fun showBackpack() {
        val g = Engine.get(this)
        if (g.inventory.isEmpty()) { toast("Рюкзак пуст"); return }
        val list = g.inventory.sortedWith(compareBy<Item>({ it.slot.ordinal }, { -it.power() }))
        AlertDialog.Builder(this)
            .setTitle("Рюкзак ${list.size}/${Gear.INV_CAP}")
            .setItems(Array<CharSequence>(list.size) { itemLabel(list[it]) }) { _, i -> showItem(list[i], false) }
            .setNegativeButton("Закрыть", null)
            .show()
    }
    private val classButtons = ArrayList<Button>()
    private lateinit var classDesc: TextView

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun watchAdRevive() {
        if (adBusy) return
        adBusy = true
        Ads.provider.showRewarded(
            this,
            onReward = {
                adBusy = false
                if (Engine.get(this).reviveWithAd()) Engine.save(this)
                render()
            },
            onFail = { adBusy = false; toast(it) },
        )
    }

    private fun openChests(double: Boolean) {
        val g = Engine.get(this)
        val res = g.openChests(double)
        if (res.total == 0) return
        Engine.save(this)
        val sb = SpannableStringBuilder()
        sb.append("Золото: +${Fmt.num(res.gold)}" + (if (double) " (×2)" else "") + "\n")
        if (res.soldGold > 0) sb.append("Рюкзак полон, лишнее продано: +${Fmt.num(res.soldGold)} G\n")
        sb.append("\n")
        val shown = res.items.sortedByDescending { it.rarity }
        for (it in shown.take(12)) sb.append(itemLabel(it)).append("\n")
        if (shown.size > 12) sb.append("…и ещё ${shown.size - 12}\n")
        AlertDialog.Builder(this)
            .setTitle("Добыча: сундуков ${res.total}")
            .setMessage(sb)
            .setPositiveButton("Надеть лучшее") { _, _ -> g.autoEquip(); Engine.save(this); render() }
            .setNegativeButton("Ок", null)
            .show()
        render()
    }

    private val refresh = object : Runnable {
        override fun run() {
            Engine.tick(this@MainActivity)
            render()
            handler.postDelayed(this, 150)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        HeroService.startSafely(this)
        handler.post(refresh)
    }

    override fun onPause() {
        handler.removeCallbacks(refresh)
        Engine.save(this)
        super.onPause()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        val title = TextView(this).apply {
            text = "Пиксель-герой"
            textSize = 26f
            setTextColor(Color.WHITE)
        }
        root.addView(title)

        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(preview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
            bottomMargin = dp(12)
        })

        stats = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.rgb(220, 230, 255))
        }
        root.addView(stats)

        // Выбор класса героя
        val classHead = TextView(this).apply {
            text = "Класс героя"
            textSize = 20f
            setTextColor(Color.rgb(255, 224, 138))
            setPadding(0, dp(16), 0, dp(4))
        }
        root.addView(classHead)
        val classRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (i in 0 until Balance.CLASS_NAMES.size) {
            val b = Button(this).apply {
                text = Balance.CLASS_NAMES[i]
                isAllCaps = false
                textSize = 12f
                setPadding(0, 0, 0, 0)
                setOnClickListener {
                    val g = Engine.get(this@MainActivity)
                    g.switchClass(i)
                    Engine.save(this@MainActivity)
                    render()
                }
            }
            classButtons.add(b)
            classRow.addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(classRow)
        classDesc = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(160, 170, 190))
        }
        root.addView(classDesc)

        // Снаряжение выбранного класса
        val gearHead = TextView(this).apply {
            text = "Снаряжение"
            textSize = 20f
            setTextColor(Color.rgb(255, 224, 138))
            setPadding(0, dp(16), 0, dp(4))
        }
        root.addView(gearHead)
        for (slot in Slot.values()) {
            val tv = TextView(this).apply {
                textSize = 14f
                setTextColor(Color.rgb(200, 210, 230))
                setPadding(0, dp(4), 0, dp(4))
                setOnClickListener {
                    val w = Engine.get(this@MainActivity).worn(slot)
                    if (w != null) showItem(w, true)
                }
            }
            slotViews.add(tv)
            root.addView(tv)
        }
        val gearRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        packBtn = Button(this).apply {
            isAllCaps = false
            textSize = 12f
            setOnClickListener { showBackpack() }
        }
        gearRow.addView(packBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val bestBtn = Button(this).apply {
            text = "Надеть лучшее"
            isAllCaps = false
            textSize = 12f
            setOnClickListener {
                val n = Engine.get(this@MainActivity).autoEquip()
                Engine.save(this@MainActivity)
                toast(if (n > 0) "Заменено предметов: $n" else "Лучшее уже надето")
                render()
            }
        }
        gearRow.addView(bestBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val junkBtn = Button(this).apply {
            text = "Продать лишнее"
            isAllCaps = false
            textSize = 12f
            setOnClickListener {
                val gold = Engine.get(this@MainActivity).sellJunk()
                Engine.save(this@MainActivity)
                toast(if (gold > 0) "Продано, +${Fmt.num(gold)} G" else "Лишнего нет")
                render()
            }
        }
        gearRow.addView(junkBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(gearRow)

        // Карточка смерти
        deadCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(58, 24, 30))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            visibility = View.GONE
        }
        deadText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
        }
        deadCard.addView(deadText)
        reviveBtn = Button(this).apply {
            isAllCaps = false
            text = "Воскресить за рекламу"
            setOnClickListener { watchAdRevive() }
        }
        deadCard.addView(reviveBtn)
        root.addView(deadCard, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
        })

        // Сундуки
        chestText = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(220, 230, 255))
            setPadding(0, dp(16), 0, dp(4))
        }
        root.addView(chestText)
        openBtn = Button(this).apply {
            isAllCaps = false
            text = "Открыть все"
            setOnClickListener { openChests(false) }
        }
        root.addView(openBtn)
        openAdBtn = Button(this).apply {
            isAllCaps = false
            text = "Открыть все: ×2 золота и предметов (реклама)"
            setOnClickListener {
                Ads.provider.showRewarded(
                    this@MainActivity,
                    onReward = { openChests(true) },
                    onFail = { toast(it) },
                )
            }
        }
        root.addView(openAdBtn)

        val head = TextView(this).apply {
            text = "Улучшения"
            textSize = 20f
            setTextColor(Color.rgb(255, 224, 138))
            setPadding(0, dp(16), 0, dp(4))
        }
        root.addView(head)

        for (u in Upgrade.values()) {
            val b = Button(this).apply {
                isAllCaps = false
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setOnClickListener {
                    if (Engine.get(this@MainActivity).buy(u)) {
                        Engine.save(this@MainActivity)
                        render()
                    }
                }
            }
            buttons[u] = b
            root.addView(b, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val fpsHead = TextView(this).apply {
            text = "Плавность виджета"
            textSize = 20f
            setTextColor(Color.rgb(255, 224, 138))
            setPadding(0, dp(16), 0, dp(4))
        }
        root.addView(fpsHead)
        val fpsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (f in intArrayOf(5, 10, 15)) {
            val b = Button(this).apply {
                text = "$f кадров/с"
                isAllCaps = false
                textSize = 12f
                setOnClickListener { Engine.setFps(this@MainActivity, f); render() }
            }
            fpsButtons[f] = b
            fpsRow.addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(fpsRow)
        val fpsNote = TextView(this).apply {
            text = "Больше кадров плавнее, но заметнее расход батареи. Анимация идёт только при включённом экране."
            textSize = 13f
            setTextColor(Color.rgb(160, 170, 190))
        }
        root.addView(fpsNote)

        val howTo = TextView(this).apply {
            text = "Как добавить виджет: удерживайте пустое место на домашнем экране → «Виджеты» → «Пиксель-герой». " +
                "Герой ходит и сражается сам, даже пока вы не смотрите: прогресс считается и в офлайне (до 8 часов). " +
                "Нажмите на виджет, чтобы вернуться сюда."
            textSize = 14f
            setTextColor(Color.rgb(160, 170, 190))
            setPadding(0, dp(20), 0, dp(8))
        }
        root.addView(howTo)

        val reset = Button(this).apply {
            text = "Начать заново"
            isAllCaps = false
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Начать заново?")
                    .setMessage("Весь прогресс героя будет удалён.")
                    .setPositiveButton("Да") { _, _ ->
                        Engine.reset(this@MainActivity)
                        render()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }
        root.addView(reset)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(20, 20, 31))
            addView(root)
        }
        setContentView(scroll)
    }

    private fun render() {
        val g = Engine.get(this)
        val d = BitmapDrawable(resources, Engine.renderBitmap(this))
        d.setFilterBitmap(false) // чёткие пиксели при увеличении
        preview.setImageDrawable(d)

        stats.text = "Зона: ${Balance.ZONE_NAMES[Balance.zoneOf(g.stage)]}\nУровень ${g.level}  ·  Стадия ${g.stage} (${g.kills + 1}/10)  ·  Золото ${Fmt.num(g.gold)}\n" +
            "Урон ${Fmt.num(g.attack().toLong())}  ·  Здоровье ${Fmt.num(g.hp.toLong())}/${Fmt.num(g.maxHp().toLong())}  ·  Скорость ${"%.1f".format(g.walkSpeed())}\n" +
            "Убито ${g.totalKills}  ·  Поражений ${g.deaths}"

        for ((i, slot) in Slot.values().withIndex()) {
            val w = g.worn(slot)
            slotViews[i].text = if (w == null) colored("${slot.title}: пусто", Color.rgb(120, 130, 150))
            else SpannableStringBuilder().append(slot.title + ": ").append(colored(w.name(), rarityColor(w.rarity))).append("  ур.${w.ilvl}\n   " + w.statLines().joinToString(", "))
        }
        for ((f, b) in fpsButtons) b.isEnabled = f != Engine.fps
        packBtn.text = "Рюкзак ${g.inventory.size}/${Gear.INV_CAP}"
        for ((i, b) in classButtons.withIndex()) b.isEnabled = i != g.heroClass
        classDesc.text = "${Balance.CLASS_NAMES[g.heroClass]}: ${Balance.CLASS_DESC[g.heroClass]}"

        if (g.dead) {
            deadCard.visibility = View.VISIBLE
            deadText.text = "Герой погиб!\nБез рекламы он очнётся через ${Math.ceil(g.deadTimer.toDouble()).toInt()} с, " +
                "но откатится на стадию назад. Реклама вернёт его сразу, с полным здоровьем, благословением (+50% урона, 1 мин) и щитом."
            reviveBtn.isEnabled = !adBusy
        } else {
            deadCard.visibility = View.GONE
        }

        val total = g.chestTotal()
        val parts = ArrayList<String>()
        for (r in Rarity.COUNT - 1 downTo 0) if (g.chests[r] > 0) parts.add("${Rarity.NAMES[r]} ×${g.chests[r]}")
        chestText.text = if (total == 0) "Сундуки: пока нет. Они выпадают из монстров, а с боссов всегда." else "Сундуки ($total): " + parts.joinToString(", ")
        openBtn.isEnabled = total > 0
        openAdBtn.isEnabled = total > 0 && !adBusy

        for ((u, b) in buttons) {
            val lv = g.levelOf(u)
            val maxed = lv >= u.maxLevel
            b.text = if (maxed) "${u.title}  ур.$lv  (максимум)" else "${u.title}  ур.$lv  →  ${hint(u)}   ·   ${Fmt.num(g.upgradeCost(u))} G"
            b.isEnabled = !maxed && g.canBuy(u)
            b.visibility = View.VISIBLE
        }
    }

    private fun hint(u: Upgrade): String = when (u) {
        Upgrade.SWORD -> "+5% урона"
        Upgrade.ARMOR -> "+5% здоровья"
        Upgrade.BOOTS -> "быстрее ходьба"
        Upgrade.LUCK -> "+12% золота"
    }
}
