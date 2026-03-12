package com.booking.worktracker.ui.localization

interface Strings {
    // -- App / Window --
    val appTitle: String
    val dailyTracker: String

    // -- Navigation --
    val navHome: String
    val navHistory: String
    val navObjectives: String
    val navTimeTracking: String
    val navAnalytics: String
    val navExport: String
    val navSettings: String

    // -- Common actions --
    val add: String
    val cancel: String
    val delete: String
    val edit: String
    val close: String
    val save: String
    val update: String
    val create: String
    val start: String
    val stop: String
    val refresh: String
    val preview: String

    // -- Greetings --
    val goodMorning: String
    val goodAfternoon: String
    val goodEvening: String
    val readyToTrack: String
    fun dayStreak(count: Int): String

    // -- Calendar --
    val previousMonth: String
    val nextMonth: String
    val dayMon: String
    val dayTue: String
    val dayWed: String
    val dayThu: String
    val dayFri: String
    val daySat: String
    val daySun: String

    // -- Daily Log Screen --
    val startYourDay: String
    fun entriesCount(count: Int): String
    fun viewingDate(date: String): String
    val dailyLog: String
    val recordWorkEntries: String
    fun loggedCount(count: Int): String
    val objectives: String
    val checkYourGoals: String
    val quickTimer: String
    val startTrackingTime: String
    fun workEntriesCount(count: Int): String
    val addEntry: String
    val noEntriesYet: String
    val entryDeleted: String
    val entryAdded: String
    val tags: String
    val addNewTag: String
    val noTagsYet: String
    val deleteEntry: String

    // -- Add Entry Dialog --
    val addWorkEntry: String
    val whatDidYouAccomplish: String
    val entryPlaceholder: String

    // -- New Tag Dialog --
    val createNewTag: String
    val tagName: String
    val tagNamePlaceholder: String
    val colorLabel: String

    // -- History / Log List --
    val history: String
    val noLogsYet: String
    fun entryCount(count: Int): String
    fun andMoreEntries(count: Int): String
    val logDetail: String
    val workLog: String

    // -- Objectives Screen --
    val workObjectives: String
    val addObjective: String
    val yearly: String
    val quarterly: String
    val yearLabel: String
    val quarterLabel: String
    val previousYear: String
    val nextYear: String
    val previousQuarter: String
    val nextQuarter: String
    fun quarterYear(quarter: Int, year: Int): String
    val noObjectivesYet: String
    val inProgress: String
    val completed: String
    val cancelled: String
    val collapse: String
    val expand: String
    val addChecklistItem: String
    val editObjective: String
    fun yearDisplay(year: Int): String
    fun quarterDisplay(quarter: Int, year: Int): String
    val objectiveTitle: String
    val objectiveTitlePlaceholder: String
    val descriptionOptional: String
    val descriptionPlaceholder: String

    // -- Checklist --
    val addChecklistItemTitle: String
    val task: String
    val taskPlaceholder: String

    // -- Time Tracking Screen --
    val timeTracking: String
    val startTimer: String
    val stopTimer: String
    val addManual: String
    val status: String
    val timerRunning: String
    fun startedAt(time: String, category: String): String
    val todaysSummary: String
    fun totalTime(formatted: String): String
    val timeEntries: String
    val noTimeEntries: String
    fun timeRange(start: String, end: String?): String
    val whatAreYouWorkingOn: String
    val workingOnPlaceholder: String
    val category: String
    val addManualEntry: String
    val description: String
    val whatDidYouWorkOn: String
    val startTime: String
    val endTime: String
    val timeFormatPlaceholder: String

    // -- Analytics Screen --
    val analyticsAndInsights: String
    val noDataAvailable: String
    val streaks: String
    val currentStreak: String
    val bestStreak: String
    val daysLogged: String
    val overview: String
    val totalEntries: String
    val avgPerDay: String
    val mostActiveDay: String
    val total: String
    val checklistCompletion: String
    fun percentValue(value: Int): String
    val tagUsage: String
    fun timesUsed(count: Int): String
    val weeklyActivity: String
    fun activeDays(count: Int): String
    fun entriesCountLabel(count: Int): String
    val recentDailyActivity: String
    fun tagsCount(count: Int): String

    // -- Export Screen --
    val exportData: String
    val dateRange: String
    val startDate: String
    val endDate: String
    val dateFormatPlaceholder: String
    val include: String
    val workEntries: String
    val format: String
    val plainText: String
    val csv: String
    val markdown: String
    val exporting: String
    val exportToFile: String
    val exportDialogTitle: String

    // -- Settings Screen --
    val settings: String
    val reminderTimes: String
    val morningReminder: String
    val morningReminderDesc: String
    val afternoonReminder: String
    val afternoonReminderDesc: String
    val timeHHMM: String
    val saveSettings: String
    val settingsSaved: String
    val dangerZone: String
    val dangerZoneDesc: String
    val deleteAllData: String
    val aboutReminders: String
    val aboutRemindersMessage: String
    val deleteAllDataConfirmTitle: String
    val deleteAllDataConfirmMessage: String
    val deleteEverything: String
    val allDataDeleted: String
    val language: String

    // -- Notifications --
    val notificationMorningTitle: String
    val notificationMorningMessage: String
    val notificationAfternoonTitle: String
    val notificationAfternoonMessage: String

    // -- Error --
    fun errorMessage(message: String): String
}
