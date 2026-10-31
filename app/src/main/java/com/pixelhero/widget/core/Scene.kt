package com.pixelhero.widget.core

import kotlin.math.sin

/**
 * Сцена виджета 384x96. Фон (небо, дальние слои, декор, земля, частицы) рисуется в 192x48 и увеличивается вдвое,
 * а герои, монстры, сундуки, полоски и текст рисуются поверх в полном разрешении.
 */
object Scene {
    const val W = 384
    const val H = 96
    private const val LW = 192
    private const val LH = 48
    private const val LG = 34              // земля в низком разрешении
    const val GROUND = 68                  // земля в полном (LG * 2)
    const val HERO_X = 24                  // левый край спрайта героя
    private const val MON_X0 = 70          // левый край монстра при dist = 0
    private val lo = Canvas(LW, LH)

    private class Theme(
        val skyTop: Int, val skyBot: Int, val far: Int, val hill1: Int, val hill2: Int,
        val grass: Int, val dirt: Int, val dirtDark: Int, val decor: Int, val decor2: Int,
        val stars: Boolean, val orb: Int, val farKind: Int, val decorKind: Int, val ambient: Int, val accent: Int,
    )

    private const val F_MOUNT = 0; private const val F_CASTLE = 1; private const val F_VOLCANO = 2; private const val F_PINES = 3
    private const val F_ROCKS = 4; private const val F_SPIKES = 5; private const val F_PYRAMID = 6; private const val F_ISLANDS = 7
    private const val A_LEAVES = 0; private const val A_DRIPS = 1; private const val A_DUST = 2; private const val A_FOG = 3
    private const val A_EMBERS = 4; private const val A_SNOW = 5; private const val A_FIREFLY = 6; private const val A_SPARKLE = 7
    private const val A_ASH = 8; private const val A_BIRDS = 9

    private fun t(
        skyTop: Int, skyBot: Int, far: Int, hill1: Int, hill2: Int, grass: Int, dirt: Int, dirtDark: Int,
        decor: Int, decor2: Int, stars: Boolean, orb: Int, farKind: Int, decorKind: Int, ambient: Int, accent: Int,
    ) = Theme(rgb(skyTop), rgb(skyBot), rgb(far), rgb(hill1), rgb(hill2), rgb(grass), rgb(dirt), rgb(dirtDark), rgb(decor), rgb(decor2), stars, if (orb == 0) 0 else rgb(orb), farKind, decorKind, ambient, rgb(accent))

    private val THEMES = arrayOf(
        t(0x5DB7FF, 0xBFE8FF, 0x8FB9D9, 0x7AC77A, 0x4F9D58, 0x4CAF50, 0x8A5A2B, 0x6E4521, 0x2E7D32, 0x6E4521, false, 0xFFF2A0, F_MOUNT, 0, A_LEAVES, 0xFF7AA8),   // 0 лес
        t(0x3FA9A0, 0xB6E8A8, 0x2F8A6A, 0x2E8B4E, 0x1F6B3E, 0x2E9E4A, 0x6B4A25, 0x4E3419, 0x1F7A3A, 0x7A5A2A, false, 0xFFF6B0, F_PINES, 1, A_FIREFLY, 0xFF5AA0), // 1 джунгли
        t(0x14142A, 0x3A3358, 0x2A2645, 0x4A4466, 0x332E4D, 0x5B5675, 0x2B2740, 0x1E1B30, 0x6A6490, 0x6AF0FF, true, 0, F_SPIKES, 2, A_DRIPS, 0x6AF0FF),               // 2 пещера
        t(0x1E1A16, 0x4A3A2A, 0x34281E, 0x4E3C2A, 0x382A1E, 0x6A5A44, 0x3A2C1E, 0x28201A, 0x7A5A34, 0xFFC04A, false, 0, F_SPIKES, 3, A_DUST, 0xFFC04A),            // 3 шахты
        t(0xF2B36B, 0xF8E0A6, 0xD9A06A, 0xC9A066, 0xA87F4A, 0xD9B36A, 0x9B6B3A, 0x7E5429, 0x5E8C3A, 0x7E5429, false, 0xFF9A3A, F_MOUNT, 4, A_DUST, 0xE8C98A),       // 4 пустошь
        t(0xE8A24A, 0xFFE6A8, 0xC98A4A, 0xD9A35A, 0xB07E3E, 0xE0BC6A, 0xA87A42, 0x7E5A2E, 0xD8B878, 0x7A5A34, false, 0xFFE070, F_PYRAMID, 5, A_DUST, 0x6AE0D0),   // 5 храм
        t(0x2C4A3A, 0x8FB08A, 0x2A4535, 0x4F7A55, 0x365A40, 0x4C7A3A, 0x3A3A22, 0x2A2A18, 0x3A2E22, 0x5E7A3A, false, 0, F_PINES, 6, A_FIREFLY, 0xC8FF6A),         // 6 болото
        t(0x10112A, 0x4A4F7A, 0x23264A, 0x34386A, 0x262A52, 0x3F4A5A, 0x2A2E3E, 0x1C1F2C, 0x8A90A8, 0x5A6078, true, 0xDDE6FF, F_MOUNT, 7, A_FOG, 0x9AF0C0),         // 7 кладбище
        t(0x3A2A6A, 0xB48FD1, 0x4A3A72, 0x6B5B95, 0x4B3F72, 0x7A7A8C, 0x4A4A5E, 0x35354A, 0x8A8AA0, 0x5A5A70, true, 0xF0EAFF, F_CASTLE, 8, A_FOG, 0xFFD84A),        // 8 замок
        t(0x8FD0FF, 0xEAF8FF, 0xB8D8F0, 0xDCEBF7, 0xB4CFE6, 0xF2FAFF, 0x7FA0C0, 0x5E82A6, 0x9AD8FF, 0xFFFFFF, false, 0xFFFFFF, F_MOUNT, 9, A_SNOW, 0xBFEFFF),     // 9 лёд
        t(0x3E9BFF, 0xD8F0FF, 0xA8D4F4, 0xE8F4FF, 0xC4E0F8, 0x7CD36A, 0xB8A488, 0x8E7C62, 0xFFFFFF, 0xFFD24A, false, 0xFFF6B0, F_ISLANDS, 10, A_BIRDS, 0xFFD24A), // 10 небесные острова
        t(0x3A0F0F, 0xD2461E, 0x4A1818, 0x5A1F1F, 0x3A1212, 0x7A2B1A, 0x2B1010, 0x1A0808, 0x2A1010, 0xFF7A1A, false, 0, F_VOLCANO, 11, A_EMBERS, 0xFFB84A),          // 11 вулкан
        t(0x2A0A0A, 0x9A3A12, 0x3A1410, 0x5A2418, 0x3E1812, 0x6A3A1A, 0x2E1608, 0x1E0E04, 0xFFD24A, 0x7A5A2A, false, 0, F_SPIKES, 12, A_EMBERS, 0xFFE08A),         // 12 логово
        t(0x0A0220, 0x5A2A8A, 0x2A1450, 0x3E1F6E, 0x2A1450, 0x4A2A7A, 0x1E1038, 0x120A24, 0xFF6AE0, 0x6AF0FF, true, 0, F_ROCKS, 13, A_SPARKLE, 0xFF9AF0),         // 13 бездна
        t(0x020208, 0x1A1040, 0x100A30, 0x1E1450, 0x120C34, 0x2A2060, 0x0E0A24, 0x080618, 0x6A7AFF, 0xFFFFFF, true, 0xC0B8FF, F_ROCKS, 14, A_SPARKLE, 0x9AB0FF),   // 14 пустота
        t(0x120303, 0x7A1010, 0x2A0A0A, 0x3A0E0E, 0x240808, 0x4A1410, 0x1E0808, 0x120404, 0xD8D0B8, 0x8A8470, false, 0, F_SPIKES, 15, A_ASH, 0xFF5A1A),            // 15 ад
    )

