/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight?>()

    private val _nights = database.getAllNights()

    val nights: LiveData<List<SleepNight>>
        get() = _nights

    val nightStrings = Transformations.map(_nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality= MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    val starButtonVisible= Transformations.map(tonight) {
        null == it
    }

    val stopButtonVisible= Transformations.map(tonight) {
        null != it
    }

    val clearButtonVisible= Transformations.map(_nights) {
        _nights.value?.isNotEmpty()
    }

    private val _showSnackBarEvent= MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackBarEvent

    private val _navigateToSleepDataQuality= MutableLiveData<Long>()
    val navigateToSleepDataQuality: LiveData<Long>
        get() = _navigateToSleepDataQuality

    fun onSleepNightClicked(nightId: Long) {
        _navigateToSleepDataQuality.value= nightId
    }

    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value= null
    }

    fun doneShowingSnackBar() {
        _showSnackBarEvent.value= false
    }

    fun doneNavigating() {
        _navigateToSleepQuality.value= null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()

            insert(newNight)

            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        return withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStopTracking() {
        uiScope.launch {

            val oldNight = tonight.value ?: return@launch

            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)

            _navigateToSleepQuality.value= oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        return withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null

            _showSnackBarEvent.value= true
        }
    }

    private suspend fun clear() {
        return withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

