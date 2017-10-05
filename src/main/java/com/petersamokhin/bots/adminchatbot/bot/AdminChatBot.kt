package com.petersamokhin.bots.adminchatbot.bot

import com.petersamokhin.bots.adminchatbot.bot.Commands.*
import com.petersamokhin.bots.adminchatbot.bot.ListTypes.*
import com.petersamokhin.bots.adminchatbot.bot.Utils.Companion.db
import com.petersamokhin.bots.sdk.callbacks.Callback
import com.petersamokhin.bots.sdk.clients.User
import com.petersamokhin.bots.sdk.objects.Message

/**
 * Main bot logic
 */
class AdminChatBot(private val user: User) {

    private val handler: CommandsHandler

    init {
        Utils.user = this.user
        this.handler = CommandsHandler(this.user)
    }

    /**
     * MAIN
     */
    fun start() {

        // Handle commands on messages
        user.onMessage { message ->

            // Work only with chats
            if (message.isMessageFromChat) {
                handleMessage(message)
            } else {
                handleNonChatMessage(message)
            }
        }

        // Handle commands on messages with forwarded
        user.onMessageWithFwds { message ->
            if (message.isMessageFromChat) {
                handleMessageWithFwds(message)
            } else {
                handleNonChatMessage(message)
            }
        }

        // Handle chat join
        user.onChatJoin { _, invited, chat ->
            handleJoin(invited, chat)
        }

        // Handle chat leave
        user.onChatLeave { _, kicked, chat ->
            handleLeave(kicked, chat)
        }

        // Handle title change
        user.onChatTitleChanged { _, _, _, chat ->
            handleTitleChange(chat)
        }
    }

    /**
     * Logic:
     * Handle kick, ban, admin, lists, extra, pidor game, etc
     */
    private fun handleMessage(message: Message) {

        // Message words
        val words = message.text.trim().split(" ")

        // Command from message (forst word starts with '/')
        val command = words[0]

        // Message text (w/ command)
        val commandText = words.subList(1, words.size).joinToString(" ")

        // Chat id
        val chat = message.chatIdLong

        // Sender and target
        val sender = message.authorId()
        val target = Utils.findUserInMessage(message.text)

        when (command) {

        // Show list
            LIST.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                // Handle
                when {
                    words.contains("admins") -> {
                        handler.getList(ADMINS, chat, Callback { msg ->
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text(msg)
                                    .send()
                        })
                    }

                    words.contains("banned") -> {
                        handler.getList(BANNED, chat, Callback { msg ->
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text(msg)
                                    .send()
                        })
                    }

                    words.contains("extra") -> {
                        handler.getList(LIST_EXTRA, chat, Callback { msg ->
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text(msg)
                                    .send()
                        })
                    }
                }
            }

        // Kick command
            KICK.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleKick(sender, target, chat)
            }