    fun render(g: GameState, c: Canvas) {
        val zone = Balance.zoneOf(g.stage)
        val th = THEMES[zone]
        drawBackground(g, lo, th, zone)
        drawGround(g, lo, th, zone)
        drawAmbient(g, lo, th, zone)
        upscale2(lo, c)
        drawGroundDetail(g, c, th, zone)
        drawChestDrop(g, c)
        drawActors(g, c, zone)
        if (g.dead) c.dim(0.5f)
        drawHud(g, c)
        if (g.dead) drawDeadOverlay(g, c)
    }

    private fun upscale2(src: Canvas, dst: Canvas) {
        val sw = src.w
        val dw = dst.w
        for (y in 0 until src.h) {
            val row = y * 2 * dw
            for (x in 0 until sw) {
                val v = src.px[y * sw + x]
                val i = row + x * 2
                dst.px[i] = v; dst.px[i + 1] = v; dst.px[i + dw] = v; dst.px[i + dw + 1] = v
            }
        }
    }

    /** Мелкие детали земли в полном разрешении: травинки на кромке и тёмная полоса под ней. */
    private fun drawGroundDetail(g: GameState, c: Canvas, t: Theme, zone: Int) {
        val s = (g.scroll * 2f).toInt()
        val light = lighten(t.grass, 0.3f)
        val dark = darken(t.grass, 0.28f)
        for (x in 0 until W) {
            val wx = x + s
            val h = wx and 0xFFFF
            if (h % 5 == 0) c.put(x, GROUND - 1, light)
            if (h % 11 == 3) { c.put(x, GROUND - 1, t.grass); c.put(x, GROUND - 2, light) }
            if (h % 17 == 7) c.put(x, GROUND + 1, dark)
            if (h % 23 == 5) { c.put(x, GROUND + 3, lighten(t.dirt, 0.18f)); c.put(x + 1, GROUND + 4, darken(t.dirt, 0.25f)) }
        }
    }

    private fun pmod(a: Double, m: Double): Double = ((a % m) + m) % m

    private fun circle(c: Canvas, cx: Int, cy: Int, r: Int, col: Int) {
        for (j in -r..r) for (i in -r..r) if (i * i + j * j <= r * r + r / 2) c.put(cx + i, cy + j, col)
    }

    private fun ellipse(c: Canvas, cx: Int, cy: Int, rx: Int, ry: Int, col: Int) {
        for (j in -ry..ry) for (i in -rx..rx) {
            val a = i.toDouble() / (rx + 0.5)
            val b = j.toDouble() / (ry + 0.5)
            if (a * a + b * b <= 1.0) c.put(cx + i, cy + j, col)
        }
    }

    private fun tri(c: Canvas, cx: Int, baseY: Int, h: Int, halfW: Int, col: Int) {
        for (j in 0 until h) {
            val w = ((halfW * (j + 1)).toDouble() / h).toInt()
            c.rect(cx - w, baseY - h + 1 + j, w * 2 + 1, 1, col)
        }
    }

    private fun lighten(col: Int, a: Float) = lerpColor(col, rgb(0xFFFFFF), a)
    private fun darken(col: Int, a: Float) = lerpColor(col, rgb(0x000000), a)

