package me.joxquin.notivas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.UpdateInfo
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.UpdateRepository
import me.joxquin.notivas.di.AppModule

class MainShellViewModel(
    private val canvasRepository: CanvasRepository = AppModule.canvasRepository,
    private val updateRepository: UpdateRepository = UpdateRepository()
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val updateCheck = viewModelScope.launch {
        _updateInfo.value = updateRepository.checkForUpdates().takeIf { it.isUpdateAvailable }
    }

    fun cancelUpdateCheck() {
        updateCheck.cancel()
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

    suspend fun resolveAssignment(assignmentId: Long): Assignment? =
        canvasRepository.getAssignment(assignmentId)

    suspend fun resolveDiscussionTopic(courseId: Long, topicId: Long): CanvasDiscussionTopic? =
        canvasRepository.getDiscussionTopic(courseId, topicId)

    suspend fun resolveCourse(courseId: Long): Course? =
        canvasRepository.getCourse(courseId)
}
