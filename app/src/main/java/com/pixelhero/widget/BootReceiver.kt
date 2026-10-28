package com.pixelhero.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** После перезагрузки телефона снова запускает анимацию виджета. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Engine.get(context)
            HeroService.startSafely(context)
        }
    }
}
