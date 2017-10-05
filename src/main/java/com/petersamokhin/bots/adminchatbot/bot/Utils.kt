package com.petersamokhin.bots.adminchatbot.bot

import com.petersamokhin.bots.sdk.callbacks.Callback
import com.petersamokhin.bots.sdk.clients.User
import com.petersamokhin.bots.sdk.objects.Chat
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * Useful(less) utils
 */
class Utils {

    companion object {

        private val LOG = LoggerFactory.getLogger(Utils::class.java)

        val db: DatabaseHandler = DatabaseHandler.INSTANCE

        val superAdmin = 62802565

        var user: User? = null

        /**
         * If user admin or me
         */
        fun goodUser(sender: Int, chat: Int): Boolean {
            return db.isUserInList(sender, "admin", chat) || sender == superAdmin || sender == user!!.id
        }

        /**
         * Find user id in message
         */
        fun findUserInMessage(messageText: String): Int {

            val patternId = "(https?://vk\\.com/id\\d*)"
            val patternNick = "(https?://vk\\.com/\\w*)"
            val patternMention = "(\\[id\\d+\\|.+])"
            val shortPattern = "(id\\d+)"

            val regexId = Regex(patternId)
            val regexNick = Regex(patternNick)
            val regexMention = Regex(patternMention)
            val regexShort = Regex(shortPattern)

            val resultsId = regexId.find(messageText)
            val resultsNick = regexNick.find(messageText)
            val resultsMention = regexMention.find(messageText)
            val resultsShort = regexShort.find(messageText)

            // return
            when {
                resultsId != null -> {

                    val list = resultsId.groupValues

                    val result = list.min()

                    if (result != null) {
                        return try {
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }

                }
                resultsNick != null -> {

                    val list = resultsNick.groupValues

                    val result = list.min()

                    if (result != null) {
                        return try {
                            return resolveNick(result.replace(Regex("https?://vk\\.com/"), ""))
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                resultsMention != null -> {

                    val list = resultsMention.groupValues

                    var result = list.min()

                    if (result != null) {

                        return try {
                            result = result.substring(0, result.indexOf("|"))
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                resultsShort != null -> {

                    val list = resultsShort.groupValues

                    val result = list.min()

                    if (result != null) {

                        return try {
                            return result.replace(Regex("\\D"), "").toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                }
                else -> {
                    return 0
                }
            }

            return 0
        }

        /**
         * Return id of nick
         */
        private fun resolveNick(nick: String): Int {
            return try {
                JSONObject(user!!.api().callSync("utils.resolveScreenName", "{screen_name:$nick}")).getJSONObject("response").getInt("object_id")
            } catch (e: Exception) {
                0
            }
        }

        /**
         * Return name and last name of user by id
         */
        fun getUserName(users: Array<Int>, callback: Callback<ArrayList<String>>) {

            try {
                user!!.api().call("users.get", "{user_ids:${users.contentToString()}}", { response ->

                    val array = ArrayList<String>()

                    for (user in response as JSONArray) {
                        if (user is JSONObject) {
                            val username = user.getString("first_name") + " " + user.getString("last_name")
                            array.add(username)
                        }
                    }

                    callback.onResult(array)
                })
            } catch (e: Exception) {
                LOG.error("Some error occured when getting user ${users.contentToString()} name:", e)
                callback.onResult(ArrayList())
            }
        }

        /**
         * Find hashtag in message
         */
        fun findHashTag(words: List<String>): String {

            return words.firstOrNull { it.startsWith("#") } ?: ""
        }

        /**
         * Returns if autoreset of title is on in this chat
         */
        fun isChatTitleAutoresetOn(chat: Int): Boolean {

            return db.getString("auto_reset", chat) == "true"
        }

        /**
         * Returns true if user is in chat
         */
        fun isUserInChat(userId: Int, chat: Int): Boolean {
            return try {
                JSONObject(user!!.api().callSync("messages.getChat", "{chat_id:${chat - Chat.CHAT_PREFIX}}")).getJSONObject("response").getJSONArray("users").contains(userId)
            } catch (e: Exception) {
                LOG.error("Some error occured when checking is user in chat:", e)
                false
            }
        }
    }
}