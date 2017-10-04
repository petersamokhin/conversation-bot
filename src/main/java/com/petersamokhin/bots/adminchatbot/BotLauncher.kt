package com.petersamokhin.bots.adminchatbot

import com.petersamokhin.bots.adminchatbot.bot.AdminChatBot
import com.petersamokhin.bots.sdk.clients.User

/**
 * Conversation Bot
 * Can administer and entertain vegetables (your friend) in VK conversations
 * Contains "Group Butler" and "Sublime Bot" from Telegram
 *
 * @author https://petersamokhin.com/
 *
 * Used:
 * VK Bot Java SDK 0.1.4-alpha
 * Kotlin 1.1.51
 * MongoDB
 */
class BotLauncher {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            startBot()
        }

        private fun startBot() {

            // Bot client - 'Conversation Bot'
            val user = User(449557039, "access_token")

            // start bot
            AdminChatBot(user).start()
        }
    }
}
