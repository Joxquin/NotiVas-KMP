package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasDiscussionTopic

sealed class NewContentItem {
    abstract val id: Long
    abstract val courseId: Long?
    abstract val title: String

    data class PublishedAssignment(
        override val id: Long,
        override val courseId: Long?,
        override val title: String
    ) : NewContentItem()

    data class OpenQuiz(
        override val id: Long,
        override val courseId: Long?,
        override val title: String
    ) : NewContentItem()

    data class AcademicForum(
        override val id: Long,
        override val courseId: Long?,
        override val title: String
    ) : NewContentItem()
}

data class SyncContentDiff(
    val newAssignments: List<NewContentItem.PublishedAssignment> = emptyList(),
    val newQuizzes: List<NewContentItem.OpenQuiz> = emptyList(),
    val newForos: List<NewContentItem.AcademicForum> = emptyList(),
    val allAssignmentIds: Set<Long> = emptySet(),
    val allForumIds: Set<Long> = emptySet()
)

object SyncDiffDetector {
    fun detectChanges(
        currentAssignments: List<Assignment>,
        currentForums: List<CanvasDiscussionTopic>,
        knownAssignmentIds: Set<Long>,
        knownForumIds: Set<Long>,
        hasCompletedInitialSync: Boolean,
        filterNewAssignments: Boolean,
        filterNewQuizzes: Boolean,
        filterNewForos: Boolean
    ): SyncContentDiff {
        val allAssignmentIds = currentAssignments.map { it.id }.toSet()
        val allForumIds = currentForums.map { it.id }.toSet()

        if (!hasCompletedInitialSync) {
            // Primera sincronización: solo registrar conjuntos sin alertar retroactivamente
            return SyncContentDiff(
                newAssignments = emptyList(),
                newQuizzes = emptyList(),
                newForos = emptyList(),
                allAssignmentIds = allAssignmentIds,
                allForumIds = allForumIds
            )
        }

        val newAssignments = mutableListOf<NewContentItem.PublishedAssignment>()
        val newQuizzes = mutableListOf<NewContentItem.OpenQuiz>()

        // Si no habia asignaciones previamente conocidas, se inicializa el estado sin emitir alertas masivas
        if (knownAssignmentIds.isNotEmpty()) {
            currentAssignments.forEach { assignment ->
                if (!knownAssignmentIds.contains(assignment.id)) {
                    if (assignment.isQuiz) {
                        if (filterNewQuizzes) {
                            newQuizzes.add(
                                NewContentItem.OpenQuiz(
                                    id = assignment.id,
                                    courseId = assignment.courseId,
                                    title = assignment.name
                                )
                            )
                        }
                    } else {
                        if (filterNewAssignments) {
                            newAssignments.add(
                                NewContentItem.PublishedAssignment(
                                    id = assignment.id,
                                    courseId = assignment.courseId,
                                    title = assignment.name
                                )
                            )
                        }
                    }
                }
            }
        }

        val newForos = mutableListOf<NewContentItem.AcademicForum>()
        // Si no habia foros previamente conocidos, se inicializa el estado sin emitir alertas masivas
        if (filterNewForos && knownForumIds.isNotEmpty()) {
            currentForums.forEach { forum ->
                if (!knownForumIds.contains(forum.id)) {
                    newForos.add(
                        NewContentItem.AcademicForum(
                            id = forum.id,
                            courseId = forum.courseId,
                            title = forum.title
                        )
                    )
                }
            }
        }

        return SyncContentDiff(
            newAssignments = newAssignments,
            newQuizzes = newQuizzes,
            newForos = newForos,
            allAssignmentIds = allAssignmentIds,
            allForumIds = allForumIds
        )
    }
}

