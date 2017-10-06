package com.petersamokhin.bots.adminchatbot.bot

import com.petersamokhin.bots.sdk.callbacks.Callback
import com.petersamokhin.bots.sdk.clients.User
import com.petersamokhin.bots.sdk.objects.Chat
import com.petersamokhin.bots.sdk.objects.Message
import com.petersamokhin.bots.sdk.utils.vkapi.docs.DocTypes
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Handler of commands
 */
class CommandsHandler(private val user: User) {

    /* HANDLE COMMANDS */

    fun helpMessage(): Message {
        return Message()
                .from(user)
                .text("Как пользоваться ботом: ")
                .attachments("photo449557039_456239052")
    }

    fun handleKick(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            user.chat(chat).kickUser(target, Callback<Any> { response ->

                if (response.toString() == "1") {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Теперь [id$target|пользователь исключен] и сможет вернуться в беседу только по приглашению.")
                            .send()
                } else {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Не удалось исключить [id$target|пользователя]!")
                            .send()
                }
            })
        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] тоже администратор и [id$sender|ты] попытался его кикнуть.")
                    .send()
        }
    }

    fun getList(type: ListTypes, chat: Int, callback: Callback<String>) {

        when (type) {
            ListTypes.ADMINS -> {
                var msg = "Список администраторов:<br>"
                val list = Utils.db.getList(ListTypes.ADMINS, chat)
                Utils.getUserName(list.toTypedArray(), Callback { users ->
                    for ((index, item) in list.withIndex()) {
                        val itemAbs = Math.abs(item)
                        msg += "${index + 1}. [${if (item > 0) "id" else "club"}$itemAbs|${users[index]}]<br>"
                    }

                    callback.onResult(msg)
                })
            }
            ListTypes.BANNED -> {
                var msg = "Список заблокированных:<br>"
                val list = Utils.db.getList(ListTypes.BANNED, chat)
                Utils.getUserName(list.toTypedArray(), Callback { users ->
                    for ((index, item) in list.withIndex()) {
                        val itemAbs = Math.abs(item)
                        msg += "${index + 1}. [${if (item > 0) "id" else "club"}$itemAbs|${users[index]}]<br>"
                    }

                    callback.onResult(msg)
                })
            }
            ListTypes.LIST_EXTRA -> {
                var msg = "Список медиа:<br>"
                val list = Utils.db.getStringsList(chat, "extra", "key")
                for ((index, item) in list.withIndex()) {
                    msg += "${index + 1}. $item<br>"
                }

                callback.onResult(msg)
            }
            else -> {
            }
        }
    }

    fun handleBan(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat) && !Utils.db.isUserInList(target, "banned", chat)) {

            Utils.db.addUserToList(target, "banned", chat)

            user.chat(chat).kickUser(target, Callback<Any> {})

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пользователь] теперь забанен.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] тоже администратор и [id$sender|ты] попытался его забанить.")
                    .send()
        }
    }

    fun handleUnban(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.removeUserFromList(target, "banned", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пользователь] теперь разбанен, если он был забанен.")
                    .send()
        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] тоже администратор и [id$sender|ты] попытался его разбанить?")
                    .send()
        }
    }

    fun handleWarn(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.incrementCounter(target, chat, "warnings")

            val wCount = Utils.db.getCounter(target, chat, "warnings")

            when {
                wCount < 5 -> {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("[id$target|Пользователь] получил предупреждение. <br>Теперь у него $wCount предупреждений. Ещё ${5 - wCount} и он будет исключен из беседы.")
                            .send()
                }
                else -> {

                    handleKick(sender, target, chat)
                    Message()
                            .from(user)
                            .to(chat)
                            .text("[id$target|Пользователь] получил слишком много предупреждений и был исключен.")
                            .send()
                }
            }
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] тоже администратор и [id$sender|ты] попытался сделать ему предупреждение?")
                    .send()
        }
    }

    fun handleClearWarnings(sender: Int, target: Int, chat: Int) {
        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.clearCounter(target, chat, "warnings")

            Message()
                    .from(user)
                    .to(chat)
                    .text("Предупреждения [id$target|пользователя] очищены.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] тоже администратор и [id$sender|ты] попытался очистить его предупреждения.")
                    .send()
        }
    }

    fun handleAdmin(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.addUserToList(target, "admin", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пользователь] назначен администратором беседы, теперь он может банить, кикать, делать предупреждения, управлять именем беседы.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] уже администратор и [id$sender|ты] попытался сделать его администратором.")
                    .send()
        }
    }

    fun handleRemoveAdmin(sender: Int, target: Int, chat: Int) {
        if (Utils.goodUser(sender, chat) && Utils.goodUser(target, chat)) {

            Utils.db.removeUserFromList(target, "admin", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пользователь] больше не администратор.")
                    .send()

        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Я выполняю распоряжения только от администраторов. Хотя, возможно, [id$target|другой пользователь] уже не администратор и [id$sender|ты] попытался его разжаловать.")
                    .send()
        }
    }

    private fun setTitle(title: String, chat: Int) {
        user.chat(chat).editTitle(title, Callback<Any> {})
    }

    fun sendTyping(chat: Int) {
        user.api().call("messages.setActivity", "{type:'typing',peer_id:$chat}", {})
    }

    fun handlePidorCommand(sender: Int, chat: Int) {

        val isPidor = Utils.db.isUserInList(sender, "is_pidor", chat)

        var everyDayUserName = Utils.db.getString("pidor_name", chat)

        if (everyDayUserName.isEmpty()) {
            everyDayUserName = "хороший человек"
        }

        val pidorMap = Utils.db.getPidorStats(chat)

        if (pidorMap.keys.size < 2) {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Сейчас в игре всего один игрок и в ней нет смысла. Нужно больше зарегистрированных: пишите ${Commands.PIDOREG.value}")
                    .send()
            return
        }

        when {

            isPidor -> {
                val sdf = SimpleDateFormat("dd.MM.yyyy")

                var lastDateString = Utils.db.getString("last_pidor_date", chat)

                if (lastDateString.length < 5) {
                    lastDateString = "01.01.1980"
                }

                val currentDate = sdf.parse(sdf.format(Date()))
                val lastDate = sdf.parse(lastDateString)

                val currentDateString = sdf.format(currentDate)

                val newDate = lastDate.before(currentDate)

                if (newDate) {

                    Utils.db.addString("last_pidor_date", currentDateString, chat)

                    val list = Utils.db.getList(ListTypes.LIST_PIDOR, chat)

                    if (list.size > 0) {

                        /* ИЩЕМ ПИДОРА */

                        val rand = Random().nextInt(list.size + 1)
                        val pidorId = list[rand]

                        // пишем в бд
                        Utils.db.incrementCounter(pidorId, chat, "pidor")
                        Utils.db.addString("today_pidor", "$pidorId", chat)

                        handleSendPidor(pidorId, chat)

                    } else {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("В игре пока никто не зарегистрировался! Будь первым: ${Commands.PIDOREG.value}")
                                .send()
                    }

                } else {

                    try {
                        val pidorId = Utils.db.getString("today_pidor", chat).toInt()

                        Utils.getUserName(arrayOf(pidorId), Callback { users ->
                            if (users.size > 0) {
                                Message()
                                        .from(user)
                                        .to(chat)
                                        .text("Сегодня $everyDayUserName дня -- [id$pidorId|${users[0]}]")
                                        .send()
                            }
                        })

                    } catch (e: Exception) {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("Сегодня $everyDayUserName дня -- никто. Ошибка? Может быть..")
                                .send()
                    }

                }
            }

            else -> {
                Utils.getUserName(arrayOf(sender), Callback { users ->
                    if (users.size > 0) {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("[id$sender|${users[0]}], для начала зарегистрируйся в игре: ${Commands.PIDOREG.value}")
                                .send()
                    }
                })
            }
        }
    }

    private fun handleSendPidor(pidorId: Int, chat: Int) {

        var everyDayUserName = Utils.db.getString("pidor_name", chat)

        if (everyDayUserName.isEmpty()) {
            everyDayUserName = "хороший человек"
        }

        Utils.getUserName(arrayOf(pidorId), Callback { users ->
            val firstMessageText = "Так-с так-с... кто тут у нас сегодня $everyDayUserName дня?"
            val secondMessageText = "... придумать больше сообщений ..."
            val thirdMessageText = "... придумать больше сообщений ..."
            val finalMessageText = "Сегодня $everyDayUserName дня -- [id$pidorId|${users[0]}]"

            // Формируем сообщения
            val firstMessage = Message()
                    .from(user)
                    .to(chat)
                    .text(firstMessageText)

            val secondMessage = Message()
                    .from(user)
                    .to(chat)
                    .text(secondMessageText)

            val thirdMessage = Message()
                    .from(user)
                    .to(chat)
                    .text(thirdMessageText)

            val finalMessage = Message()
                    .from(user)
                    .to(chat)
                    .text(finalMessageText)

            val timer = Timer()

            // Отправляем
            firstMessage.send()

            timer.schedule(timerTask {
                secondMessage.send()
            }, 1000L)

            timer.schedule(timerTask {
                thirdMessage.send()
            }, 2000L)

            timer.schedule(timerTask {
                finalMessage.send()
            }, 3000L)
        })
    }

    fun handlePidoreg(sender: Int, chat: Int) {

        val isPidor = Utils.db.isUserInList(sender, "is_pidor", chat)

        Utils.getUserName(arrayOf(sender), Callback { users ->

            if (users.size > 0) {
                when {
                    isPidor -> {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("[id$sender|${users[0]}], ты уже в игре.")
                                .send()
                    }

                    else -> {
                        Utils.db.addUserToList(sender, "is_pidor", chat)

                        Message()
                                .from(user)
                                .to(chat)
                                .text("[id$sender|${users[0]}], теперь ты зарегистрирован в игре!")
                                .send()
                    }
                }
            }
        })
    }

    fun handlePidorStats(chat: Int) {

        var everyDayUserName = Utils.db.getString("pidor_name", chat)

        if (everyDayUserName.isEmpty()) {
            everyDayUserName = "хороший человек"
        }

        val pidorMap = Utils.db.getPidorStats(chat).toList().sortedBy { (_, value) -> -value }.toMap()

        Utils.getUserName(pidorMap.keys.toList().toTypedArray(), Callback { users ->

            var msg = "Статистика игры «$everyDayUserName»:<br><br>"

            var index = 1

            for (pidorId in pidorMap.keys) {
                if (pidorMap.containsKey(pidorId) && pidorMap[pidorId] != null && pidorMap[pidorId]!! > 0) {
                    msg += "$index. [id$pidorId|${users[index - 1]}] -- ${pidorMap[pidorId]!!} раз<br>"
                    index++
                }

                if (index > 10) break
            }

            msg += "<br><br>Всего участников игры: ${pidorMap.keys.size}"

            Message()
                    .from(user)
                    .to(chat)
                    .text(msg)
                    .send()
        })
    }

    fun handlePidorme(sender: Int, chat: Int) {

        val user = Utils.db.getUser(sender, chat)

        var everyDayUserName = Utils.db.getString("pidor_name", chat)

        if (everyDayUserName.isEmpty()) {
            everyDayUserName = "хороший человек"
        }

        Utils.getUserName(arrayOf(sender), Callback { users ->

            val msg = if (user != null) {
                val pidor = user.getInteger("pidor")
                val warns = user.getInteger("warnings")
                val admin = user.getBoolean("admin")
                val banned = user.getBoolean("banned")

                "[id$sender|${users[0]}], ты ${if (admin || sender == Utils.superAdmin) "" else "не"} админ, ${if (banned) "" else "не"} забанен, у тебя $warns предупреждений и ты носил почётное звание «$everyDayUserName» $pidor раз."
            } else {
                "[id$sender|${users[0]}], ты не админ, не забанен, у тебя 0 предупреждений и ты носил почётное звание «$everyDayUserName» 0 раз."
            }

            Message()
                    .from(this.user)
                    .to(chat)
                    .text(msg)
                    .send()
        })
    }

    fun handleSetMainTitle(title: String, sender: Int, chat: Int) {

        if (Utils.goodUser(sender, chat)) {

            Utils.db.addString("main_title", title, chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("Установлено главное имя беседы: «$title»")
                    .send()
        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Только администратор может управлять именем беседы.")
                    .send()
        }
    }

    fun handleResetTitle(chat: Int) {

        setTitle(Utils.db.getString("main_title", chat), chat)
    }

    fun handleAutoResetTitle(sender: Int, chat: Int) {

        if (Utils.goodUser(sender, chat)) {

            // if off
            if (Utils.isChatTitleAutoresetOn(chat)) {

                Utils.db.addString("auto_reset", "true", chat)
                Message()
                        .from(user)
                        .to(chat)
                        .text("Автоматическое возвращение главного имени беседы включено.")
                        .send()

            } else {
                Utils.db.addString("auto_reset", "false", chat)
                Message()
                        .from(user)
                        .to(chat)
                        .text("Автоматическое возвращение главного имени беседы выключено.")
                        .send()
            }
        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Только администратор может управлять именем беседы.")
                    .send()
        }
    }

    fun handleSaveExtra(hashtag: String, chat: Int, message: Message) {

        if (hashtag.length < 2) {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Хештег в вашем сообщении либо слишком короткий, либо его нет.")
                    .send()
            return
        }

        // We'll save only gifs and photos
        if (message.isPhotoMessage || message.isGifMessage) {

            when {
                message.isPhotoMessage -> {

                    val photoLink = message.getBiggestPhotoUrl(message.photos)

                    message.uploadPhoto(photoLink, { response ->

                        // photo62802565_456241137
                        val photoAsDoc = response.toString()

                        if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                            Utils.db.addString(hashtag, photoAsDoc, chat, "extra")

                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Сохранено как «$hashtag»")
                                    .attachments(photoAsDoc)
                                    .send()
                        } else {
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Произошла ошибка.")
                                    .send()
                        }
                    })
                }

                message.isGifMessage -> {

                    val attachs = message.attachments

                    var handled = false

                    for ((index, _) in attachs.withIndex()) {
                        val attach = attachs.getJSONObject(index)
                        val doc = attach.getJSONObject("doc")

                        // type == 3 if gif
                        if (doc.getInt("type") == 3) {
                            val url = doc.getString("url")

                            message.uploadDoc(JSONObject().put("doc", url).put("type", "doc"), { response ->

                                // doc62802565_449505848
                                val docAsAttach = response.toString()

                                if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                    Utils.db.addString(hashtag, docAsAttach, chat, "extra")

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Сохранено как «$hashtag»")
                                            .attachments(docAsAttach)
                                            .send()
                                }
                            })

                            handled = true
                            break
                        }
                    }

                    if (!handled) {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("Произошла ошибка.")
                                .send()
                    }
                }
            }

        } else {
            Message()
                    .from(user)
                    .to(chat) // voice if forwarded
                    .text("Можно сохранить только картинку, гифку или голосовое сообщение.")
                    .send()
        }
    }

    fun handleSetWelcomeMessage(msg: String, chat: Int, message: Message) {

        if (message.isPhotoMessage || message.isGifMessage) {

            when {

                message.isPhotoMessage -> {

                    val photoLink = message.getBiggestPhotoUrl(message.photos)

                    message.uploadPhoto(photoLink, { response ->
                        val photoAsDoc = response.toString()

                        if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                            Utils.db.addString("welcome_attach", photoAsDoc, chat)
                            Utils.db.addString("welcome_text", msg, chat)

                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Приветственное сообщение сохранено.")
                                    .send()
                        } else {
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Произошла ошибка.")
                                    .send()
                        }
                    })
                }

                message.isGifMessage -> {

                    val attachs = message.attachments

                    var handled = false

                    for ((index, _) in attachs.withIndex()) {
                        val attach = attachs.getJSONObject(index)
                        val doc = attach.getJSONObject("doc")

                        if (doc.getInt("type") == 3) {
                            val url = doc.getString("url")

                            message.uploadDoc(JSONObject().put("doc", url).put("type", "doc"), { response ->

                                val docAsAttach = response.toString()

                                if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                    Utils.db.addString("welcome_attach", docAsAttach, chat)
                                    Utils.db.addString("welcome_text", msg, chat)

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Приветственное сообщение сохранено.")
                                            .send()
                                }
                            })

                            handled = true
                            break
                        }
                    }

                    if (!handled) {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("Произошла ошибка.")
                                .send()
                    }
                }
            }

        } else {
            Utils.db.addString("welcome_attach", "", chat)
            Utils.db.addString("welcome_text", msg, chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("Приветственное сообщение сохранено.")
                    .send()
        }
    }

    fun handleSaveGoodbyeMessage(msg: String, chat: Int, message: Message) {

        if (message.isPhotoMessage || message.isGifMessage) {
            when {

                message.isPhotoMessage -> {

                    val photoLink = message.getBiggestPhotoUrl(message.photos)

                    message.uploadPhoto(photoLink, { response ->
                        val photoAsDoc = response.toString()

                        if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                            Utils.db.addString("goodbye_attach", photoAsDoc, chat)
                            Utils.db.addString("goodbye_text", msg, chat)

                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Сообщение для прощания сохранено.")
                                    .send()
                        } else {
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text("Произошла ошибка.")
                                    .send()
                        }
                    })
                }

                message.isGifMessage -> {

                    val attachs = message.attachments

                    var handled = false

                    for ((index, _) in attachs.withIndex()) {
                        val attach = attachs.getJSONObject(index)
                        val doc = attach.getJSONObject("doc")

                        if (doc.getInt("type") == 3) {
                            val url = doc.getString("url")

                            message.uploadDoc(JSONObject().put("doc", url).put("type", "doc"), { response ->

                                val docAsAttach = response.toString()

                                if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                    Utils.db.addString("goodbye_attach", docAsAttach, chat)
                                    Utils.db.addString("goodbye_text", msg, chat)

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Сообщение для прощания сохранено.")
                                            .send()
                                }
                            })

                            handled = true
                            break
                        }
                    }

                    if (!handled) {
                        Message()
                                .from(user)
                                .to(chat)
                                .text("Произошла ошибка.")
                                .send()
                    }
                }
            }
        } else {
            Utils.db.addString("goodbye_attach", "", chat)
            Utils.db.addString("goodbye_text", msg, chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("Сообщение для прощания сохранено.")
                    .send()
        }
    }

    fun handleSaveExtraFromForwarded(hashtag: String, chat: Int, message: Message) {

        try {

            val fwds = message.forwardedMessages

            val firstFwd = fwds.getJSONObject(0)

            val attachs = firstFwd.getJSONArray("attachments")

            if (hashtag.length < 2) {
                Message()
                        .from(user)
                        .to(chat)
                        .text("Хештег в вашем сообщении либо слишком короткий, либо его нет.")
                        .send()
                return
            }

            var handled = false

            for ((index, _) in attachs.withIndex()) {

                val attach = attachs.getJSONObject(index)

                val attachType = attach.getString("type")

                if (attachType == "doc" || attachType == "photo") {

                    val docOrPhoto = attach.getJSONObject(attachType)

                    when (attachType) {
                        "doc" -> {
                            val docType = docOrPhoto.getInt("type")

                            val isVoiceMessage = docOrPhoto.toString().contains("audio_msg") && docOrPhoto.toString().contains("waveform")

                            var docTypeEnum = DocTypes.DOC

                            if (isVoiceMessage) docTypeEnum = DocTypes.AUDIO_MESSAGE

                            if (docType == 3 || docType == 1 || isVoiceMessage) {

                                val url = docOrPhoto.getString("url")

                                message.uploadDoc(JSONObject().put("doc", url).put("type", docTypeEnum.type), { response ->

                                    val docAsAttach = response.toString()

                                    if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                        Utils.db.addString(hashtag, docAsAttach, chat, "extra")

                                        Message()
                                                .from(user)
                                                .to(chat)
                                                .text("Сохранено как «$hashtag»")
                                                .doc(docAsAttach)
                                                .send()
                                    }
                                })

                                handled = true
                            }
                        }

                        "photo" -> {

                            val array = JSONArray().put(docOrPhoto)
                            val url = message.getBiggestPhotoUrl(array)

                            message.uploadPhoto(url, { response ->
                                val photoAsDoc = response.toString()

                                if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                                    Utils.db.addString(hashtag, photoAsDoc, chat, "extra")

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Сохранено как «$hashtag»")
                                            .attachments(photoAsDoc)
                                            .send()
                                }
                            })

                            handled = true
                        }
                    }
                    break
                }
            }

            if (!handled) {
                Message()
                        .from(user)
                        .to(chat)
                        .text("Произошла ошибка.")
                        .send()
            }

        } catch (e: Exception) {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Произошла ошибка.")
                    .send()
        }
    }

    fun handleSetWelcomeMessageFromFwds(chat: Int, message: Message) {

        try {

            val fwds = message.forwardedMessages

            val firstFwd = fwds.getJSONObject(0)

            val attachs = firstFwd.getJSONArray("attachments")

            var handled = false

            val msg = firstFwd.getString("body")

            if (attachs.length() == 0) {

                Utils.db.addString("welcome_text", msg, chat)
                Utils.db.addString("welcome_attach", "", chat)

                Message()
                        .from(user)
                        .to(chat)
                        .text("Приветственное сообщение сохранено.")
                        .send()

                handled = true
            }

            for ((index, _) in attachs.withIndex()) {

                val attach = attachs.getJSONObject(index)

                val attachType = attach.getString("type")

                if (attachType == "doc" || attachType == "photo") {

                    val docOrPhoto = attach.getJSONObject(attachType)

                    when (attachType) {
                        "doc" -> {
                            val docType = docOrPhoto.getInt("type")

                            val isVoiceMessage = docOrPhoto.toString().contains("audio_msg") && docOrPhoto.toString().contains("waveform")

                            var docTypeEnum = DocTypes.DOC

                            if (isVoiceMessage) docTypeEnum = DocTypes.AUDIO_MESSAGE

                            if (docType == 3 || docType == 1 || isVoiceMessage) {

                                val url = docOrPhoto.getString("url")

                                message.uploadDoc(JSONObject().put("doc", url).put("type", docTypeEnum.type), { response ->

                                    val docAsAttach = response.toString()

                                    if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                        Utils.db.addString("welcome_text", msg, chat)
                                        Utils.db.addString("welcome_attach", docAsAttach, chat)

                                        Message()
                                                .from(user)
                                                .to(chat)
                                                .text("Приветственное сообщение сохранено.")
                                                .doc(docAsAttach)
                                                .send()
                                    }
                                })

                                handled = true
                            }
                        }
                        "photo" -> {

                            val array = JSONArray().put(docOrPhoto)
                            val url = message.getBiggestPhotoUrl(array)

                            message.uploadPhoto(url, { response ->
                                val photoAsDoc = response.toString()

                                if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                                    Utils.db.addString("welcome_attach", photoAsDoc, chat)
                                    Utils.db.addString("welcome_text", msg, chat)

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Приветственное сообщение сохранено.")
                                            .attachments(photoAsDoc)
                                            .send()
                                }
                            })

                            handled = true
                        }
                    }
                    break
                }
            }

            if (!handled) {
                Message()
                        .from(user)
                        .to(chat)
                        .text("Произошла ошибка.")
                        .send()
            }

        } catch (e: Exception) {
            Message()
                    .from(user)
                    .to(message.chatIdLong)
                    .text("Произошла ошибка.")
                    .send()
        }
    }

    fun handleSaveGoodbyeMessageFromFwds(chat: Int, message: Message) {

        try {

            val fwds = message.forwardedMessages

            val firstFwd = fwds.getJSONObject(0)

            val attachs = firstFwd.getJSONArray("attachments")

            var handled = false

            val msg = firstFwd.getString("body")

            if (attachs.length() == 0) {

                Utils.db.addString("goodbye_text", msg, chat)
                Utils.db.addString("goodbye_attach", "", chat)

                Message()
                        .from(user)
                        .to(chat)
                        .text("Приветственное сообщение сохранено.")
                        .send()

                handled = true
            }

            for ((index, _) in attachs.withIndex()) {

                val attach = attachs.getJSONObject(index)

                val attachType = attach.getString("type")

                if (attachType == "doc" || attachType == "photo") {

                    val docOrPhoto = attach.getJSONObject(attachType)

                    when (attachType) {
                        "doc" -> {
                            val docType = docOrPhoto.getInt("type")

                            val isVoiceMessage = docOrPhoto.toString().contains("audio_msg") && docOrPhoto.toString().contains("waveform")

                            var docTypeEnum = DocTypes.DOC

                            if (isVoiceMessage) docTypeEnum = DocTypes.AUDIO_MESSAGE

                            if (docType == 3 || docType == 1 || isVoiceMessage) {

                                val url = docOrPhoto.getString("url")

                                message.uploadDoc(JSONObject().put("doc", url).put("type", docTypeEnum.type), { response ->

                                    val docAsAttach = response.toString()

                                    if (docAsAttach.startsWith("doc") && docAsAttach.length > 5) {

                                        Utils.db.addString("goodbye_text", msg, chat)
                                        Utils.db.addString("goodbye_attach", docAsAttach, chat)

                                        Message()
                                                .from(user)
                                                .to(chat)
                                                .text("Cообщение для прощания сохранено.")
                                                .send()
                                    }
                                })

                                handled = true
                            }
                        }

                        "photo" -> {

                            val array = JSONArray().put(docOrPhoto)
                            val url = message.getBiggestPhotoUrl(array)

                            message.uploadPhoto(url, { response ->
                                val photoAsDoc = response.toString()

                                if (photoAsDoc.startsWith("photo") && photoAsDoc.length > 7) {

                                    Utils.db.addString("goodbye_attach", photoAsDoc, chat)
                                    Utils.db.addString("goodbye_text", msg, chat)

                                    Message()
                                            .from(user)
                                            .to(chat)
                                            .text("Cообщение для прощания сохранено.")
                                            .send()
                                }
                            })

                            handled = true
                        }
                    }
                    break
                }
            }

            if (!handled) {
                Message()
                        .from(user)
                        .to(chat)
                        .text("Произошла ошибка.")
                        .send()
            }

        } catch (e: Exception) {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Произошла ошибка.")
                    .send()
        }
    }

    fun handleNonChat(sender: Int) {

        val answer =
                "Привет! Я -- крутой бот, могу помочь администрировать беседу, чтобы в ней было чисто и приятно, а также, заодно и развлекать.<br>" +
                        "Чтобы начать пользоваться мной, напишите ${Commands.START_BOT.value} -- и далее следуйте инструкциям."

        Message()
                .from(user)
                .to(sender)
                .text(answer)
                .send()
    }

    fun handleStartBot(sender: Int) {

        val afterAddText = "Отлично, начнём! Бот кинул заявку в друзья, примите её, и затем напишите сюда ${Commands.CREATE_CHAT.value} -- тогда бот создаст чат, в котором он будет создателем, и вы сможете использовать команды администратора."
        val cantAddText = "К сожалению, не удалось добавить вас в друзья. Повторите запрос или напишите разработчку."
        val helloText = "Привет, я бот. Ты написал мне -- для продолжения работы нужно добавить меня в друзья."
        val alreadyFriendText = "Ты уже в друзьях, теперь всё, что осталось -- создать чат. Напиши ${Commands.CREATE_CHAT.value}, если готов. Если чат уже создавался, будет создан ещё один чат."

        user.api().call("friends.areFriends", "{user_ids:$sender}", { response ->

            if (response is JSONArray) {

                val userObject = response.getJSONObject(0)
                val isFriend = userObject.has("friend_status") && userObject.getInt("friend_status") == 3

                if (isFriend) {

                    Message()
                            .from(user)
                            .to(sender)
                            .text(alreadyFriendText)
                            .send()

                } else {

                    user.api().call("friends.add", "{user_id:$sender,text:'$helloText'}", { responseAdd ->

                        if (responseAdd.toString() == "1") {
                            Message()
                                    .from(user)
                                    .to(sender)
                                    .text(afterAddText)
                                    .send()
                        } else {
                            Message()
                                    .from(user)
                                    .to(sender)
                                    .text(cantAddText)
                                    .send()
                        }
                    })
                }
            }
        })
    }

    fun handleCreateChat(sender: Int) {

        val userNotAddedYet = "Вы еще не добавили бота в друзья, проверьте список заявок или повторите команду, если заявки нет: ${Commands.START_BOT.value}"
        val chatWillBeCreated = "Всё отлично, сейчас должен быть создан чат с вами и ещё одной страницей, которая будет тут же исключена из беседы (потому что три -- минимальное количество людей для создания беседы). <br><br>Напишите в чате ${Commands.HELP.value}, чтобы узнать, как пользоваться ботом."
        val firstChatMessage = "Привет! Чат создан и теперь можно полноценно пользоваться ботом, в том числе -- администрировать чат. Теперь вы можете сами приглашать кого угодно и делать всё, что нужно -- бот должен быть создателем беседы лишь для того, чтобы потом иметь возможность исключать кого угодно, а значит -- кикать, банить и так далее.<br><br>Чтобы узнать, как пользоваться ботом, напишите: ${Commands.HELP.value}"
        val errorCretingChat = "К сожалению, произошла ошибка при создании чата. Если ошибка повторится, напишите, пожалуйста разработчику: https://vk.me/id62802565"
        val newChatTitle = "Беседа"
        val secondUser = 434865146

        user.api().call("friends.areFriends", "{user_ids:$sender}", { response ->

            if (response is JSONArray) {

                val userObject = response.getJSONObject(0)
                val isFriend = userObject.has("friend_status") && userObject.getInt("friend_status") == 3

                if (isFriend) {
                    Message()
                            .from(user)
                            .to(sender)
                            .text(chatWillBeCreated)
                            .send()

                    user.api().call("messages.createChat", "{user_ids:[$sender,$secondUser],title:$newChatTitle}", { newChatId ->

                        if (newChatId is Number) {

                            /* CHAT CREATED */

                            // get chat id of new chat
                            val chat = Chat.CHAT_PREFIX + newChatId.toInt()

                            // send message 'all is ok' and help
                            Message()
                                    .from(user)
                                    .to(chat)
                                    .text(firstChatMessage)
                                    .send()

                            // make user an admin
                            Utils.db.addUserToList(sender, "admin", chat)

                            // kick third user
                            user.chat(chat).kickUser(secondUser)
                        } else {

                            Message()
                                    .from(user)
                                    .to(sender)
                                    .text(errorCretingChat)
                                    .send()
                        }
                    })
                } else {
                    Message()
                            .from(user)
                            .to(sender)
                            .text(userNotAddedYet)
                            .send()
                }
            }
        })
    }
}