    // ---------- верхний слой, полное разрешение 384x96 ----------
    private fun drawChestDrop(g: GameState, c: Canvas) {
        if (g.dropRarity < 0 || g.dropTimer <= 0f || g.dead) return
        val age = 3f - g.dropTimer
        val art = Sprites.CHEST
        val x = 88 - ((g.scroll - g.dropScroll) * 2f).toInt()
        if (x < HERO_X + 30) return
        val fall = if (age < 0.4f) ((0.4f - age) * 110f).toInt() else 0
        val y = GROUND + 4 - art.size - fall
        val glow = rgb(Rarity.COLORS[g.dropRarity])
        if (g.dropRarity >= 2) {
            val a = lerpColor(glow, rgb(0xFFFFFF), 0.35f)
            val tw = ((g.anim * 6).toInt() % 2)
            for (k in 0 until 3 + g.dropRarity) {
                val px = x + 3 + (k * 7 + tw * 3) % 26
                val py = y - 3 - (k * 5 + tw * 2) % 10
                c.put(px, py, a); if (k % 2 == 0) { c.put(px - 1, py, glow); c.put(px + 1, py, glow); c.put(px, py - 1, glow) }
            }
        }
        c.shadowE(x + 15, GROUND + 2, 14, 3)
        c.sprite(art, x, y, false, Sprites.chestPalette(g.dropRarity))
    }

    private fun drawDeadOverlay(g: GameState, c: Canvas) {
        fun center(s: String, y: Int, col: Int, k: Int) = c.textS(s, (W - c.textWidthS(s, k)) / 2, y, col, k)
        center("RIP", 20, rgb(0xFF5050), 3)
        val blink = ((g.anim * 2).toInt() % 2) == 0
        center("TAP TO REVIVE", 44, if (blink) rgb(0xFFFFFF) else rgb(0xFFE08A), 2)
        center(Math.ceil(g.deadTimer.toDouble()).toInt().coerceAtLeast(0).toString() + "S", 60, rgb(0xB8B0E0), 2)
    }

    // ---------- фон ----------
    private fun drawBackground(g: GameState, c: Canvas, t: Theme, zone: Int) {
        for (y in 0 until LG) {
            val col = lerpColor(t.skyTop, t.skyBot, y / (LG - 1).toFloat())
            for (x in 0 until LW) c.put(x, y, col)
        }
        // шахматное смешивание на границах полос неба, чтобы градиент не был ступенчатым
        for (y in 2 until LG - 1 step 4) for (x in 0 until LW) {
            if ((x + y) % 2 == 0) c.put(x, y, lerpColor(t.skyTop, t.skyBot, (y + 1) / (LG - 1).toFloat()))
        }
        if (t.stars) {
            for (i in 0 until 30) {
                val sx = (i * 37 + 11) % LW
                val sy = (i * 13 + 3) % 18 + 1
                val twinkle = ((g.anim * 1.5 + i * 0.7).toInt() % 3) == 0
                val col = if (twinkle) rgb(0xFFFFFF) else if (i % 3 == 0) rgb(0xCFC8F0) else rgb(0x8E88B8)
                c.put(sx, sy, col)
                if (twinkle && i % 4 == 0) { c.put(sx - 1, sy, rgb(0x8E88B8)); c.put(sx + 1, sy, rgb(0x8E88B8)) }
            }
        }
        if (t.orb != 0) {
            val ox = 150 - (g.scroll * 0.01f).toInt() % 6
            val dark = zone == 7 || zone == 8
            circle(c, ox, 10, 6, lerpColor(t.orb, t.skyTop, 0.6f))
            circle(c, ox, 10, 5, lerpColor(t.orb, t.skyTop, 0.25f))
            circle(c, ox, 10, 4, t.orb)
            if (dark) { c.put(ox - 2, 8, lerpColor(t.orb, rgb(0x6A6A9A), 0.5f)); c.put(ox + 1, 11, lerpColor(t.orb, rgb(0x6A6A9A), 0.5f)); c.put(ox - 1, 12, lerpColor(t.orb, rgb(0x6A6A9A), 0.5f)) }
        }
        if (!t.stars && zone != 11 && zone != 12 && zone != 15 && zone != 3) {
            for (i in 0 until 4) { // облака с тенью снизу
                val cx = (((i * 61 + 25) - g.scroll * 0.08f).toInt() % 220 + 220) % 220 - 16
                val cy = 6 + (i * 5) % 9
                val cc = if (zone == 6) rgb(0x9AB89A) else rgb(0xFFFFFF)
                val sh = lerpColor(cc, t.skyBot, 0.55f)
                c.rect(cx, cy, 14, 3, cc)
                c.rect(cx + 3, cy - 1, 8, 1, cc)
                c.rect(cx + 5, cy - 2, 4, 1, cc)
                c.rect(cx + 1, cy + 3, 12, 1, sh)
            }
        }
        drawFar(g, c, t)
        for (x in 0 until LW) {
            val h2 = 5 + ((sin((x + g.scroll * 0.18) * 0.05) + 1.0) * 3.5).toInt()
            c.rect(x, LG - h2, 1, h2, t.hill2)
            c.put(x, LG - h2, lighten(t.hill2, 0.12f))
        }
        for (x in 0 until LW) {
            val h1 = 2 + ((sin((x + g.scroll * 0.35) * 0.08 + 1.7) + 1.0) * 2.6).toInt()
            c.rect(x, LG - h1, 1, h1, t.hill1)
            c.put(x, LG - h1, lighten(t.hill1, 0.2f))
        }
        val offFar = (g.scroll * 0.45f).toInt()
        for (sx in -16 until LW + 16) {
            val wx = sx + offFar
            if (wx % 53 == 0) decor(c, t, sx, (wx / 53) % 3, true)
        }
        val off = (g.scroll * 0.7f).toInt()
        for (sx in -16 until LW + 16) {
            val wx = sx + off
            if (wx % 37 == 0) decor(c, t, sx, (wx / 37) % 3, false)
        }
        if (zone == 11 || zone == 12) { // лавовые трещины
            for (x in 0 until LW) if ((x + (g.scroll * 0.5f).toInt()) % 11 < 3) c.put(x, LG - 1, rgb(0xFF7A1A))
        }
    }

