package com.pixelhero.widget

import android.content.Context
import android.graphics.Bitmap
import com.pixelhero.widget.core.Canvas
import com.pixelhero.widget.core.GameState
import com.pixelhero.widget.core.Scene

/**
 * Единое состояние игры на весь процесс. Им пользуются и сервис (анимация виджета), и Activity.
 * advanceTo идемпотентен: сколько бы раз и откуда его ни вызвали, время не посчитается дважды.
 */
object Engine {
    private const val PREFS = "hero"
    private const val KEY = "state"

    @Volatile
    private var state: GameState? = null

    /** Целый коэффициент увеличения пикселей (подбирается под размер виджета). */
    @Volatile
    var scale: Int = 5

    /** Кадров в секунду анимации виджета (5, 10 или 15). Выше плавнее, но больше расход батареи. */
    @Volatile
    var fps: Int = 10
        private set

    fun setFps(ctx: Context, v: Int) {
        fps = v.coerceIn(2, 20)
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt("fps", fps).apply()
    }

    private val canvas = Canvas(Scene.W, Scene.H)

    @Synchronized
    fun get(ctx: Context): GameState {
        state?.let { return it }
        val prefs = ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        fps = prefs.getInt("fps", 10).coerceIn(2, 20)
        val seed = System.nanoTime()
        val loaded = GameState.deserialize(prefs.getString(KEY, null), seed) ?: GameState(seed)
        state = loaded
        return loaded
    }

    /** Продвигает игру до текущего времени (офлайн-прогресс включается сам, если прошло больше 2 секунд). */
    @Synchronized
    fun tick(ctx: Context) {
        get(ctx).advanceTo(System.currentTimeMillis())
    }

    @Synchronized
    fun save(ctx: Context) {
        val g = get(ctx)
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, g.serialize()).apply()
    }

    @Synchronized
    fun reset(ctx: Context) {
        val g = GameState(System.nanoTime())
        g.lastMs = System.currentTimeMillis()
        state = g
        save(ctx)
    }

    /** Рисует текущий кадр и возвращает Bitmap (Scene.W*scale x Scene.H*scale). */
    @Synchronized
    fun renderBitmap(ctx: Context): Bitmap {
        val c = canvas
        Scene.render(get(ctx), c)
        val s = scale
        val px = c.scaled(s)
        return Bitmap.createBitmap(px, Scene.W * s, Scene.H * s, Bitmap.Config.ARGB_8888)
    }
}
