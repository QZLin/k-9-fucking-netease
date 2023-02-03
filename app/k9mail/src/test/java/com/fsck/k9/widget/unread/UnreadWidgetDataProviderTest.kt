package com.fsck.k9.widget.unread

import android.content.Context
import com.fsck.k9.Account
import com.fsck.k9.AppRobolectricTest
import com.fsck.k9.Preferences
import com.fsck.k9.controller.MessageCounts
import com.fsck.k9.controller.MessageCountsProvider
import com.fsck.k9.mailstore.Folder
import com.fsck.k9.mailstore.FolderRepository
import com.fsck.k9.mailstore.FolderType
import com.fsck.k9.search.SearchAccount
import com.fsck.k9.ui.folders.FolderNameFormatter
import com.fsck.k9.ui.folders.FolderNameFormatterFactory
import com.fsck.k9.ui.messagelist.DefaultFolderProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment

class UnreadWidgetDataProviderTest : AppRobolectricTest() {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val account = createAccount()
    private val preferences = createPreferences()
    private val messageCountsProvider = createMessageCountsProvider()
    private val defaultFolderStrategy = createDefaultFolderStrategy()
    private val folderRepository = createFolderRepository()
    private val folderNameFormatterFactory = createFolderNameFormatterFactory()
    private val provider = UnreadWidgetDataProvider(
        context,
        preferences,
        messageCountsProvider,
        defaultFolderStrategy,
        folderRepository,
        folderNameFormatterFactory
    )

    @Test
    fun unifiedInbox() {
        val configuration = UnreadWidgetConfiguration(
            appWidgetId = 1,
            accountUuid = SearchAccount.UNIFIED_INBOX,
            folderId = null
        )

        val widgetData = provider.loadUnreadWidgetData(configuration)

        with(widgetData!!) {
            assertThat(title).isEqualTo("Unified Inbox")
            assertThat(unreadCount).isEqualTo(SEARCH_ACCOUNT_UNREAD_COUNT)
        }
    }

    @Test
    fun regularAccount() {
        val configuration = UnreadWidgetConfiguration(
            appWidgetId = 3,
            accountUuid = ACCOUNT_UUID,
            folderId = null
        )

        val widgetData = provider.loadUnreadWidgetData(configuration)

        with(widgetData!!) {
            assertThat(title).isEqualTo(ACCOUNT_NAME)
            assertThat(unreadCount).isEqualTo(ACCOUNT_UNREAD_COUNT)
        }
    }

    @Test
    fun folder() {
        val configuration = UnreadWidgetConfiguration(appWidgetId = 4, accountUuid = ACCOUNT_UUID, folderId = FOLDER_ID)

        val widgetData = provider.loadUnreadWidgetData(configuration)

        with(widgetData!!) {
            assertThat(title).isEqualTo("$ACCOUNT_NAME - $LOCALIZED_FOLDER_NAME")
            assertThat(unreadCount).isEqualTo(FOLDER_UNREAD_COUNT)
        }
    }

    @Test
    fun nonExistentAccount_shouldReturnNull() {
        val configuration = UnreadWidgetConfiguration(appWidgetId = 3, accountUuid = "invalid", folderId = null)

        val widgetData = provider.loadUnreadWidgetData(configuration)

        assertThat(widgetData).isNull()
    }

    private fun createAccount(): Account = mock {
        on { uuid } doReturn ACCOUNT_UUID
        on { displayName } doReturn ACCOUNT_NAME
    }

    private fun createPreferences(): Preferences = mock {
        on { getAccount(ACCOUNT_UUID) } doReturn account
    }

    private fun createMessageCountsProvider() = object : MessageCountsProvider {
        override fun getMessageCounts(account: Account): MessageCounts {
            return MessageCounts(unread = ACCOUNT_UNREAD_COUNT, starred = 0)
        }

        override fun getMessageCounts(searchAccount: SearchAccount): MessageCounts {
            return MessageCounts(unread = SEARCH_ACCOUNT_UNREAD_COUNT, starred = 0)
        }

        override fun getUnreadMessageCount(account: Account, folderId: Long): Int {
            return FOLDER_UNREAD_COUNT
        }
    }

    private fun createDefaultFolderStrategy(): DefaultFolderProvider = mock {
        on { getDefaultFolder(account) } doReturn FOLDER_ID
    }

    private fun createFolderRepository(): FolderRepository {
        return mock {
            on { getFolder(account, FOLDER_ID) } doReturn FOLDER
        }
    }

    private fun createFolderNameFormatterFactory(): FolderNameFormatterFactory {
        val folderNameFormatter = createFolderNameFormatter()
        return mock {
            on { create(any()) } doReturn folderNameFormatter
        }
    }

    private fun createFolderNameFormatter(): FolderNameFormatter = mock {
        on { displayName(FOLDER) } doReturn LOCALIZED_FOLDER_NAME
    }

    companion object {
        const val ACCOUNT_UUID = "00000000-0000-0000-0000-000000000000"
        const val ACCOUNT_NAME = "Test account"
        const val FOLDER_ID = 23L
        const val SEARCH_ACCOUNT_UNREAD_COUNT = 1
        const val ACCOUNT_UNREAD_COUNT = 2
        const val FOLDER_UNREAD_COUNT = 3
        const val LOCALIZED_FOLDER_NAME = "Posteingang"
        val FOLDER = Folder(
            id = FOLDER_ID,
            name = "INBOX",
            type = FolderType.INBOX,
            isLocalOnly = false
        )
    }
}
