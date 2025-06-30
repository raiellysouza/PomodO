package com.example.pomodo.model

data class PomodoroTimer(
    var id: String? = null,
    var name: String = "",
    var focusMinutes: Int = 0,
    var shortBreakMinutes: Int = 0
) {
    constructor() : this(null, "", 0, 0)
}
