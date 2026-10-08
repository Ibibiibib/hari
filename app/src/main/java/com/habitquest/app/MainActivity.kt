package com.habitquest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.habitquest.app.data.AppDatabase
import com.habitquest.app.data.HabitRepository
import com.habitquest.app.notify.ReminderScheduler
import com.habitquest.app.ui.AppRoot
import com.habitquest.app.ui.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = HabitRepository(AppDatabase.get(this))
        val vm = MainViewModel(application, repo)

        ReminderScheduler.schedule(this)

        if (Build.VERSION.SDK_INT >= 33) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent { AppRoot(vm) }
    }
}