    private fun drawFar(g: GameState, c: Canvas, t: Theme) {
        val sc = g.scroll * 0.1
        for (x in 0 until LW) {
            val wx = x + sc
            val h: Int = when (t.farKind) {
                F_MOUNT -> (13 + (sin(wx * 0.04) * 6 + sin(wx * 0.11 + 1.0) * 3 + 6)).toInt()
                F_CASTLE -> {
                    val m = pmod(wx, 60.0)
                    if (m < 12) 22 + (if (m.toInt() % 4 < 2) 2 else 0) else if (m < 18) 12 else 10
                }
                F_VOLCANO -> {
                    val m = pmod(wx, 150.0)
                    val d = Math.abs(m - 75.0)
                    if (d < 9) 25 - (if (d < 5) 3 else 0) else (25 - (d - 9) * 0.4).toInt().coerceAtLeast(7)
                }
                F_PINES -> {
                    val m = pmod(wx, 11.0)
                    12 + (if (m < 5.5) (m * 1.8).toInt() else ((11 - m) * 1.8).toInt()) + ((wx / 11).toInt() % 3)
                }
                F_ROCKS -> 6 + (sin(wx * 0.07) * 3 + 3).toInt()
                F_PYRAMID -> {
                    val m = pmod(wx, 90.0)
                    val d = Math.abs(m - 45.0)
                    (24 - d * 0.9).toInt().coerceAtLeast(5)
                }
                F_ISLANDS -> 0
                else -> {
                    val m = pmod(wx, 18.0)
                    9 + (if (m < 9) m.toInt() * 1 else (18 - m).toInt() * 1) + ((wx / 18).toInt() % 3) * 3
                }
            }
            if (h > 0) c.rect(x, LG - h, 1, h, t.far)
            if (h > 0 && t.farKind == F_PYRAMID) {
                val m = pmod(wx, 90.0)
                if (m < 45.0 && h > 5) c.put(x, LG - h, lighten(t.far, 0.3f)) else if (h > 5) c.put(x, LG - h, darken(t.far, 0.2f))
            }
            if (t.farKind == F_VOLCANO && Math.abs(pmod(wx, 150.0) - 75.0) < 4) c.put(x, LG - h, rgb(0xFF7A1A))
            if (t.farKind == F_CASTLE && pmod(wx, 60.0) in 4.0..6.0) c.put(x, LG - 16, rgb(0xFFD84A))
        }
        if (t.farKind == F_ROCKS || t.farKind == F_ISLANDS) { // парящие острова/камни
            val isl = t.farKind == F_ISLANDS
            for (i in 0 until 5) {
                val rx = (pmod(i * 47.0 + 12 - g.scroll * 0.05, 240.0) - 20).toInt()
                val ry = 7 + (i * 5) % 14 + (sin(g.anim * 0.8 + i * 1.7) * 1.5).toInt()
                val w = if (isl) 12 else 7
                c.rect(rx, ry, w, 2, if (isl) rgb(0x7CD36A) else t.decor)
                c.rect(rx + 1, ry + 2, w - 2, 2, if (isl) rgb(0x9A7A52) else darken(t.decor, 0.4f))
                c.rect(rx + 3, ry + 4, w - 6, 2, if (isl) rgb(0x7A5E3E) else darken(t.decor, 0.55f))
                c.put(rx + 1, ry, rgb(0xFFFFFF))
            }
        }
    }

