package com.pixelhero.widget

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper

/**
 * Точка подключения рекламы. Игра знает только этот интерфейс.
 * [onReward] вызывается ТОЛЬКО если пользователь досмотрел ролик до награды; [onFail] во всех остальных случаях.
 */
interface AdProvider {
    fun showRewarded(activity: Activity, onReward: () -> Unit, onFail: (String) -> Unit)
}

/** Заглушка для разработки: «ролик» на 5 секунд в диалоге. Замените на AdMobAdProvider (см. docs/). */
class FakeAdProvider : AdProvider {
    override fun showRewarded(activity: Activity, onReward: () -> Unit, onFail: (String) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var left = 5
        var finished = false
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Тестовая реклама")
            .setMessage("Награда через $left с…")
            .setCancelable(false)
            .setNegativeButton("Закрыть") { _, _ -> }
            .create()
        val tick = object : Runnable {
            override fun run() {
                if (finished) return
                left--
                if (left <= 0) {
                    finished = true
                    dialog.dismiss()
                    onReward()
                } else {
                    dialog.setMessage("Награда через $left с…")
                    handler.postDelayed(this, 1000)
                }
            }
        }
        dialog.setOnDismissListener {
            if (!finished) {
                finished = true
                handler.removeCallbacks(tick)
                onFail("Ролик закрыт раньше времени")
            }
        }
        dialog.show()
        handler.postDelayed(tick, 1000)
    }
}

object Ads {
    @JvmField var provider: AdProvider = FakeAdProvider()
}
