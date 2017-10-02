package com.petersamokhin.bots.adminchatbot.bot

import com.petersamokhin.bots.sdk.callbacks.Callback
import com.petersamokhin.bots.sdk.clients.User
import com.petersamokhin.bots.sdk.objects.Message
import com.petersamokhin.bots.sdk.utils.vkapi.docs.DocTypes
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Created by PeterSamokhin on 03/10/2017 02:07
 */
class CommandsHandler(private val user: User) {

    private val help = "Список команд:<br>" +
            "/list [admins|banned|extra] -- список администраторов/заблокированных/медиа" + "<br>" +
            "/kick -- исключить из беседы" + "<br>" +
            "/ban -- забанить пользователя (автокик)" + "<br>" +
            "/unban -- разбанить пользователя" + "<br>" +
            "/warn -- сделать предупреждение пользователю, 5 предупреждений -- кик из беседы" + "<br>" +
            "/clear_warns -- очистить предупреждения пользователя" + "<br>" +
            "/admin -- сделать пользователя администратором" + "<br>" +
            "/remove_admin -- разжаловать пользователя" + "<br>" +
            "/extra [хештег] -- сохранить медиа. Затем по хештегу оно будет отправлено." + "<br>" +
            "/main_title -- установить главное имя беседы, до которого можно потом откатиться, или поставить автоматический откат" + "<br>" +
            "/reset_title -- откатить имя беседы до главного" + "<br>" +
            "/auto_reset_title -- автоматически восстанавливать главное имя беседы при его изменении" + "<br><br>" +
            "Чтобы указать, к какому пользователю будет относиться действие, либо перешлите его сообщение и укажите команду, либо укажите [id|ссылка|упоминание|пересланное сообщение] в сообщении.<br><br>" +
            "Банить/кикать/управлять именем беседы/назначать и ражаловать админов могут только админы, и это будет работать только если бот -- создатель чата." + "<br>" +
            "/help -- помощь<br><br>" +
            "В личные сообщения бот не отвечает, только в беседах."

    /* HANDLE COMMANDS */

    fun helpMessage(): Message {
        return Message()
                .from(user)
                .text("Помощь:")
                .attachments()
    }

    fun handleKick(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            user.chat(chat).kickUser(target, Callback<Any> { response ->

                if (response.toString() == "1") {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Кикнул [id$target|пидора]!")
                            .send()
                } else {
                    Message()
                            .from(user)
                            .to(chat)
                            .text("Не удалось кикнуть [id$target|пидора]!")
                            .send()
                }
            })
        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$sender|Пидор], ты не админ, либо другой юзер админ. Будешь пиздеть, кикну тебя.")
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
                    .text("[id$target|Пидор] теперь забанен.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Только админ может банить. Либо юзер админ, либо уже забанен. А ты -- пидор.")
                    .send()
        }
    }

    fun handleUnban(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.removeUserFromList(target, "banned", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пидор] теперь разбанен, если он был забанен.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Только админ может разбанить. Либо юзер админ и не забанен. А ты -- пидор.")
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
                            .text("[id$target|Пидор] получил предупреждение. <br>Теперь у него $wCount предупреждений. Ещё ${5 - wCount} и вылетит нахрен.")
                            .send()
                }
                else -> {

                    handleKick(sender, target, chat)
                    Message()
                            .from(user)
                            .to(chat)
                            .text("[id$target|Пидор] в край охуевший, получил слишком много предупреждений.")
                            .send()
                }
            }
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Только админ может делать предупреждения. Либо юзер админ. А ты -- пидор.")
                    .send()
        }
    }

    fun handleClearWarnings(sender: Int, target: Int, chat: Int) {
        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.clearCounter(target, chat, "warnings")

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пидор] теперь чист, предупреждения удалены.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Только админ может очищать предупреждения. Либо юзер админ. А ты -- пидор.")
                    .send()
        }
    }

    fun handleAdmin(sender: Int, target: Int, chat: Int) {

        if (Utils.goodUser(sender, chat) && !Utils.goodUser(target, chat)) {

            Utils.db.addUserToList(target, "admin", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пидор] теперь админ.")
                    .send()
        } else {

            Message()
                    .from(user)
                    .to(chat)
                    .text("Только админ может назначить админа. Либо юзер уже админ. А ты -- пидор.")
                    .send()
        }
    }

    fun handleRemoveAdmin(sender: Int, target: Int, chat: Int) {
        if (Utils.goodUser(sender, chat) && Utils.goodUser(target, chat)) {

            Utils.db.removeUserFromList(target, "admin", chat)

            Message()
                    .from(user)
                    .to(chat)
                    .text("[id$target|Пидор] больше не админ.")
                    .send()

        } else {
            Message()
                    .from(user)
                    .to(chat)
                    .text("Либо [id$target|пидор] не админ, либо ты. Оба идите нахуй.")
                    .send()
        }
    }

    private fun setTitle(title: String, chat: Int) {
        println("setting title [$title] in chat $chat")
        user.chat(chat).editTitle(title, Callback<Any> { response ->
            println("Set title response = [$response]")
        })
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

                    /*
                     * ласт дату мы поменяли
                     * ищем пидора, шлем сообщения
                     * записываем его в бд
                     *
                     */
                } else {

                    /*
                     * отправляем текущего пидора
                     *
                     */

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
}