    // ---------- декор ----------
    private fun decor(c: Canvas, t: Theme, x: Int, v: Int, far: Boolean) {
        val base = LG - 1
        val main = if (far) lerpColor(t.decor, t.far, 0.55f) else t.decor
        val sec = if (far) lerpColor(t.decor2, t.far, 0.55f) else t.decor2
        val hi = lighten(main, 0.25f)
        val lo = darken(main, 0.3f)
        val k = if (far) 0 else 1
        when (t.decorKind) {
            0 -> { // дерево: ствол, крона из эллипсов, блики, цветок
                val h = 9 + v * 2 + k * 3
                c.rect(x, base - h + 4, 2 + k, h - 3, sec)
                c.put(x, base - h / 2, darken(sec, 0.3f))
                ellipse(c, x + 1, base - h + 2, 4 + k * 2, 3 + k, main)
                ellipse(c, x - 2 - k, base - h + 4, 3 + k, 2 + k, lo)
                ellipse(c, x + 4 + k, base - h + 4, 3 + k, 2 + k, lo)
                ellipse(c, x + 1, base - h + 1, 3 + k, 2, hi)
                if (v == 2 && !far) { c.put(x + 3, base - h + 3, t.accent); c.put(x - 2, base - h + 4, t.accent) }
            }
            1 -> { // пальма / лиана
                val h = 12 + v * 2 + k * 3
                for (j in 0 until h) c.put(x + (j * 3 / h) - (if (j > h / 2) 1 else 0), base - j, sec)
                if (!far) for (j in 0 until h) c.put(x + 1 + (j * 3 / h) - (if (j > h / 2) 1 else 0), base - j, darken(sec, 0.3f))
                val tx = x + 2
                for (i in 0..5) { c.put(tx - i, base - h + 1 + i / 2, main); c.put(tx + i, base - h + 1 + i / 2, main) }
                for (i in 0..3) { c.put(tx - i, base - h - 1 + i / 3, hi); c.put(tx + i, base - h - 1 + i / 3, hi) }
                c.put(tx, base - h, hi)
                if (!far) c.rect(x - 3, base - h + 2, 1, 6, lo)
            }
            2 -> { // сталактиты, кристаллы, камни
                val h = 6 + v * 3
                tri(c, x, h - 1 + 0, h, 2 + v, main)
                for (i in 0 until h) c.put(x - 1, i, lo)
                if (!far) {
                    c.rect(x + 8, base - 4, 1, 4, sec); c.rect(x + 9, base - 7, 2, 7, sec); c.rect(x + 11, base - 3, 1, 3, sec)
                    c.put(x + 9, base - 7, rgb(0xFFFFFF)); c.put(x + 9, base - 6, lighten(sec, 0.5f))
                    ellipse(c, x + 3, base - 1, 3, 1, lo)
                }
            }
            3 -> { // шахтная крепь с фонарём и рельсами
                val h = 14 + v
                c.rect(x, base - h, 2, h, main); c.rect(x + 10, base - h, 2, h, main)
                c.rect(x - 1, base - h, 14, 2, sec.let { darken(main, 0.1f) })
                c.rect(x - 1, base - h + 1, 14, 1, lo)
                c.put(x, base - h + 2, hi); c.put(x + 10, base - h + 2, hi)
                if (!far) {
                    c.rect(x + 5, base - h + 2, 1, 2, rgb(0x6A5A44))
                    c.rect(x + 4, base - h + 4, 3, 3, t.accent)
                    c.put(x + 5, base - h + 5, rgb(0xFFFFFF))
                    c.rect(x - 2, base, 16, 1, rgb(0x8A8A98))
                }
            }
            4 -> { // кактус
                val h = 9 + v * 2 + k
                c.rect(x + 1, base - h, 3, h, main)
                c.rect(x + 1, base - h, 1, h, hi); c.rect(x + 3, base - h, 1, h, lo)
                c.rect(x - 2, base - h / 2 - 2, 3, 2, main); c.rect(x - 2, base - h / 2 - 5, 2, 4, main)
                c.rect(x + 4, base - h / 2 + 1, 3, 2, main); c.rect(x + 5, base - h / 2 - 2, 2, 4, main)
                if (!far) { c.put(x + 2, base - h - 1, t.accent); c.put(x + 1, base - h - 1, t.accent) }
            }
            5 -> { // колонна с иероглифами и статуя
                val h = 14 + v * 2
                c.rect(x, base - h, 6, h, main)
                c.rect(x, base - h, 1, h, hi); c.rect(x + 5, base - h, 1, h, lo)
                c.rect(x - 1, base - h, 8, 2, sec); c.rect(x - 1, base - 1, 8, 2, sec)
                if (!far) for (j in 0 until h / 3) { c.put(x + 2, base - h + 4 + j * 3, t.accent); c.put(x + 3, base - h + 5 + j * 3, darken(t.accent, 0.3f)) }
            }
            6 -> { // коряга с мхом и висящими лозами
                val h = 12 + v * 2
                c.rect(x + 1, base - h, 2, h, main)
                c.rect(x - 2, base - h + 3, 3, 1, main); c.put(x - 2, base - h + 2, main); c.put(x - 3, base - h + 1, main)
                c.rect(x + 3, base - h + 6, 4, 1, main); c.put(x + 6, base - h + 5, main); c.put(x + 7, base - h + 4, main)
                if (!far) { for (j in 0 until 5) c.put(x - 2, base - h + 4 + j, sec); for (j in 0 until 4) c.put(x + 6, base - h + 7 + j, sec); c.rect(x - 1, base - 1, 6, 1, sec) }
            }
            7 -> { // надгробия: крест, плита, обелиск
                when (v) {
                    0 -> { c.rect(x + 2, base - 11, 2, 11, main); c.rect(x - 1, base - 8, 8, 2, main); c.put(x + 2, base - 11, hi); c.rect(x + 3, base - 10, 1, 9, lo) }
                    1 -> { c.rect(x, base - 8, 7, 8, main); c.rect(x + 1, base - 9, 5, 1, main); c.rect(x, base - 8, 1, 8, hi); c.rect(x + 2, base - 5, 3, 1, lo); c.rect(x + 3, base - 6, 1, 3, lo) }
                    else -> { c.rect(x + 1, base - 13, 4, 13, main); c.rect(x + 2, base - 14, 2, 1, main); c.rect(x + 1, base - 13, 1, 13, hi); c.rect(x, base - 1, 6, 1, sec) }
                }
            }
            8 -> { // колонна и знамя замка
                val h = 14 + v * 2
                c.rect(x, base - h, 5, h, main)
                c.rect(x, base - h, 1, h, hi); c.rect(x + 4, base - h, 1, h, lo)
                c.rect(x - 1, base - h, 7, 2, sec); c.rect(x - 1, base - 1, 7, 2, sec)
                if (!far && v != 1) { c.rect(x + 6, base - h + 2, 4, 7, rgb(0x9A2030)); c.rect(x + 6, base - h + 9, 2, 1, rgb(0x9A2030)); c.put(x + 7, base - h + 4, t.accent); c.put(x + 8, base - h + 4, t.accent) }
            }
            9 -> { // ледяные шипы с бликами
                val h = 9 + v * 3
                tri(c, x, base, h, 3, main)
                tri(c, x + 5, base, h - 4, 2, lighten(main, 0.1f))
                for (j in 2 until h - 1) c.put(x - 1 + j / 5, base - j, lighten(main, 0.6f))
                if (!far) c.rect(x + 8, base - 2, 3, 2, sec)
            }
            10 -> { // облачные колонны и золотые арки
                val h = 11 + v * 2
                c.rect(x, base - h, 4, h, rgb(0xF4F8FF))
                c.rect(x + 3, base - h, 1, h, rgb(0xC8D8EC))
                c.rect(x - 1, base - h, 6, 2, t.accent); c.rect(x - 1, base - 1, 6, 1, t.accent)
                ellipse(c, x + 8, base - 3, 4, 2, rgb(0xFFFFFF)); ellipse(c, x + 6, base - 4, 3, 2, rgb(0xE6F0FF))
            }
            11 -> { // вулканические шипы
                val h = 9 + v * 3
                tri(c, x, base, h, 3, main)
                c.put(x, base - 2, sec); if (!far) { c.put(x - 1, base - 5, sec); c.put(x + 1, base - 7, sec) }
                for (j in 1 until h - 1) c.put(x - 1 + j / 5, base - j, lighten(main, 0.18f))
            }
            12 -> { // горка золота и сталагмит
                if (!far) {
                    ellipse(c, x + 4, base - 1, 6, 3, t.decor)
                    ellipse(c, x + 4, base - 3, 4, 2, lighten(t.decor, 0.2f))
                    c.put(x + 2, base - 4, rgb(0xFFFFFF)); c.put(x + 6, base - 3, rgb(0xFFFFFF)); c.put(x + 4, base - 5, rgb(0xFFF0A0))
                    tri(c, x + 12, base, 8 + v * 2, 2, t.decor2)
                } else tri(c, x + 4, base, 7 + v * 2, 3, main)
            }
            13 -> { // кристаллы
                val h = 8 + v * 3
                c.rect(x + 1, base - h, 3, h, main); c.rect(x + 2, base - h - 1, 1, 1, hi)
                c.rect(x - 2, base - h / 2, 2, h / 2, sec); c.rect(x + 4, base - h / 2 - 2, 2, h / 2 + 2, sec)
                c.put(x + 1, base - h, rgb(0xFFFFFF)); c.rect(x + 1, base - h + 1, 1, 3, hi)
                if (!far) c.rect(x - 4, base - 1, 12, 1, darken(sec, 0.3f))
            }
            14 -> { // светящийся монолит
                val h = 13 + v * 2
                c.rect(x, base - h, 5, h, darken(t.decor, 0.55f))
                c.rect(x, base - h, 1, h, darken(t.decor, 0.2f)); c.rect(x + 4, base - h, 1, h, darken(t.decor, 0.75f))
                if (!far) for (j in 0 until 3) { c.put(x + 2, base - h + 3 + j * 3, t.decor2); c.put(x + 1 + j % 2, base - h + 4 + j * 3, t.decor) }
                c.put(x + 2, base - h - 2, t.decor2)
            }
            else -> { // костяные шипы-рёбра
                val h = 12 + v * 3
                c.rect(x + 1, base - h, 3, h, main)
                c.rect(x + 1, base - h, 1, h, hi); c.rect(x + 3, base - h, 1, h, lo)
                c.rect(x - 2, base - h + 3, 3, 1, main); c.rect(x - 3, base - h + 2, 1, 2, main)
                c.rect(x + 4, base - h + 6, 3, 1, main); c.rect(x + 6, base - h + 5, 1, 2, main)
                c.put(x + 2, base - h - 1, sec)
                if (!far) c.rect(x - 1, base, 7, 1, sec)
            }
        }
    }

