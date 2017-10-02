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
 */
class BotLauncher {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            startBot()
        }

        private fun startBot() {

            // Bot client
            val user = User(449557039, "efc6a9bb64936621b18fbbf719d9ebcfa0460456d9dd1f9d1cc24417d44f869a0ab7d7a55dd501b644332")

            // start bot
            AdminChatBot(user).start()
        }
    }
}