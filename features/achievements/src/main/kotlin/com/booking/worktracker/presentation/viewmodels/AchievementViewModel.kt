package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.AchievementCategory
import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.data.models.XpActionType
import com.booking.worktracker.domain.usecases.AwardXpUseCase
import com.booking.worktracker.domain.usecases.CheckAndUnlockAchievementsUseCase
import com.booking.worktracker.domain.usecases.GetAchievementsUseCase
import com.booking.worktracker.domain.usecases.GetUserLevelUseCase
import com.booking.worktracker.domain.usecases.GetWeeklyXpSummaryUseCase
import com.booking.worktracker.domain.usecases.SeedAchievementsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

data class DailyXpTotal(
    val date: String,
    val totalXp: Long
)

@Inject
class AchievementViewModel(
    private val getUserLevelUseCase: GetUserLevelUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getWeeklyXpSummaryUseCase: GetWeeklyXpSummaryUseCase,
    private val awardXpUseCase: AwardXpUseCase,
    private val checkAndUnlockAchievementsUseCase: CheckAndUnlockAchievementsUseCase,
    private val seedAchievementsUseCase: SeedAchievementsUseCase
) : ViewModel() {

    private val _userLevel = MutableStateFlow<UserLevel?>(null)
    val userLevel: StateFlow<UserLevel?> = _userLevel.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _weeklyXpData = MutableStateFlow<List<DailyXpTotal>>(emptyList())
    val weeklyXpData: StateFlow<List<DailyXpTotal>> = _weeklyXpData.asStateFlow()

    private val _selectedCategory = MutableStateFlow<AchievementCategory?>(null)
    val selectedCategory: StateFlow<AchievementCategory?> = _selectedCategory.asStateFlow()

    private val _recentUnlocks = MutableStateFlow<List<Achievement>>(emptyList())
    val recentUnlocks: StateFlow<List<Achievement>> = _recentUnlocks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            seedAchievementsUseCase()
            loadUserLevel()
            loadAchievements(null)
            loadWeeklyXp()
            _isLoading.value = false
        }
    }

    fun loadUserLevel() {
        getUserLevelUseCase().onSuccess { level ->
            _userLevel.value = level
        }
    }

    fun loadAchievements(category: AchievementCategory?) {
        getAchievementsUseCase(category).onSuccess { list ->
            _achievements.value = list
        }
    }

    fun loadWeeklyXp() {
        getWeeklyXpSummaryUseCase().onSuccess { data ->
            _weeklyXpData.value = data.map { (date, xp) ->
                DailyXpTotal(date = date, totalXp = xp)
            }
        }
    }

    fun awardXp(actionType: XpActionType, description: String? = null) {
        viewModelScope.launch {
            awardXpUseCase(actionType, description).onSuccess { level ->
                _userLevel.value = level
            }
            checkAndUnlockAchievementsUseCase().onSuccess { newlyUnlocked ->
                if (newlyUnlocked.isNotEmpty()) {
                    _recentUnlocks.value = newlyUnlocked
                }
            }
            loadAchievements(_selectedCategory.value)
            loadWeeklyXp()
        }
    }

    fun filterByCategory(category: AchievementCategory?) {
        _selectedCategory.value = category
        loadAchievements(category)
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            loadUserLevel()
            loadAchievements(_selectedCategory.value)
            loadWeeklyXp()
            _isLoading.value = false
        }
    }

    fun clearRecentUnlocks() {
        _recentUnlocks.value = emptyList()
    }
}