    private fun drawGround(g: GameState, c: Canvas, t: Theme, zone: Int) {
        c.rect(0, LG, LW, 2, t.grass)
        c.rect(0, LG + 2, LW, LH - LG - 2, t.dirt)
        val s = g.scroll.toInt()
        val light = lighten(t.grass, 0.22f)
        val dark = darken(t.grass, 0.2f)
        for (x in 0 until LW) {
            val wx = x + s
            if (wx % 7 == 0) c.put(x, LG + 4, t.dirtDark)
            if (wx % 11 == 3) c.put(x, LG + 7, t.dirtDark)
            if (wx % 5 == 1) c.put(x, LG, light)
            if (wx % 13 == 6) c.put(x, LG + 1, dark)
            if (wx % 17 == 9) { c.put(x, LG + 5, lighten(t.dirt, 0.25f)); c.put(x + 1, LG + 5, darken(t.dirt, 0.25f)); c.put(x, LG + 6, darken(t.dirt, 0.3f)) }
            if (wx % 19 == 4) { c.put(x, LG + 9, lighten(t.dirt, 0.2f)); c.put(x + 1, LG + 9, t.dirtDark) }
            if (wx % 9 == 2) c.put(x, LG - 1, t.grass)
            if (wx % 23 == 4) { c.put(x, LG - 1, if (zone == 0 || zone == 1 || zone == 6) t.accent else light); if (zone == 0) c.put(x, LG - 2, t.accent) }
            if (wx % 29 == 11) c.put(x, LG - 1, dark)
        }
        for (x in 0 until LW) c.put(x, LH - 2, darken(t.dirt, 0.3f))
        c.rect(0, LH - 1, LW, 1, rgb(0x14141F))
        val frac = (g.xp.toDouble() / g.xpNeed()).coerceIn(0.0, 1.0)
        c.rect(0, LH - 1, (LW * frac).toInt(), 1, rgb(0x4DE3FF))
    }

