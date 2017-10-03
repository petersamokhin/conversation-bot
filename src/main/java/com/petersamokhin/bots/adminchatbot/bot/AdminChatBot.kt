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
                Message()
                        .from(user)
                        .to(message.authorId())
                        .text("Привет! Бот работает только в беседах:")
                        .forwardedMessages(2151)
                        .send()
            }
        }

        // Handle commands on messages with forwarded
        user.onMessageWithFwds { message ->
            if (message.isMessageFromChat) {
                handleMessageWithFwds(message)
            } else {
                Message()
                        .from(user)
                        .to(message.authorId())
                        .text("Привет! Бот работает только в беседах:")
                        .forwardedMessages(2151)
                        .send()
            }
        }

        // Handle chat join
        user.onChatJoin { inviter, invited, _ ->
            handleJoin(invited, inviter)
        }

        // Handle chat leave
        user.onChatLeave { who, kicked, _ ->
            handleLeave(who, kicked)
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

        val commandText = words.subList(1, words.size).joinToString(" ")

        // Chat id
        val chat = message.chatIdLong

        // Sender and target
        val sender = message.authorId()
        val target = Utils.findUserInMessage(message.text)

        when {

        // Show list command
            words[0] == LIST.value -> {

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
            words[0] == KICK.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == BAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == WARN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == CLEAR_WARNINGS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == UNBAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == ADD_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == REMOVE_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                if (target < 1 || Utils.isUserInChat(target, chat)) {
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
            words[0] == SET_MAIN_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetMainTitle(commandText, sender, chat)
            }

        // Reset main title
            words[0] == RESET_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleResetTitle(chat)
            }

        // Reset title on it changes
            words[0] == AUTO_RESET_TITLE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleAutoResetTitle(sender, chat)
            }

        // Send help
            words[0] == HELP.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler
                        .helpMessage()
                        .to(chat)
                        .send()
            }

        // Save media
            words[0] == EXTRA.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                val hashtag = Utils.findHashTag(words)

                handler.handleSaveExtra(hashtag, chat, message)
            }

        // Save welcome message (when user invited or returned)
            words[0] == WELCOME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetWelcomeMessage(commandText, chat, message)
            }

        // Save goodbye message (when user kicked or leaved)
            words[0] == GOODBYE.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSaveGoodbyeMessage(commandText, chat, message)
            }

        // Start game (Why pidor? https://t.me/SublimeBot )
            words[0] == PIDOR.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorCommand(sender, chat)
            }

        // Join game
            words[0] == PIDOREG.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidoreg(sender, chat)
            }

        // Show game stats
            words[0] == PIDORSTATS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorStats(chat)
            }

        // Show user stats (is admin, banned; warnings, pidor count etc)
            words[0] == ME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handlePidorme(sender, chat)
            }

        // Chat superuser name (coz PIDOR! is censored )
            words[0] == PIDORNAME.value -> {

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

        // chat id
        val chat = message.chatIdLong

        // sender and target
        val sender = message.authorId()
        val target = message.forwardedMessages.getJSONObject(0).getInt("user_id")

        when {

        // Kick command
            words[0] == KICK.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleKick(sender, target, message.chatIdLong)
            }

        // Ban command
            words[0] == BAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleBan(sender, target, message.chatIdLong)
            }

        // Unban command
            words[0] == UNBAN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleUnban(sender, target, message.chatIdLong)
            }

        // Add admin command
            words[0] == ADD_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleAdmin(sender, target, message.chatIdLong)
            }

        // Remove admin commant
            words[0] == REMOVE_ADMIN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleRemoveAdmin(sender, target, message.chatIdLong)
            }

        // Warn user
            words[0] == WARN.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleWarn(sender, target, message.chatIdLong)
            }

        // Unwarn user
            words[0] == CLEAR_WARNINGS.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleClearWarnings(sender, target, message.chatIdLong)
            }

        // Save media
            words[0] == EXTRA.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSaveExtraFromForwarded(Utils.findHashTag(words), chat, message)
            }

        // Save welcome message
            words[0] == WELCOME.value -> {

                // Bot is typing...
                handler.sendTyping(chat)

                handler.handleSetWelcomeMessageFromFwds(chat, message)
            }

        // Save goodbye message
            words[0] == GOODBYE.value -> {

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
                        .text("Привет. Я -- крутой бот. Чтобы узнать, как мной пользоваться, напишите /help.")
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

                Message()
                        .from(user)
                        .to(chat)
                        .text(msg)
                        .attachments(attach)
                        .send()
            }
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