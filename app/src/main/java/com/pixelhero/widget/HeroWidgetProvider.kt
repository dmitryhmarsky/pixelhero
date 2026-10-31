package com.pixelhero.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.pixelhero.widget.core.Scene

class HeroWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        pickScale(context, manager, ids)
        Engine.tick(context)
        push(context, manager, ids, full = true)
        // Если систему не пускает стартовать сервис из фона, анимация включится при открытии приложения.
        HeroService.startSafely(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        val ids = allIds(context, manager)
        pickScale(context, manager, ids)
        push(context, manager, ids, full = true)
    }

    override fun onEnabled(context: Context) {
        HeroService.startSafely(context)
    }

    override fun onDisabled(context: Context) {
        context.stopService(Intent(context, HeroService::class.java))
        Engine.save(context)
    }

    companion object {
        fun allIds(context: Context, manager: AppWidgetManager): IntArray =
            manager.getAppWidgetIds(ComponentName(context, HeroWidgetProvider::class.java))

        /** Подбирает целый масштаб пикселей так, чтобы картинка почти не растягивалась лаунчером. */
        fun pickScale(context: Context, manager: AppWidgetManager, ids: IntArray) {
            if (ids.isEmpty()) return
            val density = context.resources.displayMetrics.density
            var best = 8
            for (id in ids) {
                val o = manager.getAppWidgetOptions(id)
                val wPx = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) * density
                val hPx = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40) * density
                val s = minOf(wPx / Scene.W, hPx / Scene.H).toInt()
                if (s < best) best = s
            }
            Engine.scale = best.coerceIn(2, 4)
        }

        /** Обновляет все виджеты. full = true ставит и обработчик нажатия, иначе меняется только картинка. */
        fun update(context: Context, full: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = allIds(context, manager)
            if (ids.isEmpty()) return
            push(context, manager, ids, full)
        }

        private fun push(context: Context, manager: AppWidgetManager, ids: IntArray, full: Boolean) {
            if (ids.isEmpty()) return
            val rv = RemoteViews(context.packageName, R.layout.hero_widget)
            rv.setImageViewBitmap(R.id.img, Engine.renderBitmap(context))
            if (full) {
                val open = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                rv.setOnClickPendingIntent(R.id.root, open)
                manager.updateAppWidget(ids, rv)
            } else {
                manager.partiallyUpdateAppWidget(ids, rv)
            }
        }
    }
}
