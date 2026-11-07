package me.joxquin.notivas.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationNavHandlerTest {

    @Test
    fun testNavigateToAndConsumeTarget() {
        val target = NotificationNavTarget.Assignment(
            assignmentId = 12345L,
            courseId = 67890L,
            courseName = "Programación Móvil"
        )

        NotificationNavHandler.navigateTo(target)
        assertEquals(target, NotificationNavHandler.pendingTarget.value)

        val consumed = NotificationNavHandler.consumePendingTarget()
        assertEquals(target, consumed)
        assertNull(NotificationNavHandler.pendingTarget.value)
        assertNull(NotificationNavHandler.consumePendingTarget())
    }

    @Test
    fun testForumTargetNavigation() {
        val forumTarget = NotificationNavTarget.Forum(
            topicId = 999L,
            courseId = 111L
        )

        NotificationNavHandler.navigateTo(forumTarget)
        assertEquals(forumTarget, NotificationNavHandler.pendingTarget.value)

        val consumed = NotificationNavHandler.consumePendingTarget()
        assertEquals(forumTarget, consumed)
        assertNull(NotificationNavHandler.pendingTarget.value)
    }

    @Test
    fun testRecentActivityTargetNavigation() {
        NotificationNavHandler.navigateTo(NotificationNavTarget.RecentActivity)
        assertEquals(NotificationNavTarget.RecentActivity, NotificationNavHandler.pendingTarget.value)

        val consumed = NotificationNavHandler.consumePendingTarget()
        assertEquals(NotificationNavTarget.RecentActivity, consumed)
        assertNull(NotificationNavHandler.pendingTarget.value)
    }
}