        // Ban command
            BAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleBan(sender, target, chat)
            }

        // Warn command
            WARN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleWarn(sender, target, chat)
            }

        // Clear warns command
            CLEAR_WARNINGS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleClearWarnings(sender, target, chat)
            }

        // Unban command
            UNBAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleUnban(sender, target, chat)
            }

        // Add admin command
            ADD_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleAdmin(sender, target, chat)
            }

        // Remove admin command
            REMOVE_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || !Utils.isUserInChat(target, chat)) {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Пользователя нет в беседе, либо пользователь в сообщении не распознан: используйте либо упоминание, либо ссылку на пользователя, либо перешлите его сообщение.")
                            .send()
                    return
                }

                handler.handleRemoveAdmin(sender, target, chat)
            }

        // Set main title to reset it in future or set autoreset
            SET_MAIN_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetMainTitle(commandText, sender, chat)
            }

        // Reset main title
            RESET_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleResetTitle(chat)
            }

        // Reset title on it changes
            AUTO_RESET_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleAutoResetTitle(sender, chat)
            }

        // Send help
            HELP.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler
                        .helpMessage()
                        .to(chat)
                        .send()
            }

        // Save media
            EXTRA.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                val hashtag = Utils.findHashTag(words)

                handler.handleSaveExtra(hashtag, chat, message)
            }

        // Save welcome message (when user invited or returned)
            WELCOME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetWelcomeMessage(commandText, chat, message)
            }

        // Save goodbye message (when user kicked or leaved)
            GOODBYE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSaveGoodbyeMessage(commandText, chat, message)
            }

        // Start game (Why pidor? https://t.me/SublimeBot )
            PIDOR.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorCommand(sender, chat)
            }

        // Join game
            PIDOREG.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidoreg(sender, chat)
            }

        // Show game stats
            PIDORSTATS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorStats(chat)
            }

        // Show user stats (is admin, banned; warnings, pidor count etc)
            ME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorme(sender, chat)
            }

        // Chat superuser name (coz PIDOR! is censored )
            PIDORNAME.value -> {

                handler.sendTyping(chat)

                db.addString("pidor_name", commandText, chat)

                Message()
                        .from(user)
                        .to(chat)
                        .text("Теперь каждый день будет разыгрываться звание «$commandText» дня.")
                        .send()
            }

        // Handle message with no commands
            else -> {
                handleIfWithExtra(message)
            }
        }
    }

    /**
     * Logic:
     * Handle kick, ban, admin, extra, etc
     */
    private fun handleMessageWithFwds(message: Message) {

        // words of message
        val words = message.text.trim().split(" ")

        // Command from message (forst word starts with '/')
        val command = words[0]

        // chat id
        val chat = message.chatIdLong

        // sender and target
        val sender = message.authorId()
        val target = message.forwardedMessages.getJSONObject(0).getInt("user_id")

        when (command) {

        // Kick command
            KICK.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleKick(sender, target, message.chatIdLong)
            }

        // Ban command
            BAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleBan(sender, target, message.chatIdLong)
            }

        // Unban command
            UNBAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleUnban(sender, target, message.chatIdLong)
            }

        // Add admin command
            ADD_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleAdmin(sender, target, message.chatIdLong)
            }

        // Remove admin commant
            REMOVE_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleRemoveAdmin(sender, target, message.chatIdLong)
            }

        // Warn user
            WARN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleWarn(sender, target, message.chatIdLong)
            }

        // Unwarn user
            CLEAR_WARNINGS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleClearWarnings(sender, target, message.chatIdLong)
            }

        // Save media
            EXTRA.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSaveExtraFromForwarded(Utils.findHashTag(words), chat, message)
            }

        // Save welcome message
            WELCOME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetWelcomeMessageFromFwds(chat, message)
            }

        // Save goodbye message
            GOODBYE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSaveGoodbyeMessageFromFwds(chat, message)
            }

        // Else
            else -> {
                handleIfWithExtra(message)
            }
        }
    }

    /**
     * Logic: auto-add and create chat
     */
    private fun handleNonChatMessage(message: Message) {

        // Message words
        val words = message.text.trim().split(" ")

        // Command from message (forst word starts with '/')
        val command = words[0]

        // Message text without command
        //val commandText = words.subList(1, words.size).joinToString(" ")

        // User
        val sender = message.authorId()

        when (command) {

        // Command that will starts bot
            START_BOT.value -> {

                // Bot is typing...
                handler.sendTyping(sender)

                handler.handleStartBot(sender)
            }

        // Command that will add user and creating chat
            CREATE_CHAT.value -> {

                // Bot is typing...
                handler.sendTyping(sender)

                handler.handleCreateChat(sender)
            }

            else -> {

                // Bot is typing...
                handler.sendTyping(sender)

                handler.handleNonChat(sender)
            }
        }
    }

    /**
     * Logic:
     * Handle banned
     */
    private fun handleJoin(invited: Int, chat: Int) {

        if (!db.isUserInList(invited, "banned", chat)) {
            if (invited != user.id) {

                Utils.getUserName(arrayOf(invited), Callback { users ->

                    var msg = Utils.db.getString("welcome_text", chat)
                    val attach = Utils.db.getString("welcome_attach", chat)

                    if (msg.contains("\${username}")) {
                        msg = msg.replace("\${username}", "[id$invited|${users[0]}]")
                    }

                    Message()
                            .from(user)
                            .to(chat)
                            .text(msg)
                            .attachments(attach)
                            .send()
                })
            } else {
                Message()
                        .from(user)
                        .to(chat)
                        .text("Привет. Я -- крутой бот. Чтобы узнать, как мной пользоваться, напишите /help. К сожалению, если вы меня пригласили, и я не являюсь создателем чата, то я не смогу администрировать беседу (кикать/банить и так далее).")
                        .send()
            }
        } else {
            user.chat(chat).kickUser(invited, Callback<Any> {})
        }
    }

    /**
     * Logic:
     * simple
     */
    private fun handleLeave(kicked: Int, chat: Int) {

        var msg = db.getString("goodbye_text", chat)
        val attach = db.getString("goodbye_attach", chat)

        Utils.getUserName(arrayOf(kicked), Callback { users ->

            if (msg.contains("\${username}")) {
                msg = msg.replace("\${username}", "[id$kicked|${users[0]}]")
            }

            Message()
                    .from(user)
                    .to(chat)
                    .text(msg)
                    .attachments(attach)
                    .send()
        })

    }

    /**
     * Logic:
     * Autoreset title if on
     */
    private fun handleTitleChange(chat: Int) {

        if (Utils.isChatTitleAutoresetOn(chat)) {
            user.chat(chat).editTitle(db.getString("main_title", chat), Callback<Any> { response ->
                if (response.toString() != "1") {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Не удалось вернуть имя беседы.")
                            .send()
                }
            })
        }
    }

    /**
     * Logic:
     * butler
     */
    private fun handleIfWithExtra(message: Message) {

        val extraList = db.getStringsList(message.chatIdLong, "extra", "key")

        for (extra in extraList) {
            if (message.text.contains(extra)) {
                Message()
                        .from(user)
                        .to(message.chatIdLong)
                        .forwardedMessages(message.messageId)
                        .attachments(db.getString(extra, message.chatIdLong, "extra"))
                        .send()
                break
            }
        }
    }
}