    // ---------- частицы ----------
    private fun drawAmbient(g: GameState, c: Canvas, t: Theme, zone: Int) {
        val tm = g.anim.toDouble()
        val sc = g.scroll.toDouble()
        val wd = LW.toDouble()
        when (t.ambient) {
            A_LEAVES -> for (i in 0 until 9) {
                val x = pmod(i * 29.0 + sin(tm * 0.9 + i) * 4 - tm * 6 - sc * 0.15, wd).toInt()
                val y = pmod(i * 7.0 + tm * 3.5, LG.toDouble()).toInt()
                c.put(x, y, if (i % 2 == 0) rgb(0x6FBF4A) else rgb(0xC98A3A))
            }
            A_DRIPS -> for (i in 0 until 6) {
                val x = (i * 33 + 14 - (sc * 0.4).toInt() % LW + LW) % LW
                val y = pmod(i * 6.0 + tm * 16, LG + 2.0).toInt()
                if (y < LG) { c.put(x, y, t.accent); if (y > 0) c.put(x, y - 1, lerpColor(t.accent, t.skyTop, 0.6f)) }
            }
            A_DUST -> for (i in 0 until 14) {
                val x = pmod(i * 23.0 - tm * 26 - sc * 0.3, wd).toInt()
                val y = 16 + (i * 5) % 14 + (sin(tm + i) * 1.2).toInt()
                c.put(x, y, t.accent); if (i % 3 == 0) c.put(x + 1, y, lighten(t.accent, 0.4f))
            }
            A_FOG -> for (j in 0 until 6) for (x in 0 until LW) {
                val y = LG - 6 + j
                val a = 0.06f + 0.07f * (0.5f + 0.5f * sin((x + sc * 0.2) * 0.1 + tm * 0.6 + j).toFloat())
                val idx = y * LW + x
                c.px[idx] = lerpColor(c.px[idx], if (zone == 7) rgb(0xBFD0F0) else rgb(0xE0D0FF), a)
            }
            A_EMBERS -> for (i in 0 until 16) {
                val x = pmod(i * 13.0 + sin(tm * 1.3 + i) * 3 - sc * 0.2, wd).toInt()
                val y = LG - 1 - pmod(tm * 7 + i * 6.0, LG.toDouble()).toInt()
                c.put(x, y, if (i % 3 == 0) rgb(0xFFE08A) else rgb(0xFF7A1A))
            }
            A_SNOW -> for (i in 0 until 28) {
                val x = pmod(i * 7.3 + sin(tm * 0.8 + i) * 2.5 - tm * 5 - sc * 0.2, wd).toInt()
                val y = pmod(i * 5.1 + tm * (5.0 + i % 3), LG + 4.0).toInt()
                if (y < LH - 1) c.put(x, y, rgb(0xFFFFFF))
            }
            A_FIREFLY -> for (i in 0 until 12) {
                val x = pmod(i * 17.0 + sin(tm * 0.7 + i * 2) * 8 - sc * 0.25, wd).toInt()
                val y = 14 + (i * 3) % 14 + (sin(tm * 0.9 + i) * 3).toInt()
                if (((tm * 1.4 + i * 0.9).toInt() % 3) != 0) {
                    c.put(x, y, t.accent)
                    c.put(x + 1, y, lerpColor(t.accent, t.skyTop, 0.65f)); c.put(x - 1, y, lerpColor(t.accent, t.skyTop, 0.65f))
                }
            }
            A_SPARKLE -> for (i in 0 until 18) {
                val x = (i * 41 + 7) % LW
                val y = (i * 19 + 3) % (LG - 3) + 1
                val ph = ((tm * 2.0 + i * 0.37) % 2.0)
                if (ph < 0.5) {
                    val col = if (i % 2 == 0) rgb(0xFF9AF0) else rgb(0x9AF0FF)
                    c.put(x, y, rgb(0xFFFFFF))
                    if (ph in 0.15..0.35) { c.put(x - 1, y, col); c.put(x + 1, y, col); c.put(x, y - 1, col); c.put(x, y + 1, col) }
                }
            }
            A_ASH -> {
                for (i in 0 until 18) {
                    val x = pmod(i * 11.0 + sin(tm + i) * 3 - tm * 3 - sc * 0.2, wd).toInt()
                    val y = pmod(i * 5.0 + tm * 4, LG.toDouble()).toInt()
                    c.put(x, y, if (i % 4 == 0) rgb(0xFF5A1A) else rgb(0x8A7A78))
                }
                val ph = tm % 9.0
                if (ph in 0.0..0.12) for (y in 0 until LG) for (x in 0 until LW) {
                    val idx = y * LW + x
                    c.px[idx] = lerpColor(c.px[idx], rgb(0xFFB8A0), 0.35f)
                }
            }
            A_BIRDS -> for (i in 0 until 3) {
                val x = pmod(i * 71.0 - tm * (9 + i * 2) - sc * 0.1, wd + 20) .toInt() - 10
                val y = 8 + i * 6 + (sin(tm * 3 + i) * 1.5).toInt()
                val up = ((tm * 6 + i).toInt() % 2) == 0
                val bc = rgb(0x2A3A5A)
                c.put(x, y, bc); c.put(x + 3, y, bc)
                if (up) { c.put(x + 1, y - 1, bc); c.put(x + 2, y - 1, bc) } else { c.put(x + 1, y + 1, bc); c.put(x + 2, y + 1, bc) }
            }
        }
    }

    private fun bar(c: Canvas, x: Int, y: Int, w: Int, frac: Double, fill: Int) {
        c.rect(x - 1, y - 1, w + 2, 6, rgb(0x14141F))
        c.rect(x, y, w, 4, rgb(0x3A2A3A))
        val f = (w * frac.coerceIn(0.0, 1.0)).toInt().coerceAtLeast(if (frac > 0) 1 else 0)
        c.rect(x, y, f, 4, fill)
        c.rect(x, y, f, 1, lighten(fill, 0.35f))
        c.rect(x, y + 3, f, 1, darken(fill, 0.25f))
    }

    // ---------- персонажи ----------
    private fun drawActors(g: GameState, c: Canvas, zone: Int) {
        if (g.dead) {
            val gr = Sprites.GRAVE
            c.shadowE(HERO_X + 28, GROUND + 2, 15, 3)
            c.sprite(gr, HERO_X + 15, GROUND + 4 - gr.size, false, Sprites.gravePalette())
            return
        }
        val cls = g.heroClass.coerceIn(0, 4)
        val walking = g.phase == Phase.WALK
        val heroTint = if (g.flashHero > 0) rgb(0xFF6060) else 0
        val hy: Int
        val hx: Int
        val feetX: Int
        if (cls == 0) { // воин: нарисованные вручную кадры (WarriorData)
            val art: Array<String>
            var bob = 0
            if (g.attackAnim > 0f) {
                val p = ((Balance.ATTACK_ANIM - g.attackAnim) / Balance.ATTACK_ANIM).coerceIn(0f, 0.999f)
                art = WarriorData.ATTACK[(p * WarriorData.ATTACK.size).toInt()]
            } else if (walking) {
                art = WarriorData.VIEW_EAST
                bob = if (((g.anim * 8).toInt() % 2) == 1) 1 else 0
            } else art = WarriorData.ATTACK[0]
            hx = HERO_X - WarriorData.FEET_X + 28
            hy = GROUND + 4 - art.size - bob
            feetX = HERO_X + 28
            c.shadowE(feetX, GROUND + 2, 19, 4)
            c.sprite(art, hx, hy, false, Sprites.warriorPalette(heroTint))
        } else {
            val frames = Sprites.HEROES[cls]
            val fi = when {
                g.attackAnim > ATK_HALF -> Sprites.F_WINDUP
                g.attackAnim > 0f -> Sprites.F_STRIKE
                walking -> WALK_CYCLE[(g.anim * 8).toInt() % 4]
                else -> Sprites.F_IDLE
            }
            val heroArt = frames[fi]
            hx = HERO_X
            hy = GROUND + 4 - heroArt.size
            feetX = HERO_X + 24
            c.shadowE(feetX, GROUND + 2, 19, 4)
            c.sprite(heroArt, hx, hy, false, Sprites.heroPalette(cls, g.swordTier(), g.armorTier(), heroTint))
        }
        if (g.blessing > 0) { // золотое сияние вокруг героя
            for (k in 0 until 6) {
                val a = k * Math.PI / 3 + g.anim * 1.5
                val px = feetX + (Math.cos(a) * 22).toInt()
                val py = hy + 22 + (Math.sin(a) * 20).toInt()
                c.put(px, py, rgb(0xFFE08A)); c.put(px + 1, py, rgb(0xFFFFFF)); c.put(px, py + 1, rgb(0xFFB84A))
            }
        }
        if (g.shield > 0) {
            for (i in 0 until 20) {
                val a = i * Math.PI / 10 + g.anim * 2
                val px = feetX + (Math.cos(a) * 26).toInt()
                val py = hy + 24 + (Math.sin(a) * 25).toInt()
                c.put(px, py, rgb(0x6AF0FF)); c.put(px + 1, py, rgb(0xBFFBFF))
            }
        }
        bar(c, feetX - 14, hy - 5, 28, g.hp / g.maxHp(), rgb(0x4CE06A))

        val m = g.monster ?: return
        val art = Sprites.MONSTERS[m.type][(g.anim * 3).toInt() % 2]
        val mw = art[0].length
        val mh = art.size
        val mx = MON_X0 + (g.dist * 2f).toInt()
        val flyer = m.type == 1 || m.type == 7 || m.type == 10 || m.type == 15
        val hover = if (flyer) 9 + (sin(g.anim * 4.0) * 3).toInt() else 0
        val my = GROUND + 4 - mh - hover
        val tint = if (g.flashMon > 0) rgb(0xFFFFFF) else 0
        c.shadowE(mx + mw / 2, GROUND + 2, if (flyer) mw / 3 else mw / 2 - 2, if (flyer) 3 else 4, if (flyer) 0.22f else 0.34f)
        c.sprite(art, mx, my, true, Sprites.monsterPalette(m.type, tint, if (m.boss) zone % 5 else 0))
        // снаряды дальнего боя: шар мага и стрела лучника летят к монстру
        if (g.attackAnim in 0.0001f..ATK_HALF && (cls == 1 || cls == 2)) {
            val p = (ATK_HALF - g.attackAnim) / ATK_HALF
            val sx = HERO_X + 44
            val px = sx + ((mx + 8 - sx) * p).toInt()
            val py = GROUND - 20
            if (cls == 1) {
                circle(c, px, py, 4, rgb(0x4A7AD8)); circle(c, px, py, 3, rgb(0x8AF0FF)); circle(c, px, py, 1, rgb(0xFFFFFF))
                for (k in 1..3) c.put(px - 4 - k * 2, py + (k % 2), rgb(0x8AF0FF))
            } else {
                c.rect(px - 10, py, 12, 2, rgb(0xE8EEF5)); c.rect(px + 2, py - 1, 3, 4, rgb(0xFFFFFF))
                c.rect(px - 10, py - 1, 3, 4, rgb(0xFFD24A))
            }
        }
        if (m.boss) {
            val pulse = ((g.anim * 4).toInt() % 2) == 0
            c.rect(mx + mw / 2 - 2, my - 12, 5, 5, if (pulse) rgb(0xFF3030) else rgb(0x801818))
        }
        if (g.phase == Phase.FIGHT || g.dist < 40f) {
            val bw = if (m.boss) 44 else 30
            bar(c, mx + (mw - bw) / 2, my - 6, bw, m.hp / m.maxHp, rgb(0xFF5050))
        }
    }

    private fun drawHud(g: GameState, c: Canvas) {
        c.textS("LV" + g.level + " G" + Fmt.num(g.gold), 5, 4, rgb(0xFFFFFF), 2)
        val st = "ST" + g.stage + " " + (g.kills + 1) + "/" + Balance.KILLS_PER_STAGE
        c.textS(st, W - c.textWidthS(st, 2) - 5, 4, rgb(0xFFE08A), 2)
        val best = g.bestChest()
        if (best >= 0) {
            val x = 168
            val col = rgb(Rarity.COLORS[best])
            c.rect(x, 4, 18, 12, rgb(0x1B1B2F))
            c.rect(x + 2, 6, 14, 8, col)
            c.rect(x + 2, 6, 14, 2, lighten(col, 0.3f))
            c.rect(x + 8, 10, 3, 3, rgb(0xFFE08A))
            c.textS(g.chestTotal().toString(), x + 22, 4, col, 2)
        }
        for (p in g.popups) {
            c.textS(p.text, (p.x * 2f).toInt(), (p.y * 2f).toInt().coerceAtLeast(16), p.color, 2)
        }
    }

    private const val ATK_HALF = Balance.ATTACK_ANIM / 2
    private val WALK_CYCLE = intArrayOf(Sprites.F_WALK_A, Sprites.F_IDLE, Sprites.F_WALK_B, Sprites.F_IDLE)
}
