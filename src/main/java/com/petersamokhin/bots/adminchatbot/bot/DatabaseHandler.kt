package com.petersamokhin.bots.adminchatbot.bot

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

/**
 * Database handler
 */
class DatabaseHandler private constructor() {

    // Singleton
    private object Holder {
        val INSTANCE = DatabaseHandler()
    }

    companion object {
        val INSTANCE: DatabaseHandler by lazy { Holder.INSTANCE }

        private var mongo: MongoClient? = null
        private var options: MongoClientOptions? = null
        private var database: MongoDatabase? = null
        private var strings_collection: MongoCollection<Document>? = null
        private var users_collection: MongoCollection<Document>? = null
    }

    init {
        options = MongoClientOptions.builder()
                .threadsAllowedToBlockForConnectionMultiplier(10)
                .connectionsPerHost(50)
                .build()

        mongo = MongoClient("localhost", options)
        database = mongo!!.getDatabase("admin_bot_database")
        strings_collection = database!!.getCollection("strings_collection")
        users_collection = database!!.getCollection("users_collection")
    }

    private fun getNewUser(userId: Int, chat: Int): Document {
        val user = Document()
                .append("user_id", userId)
                .append("chat_id", chat)
                .append("warnings", 0)
                .append("pidor", 0)
                .append("is_pidor", false)
                .append("admin", false)
                .append("banned", false)

        return user
    }

    /* Warnings */

    fun incrementCounter(userId: Int, chat: Int, type: String) {

        val search = Document()
                .append("user_id", userId)
                .append("chat_id", chat)

        var user = users_collection!!.find(search).first()

        if (user == null) {
            user = getNewUser(userId, chat)
            user.append(type, 1)
            users_collection!!.insertOne(user)
        } else {
            val wCount = user.getInteger(type)
            users_collection!!.findOneAndReplace(search, user.append(type, wCount + 1))
        }
    }

    fun getCounter(userId: Int, chat: Int, type: String): Int {
        val search = Document()
                .append("user_id", userId)
                .append("chat_id", chat)

        val user = users_collection!!.find(search).first()

        if (user != null && user.containsKey(type)) {
            return user.getInteger(type)
        } else {
            return 0
        }
    }

    fun clearCounter(userId: Int, chat: Int, type: String) {
        val search = Document().append("user_id", userId).append("chat_id", chat)

        val user = users_collection!!.find(search).first()

        if (user != null) {
            users_collection!!.findOneAndReplace(search, user.append(type, 0))
        }
    }

    /* Users */

    fun addUserToList(userId: Int, type: String, chat: Int) {
        val search = Document()
                .append("user_id", userId)
                .append("chat_id", chat)

        var user = users_collection!!.find(search).first()

        if (user == null) {
            user = getNewUser(userId, chat)
            user.append(type, true)
            users_collection!!.insertOne(user)
        } else {
            users_collection!!.findOneAndReplace(search, user.append(type, true))
        }
    }

    fun removeUserFromList(userId: Int, type: String, chat: Int) {
        val search = Document().append("user_id", userId).append("chat_id", chat)

        val user = users_collection!!.find(search).first()

        if (user != null) {
            users_collection!!.findOneAndReplace(search, user.append(type, false))
        }
    }

    fun isUserInList(userId: Int, type: String, chat: Int): Boolean {

        val user = users_collection!!.find(Document().append("user_id", userId).append("chat_id", chat)).first()

        return user != null && user.getBoolean(type)
    }

    fun getUser(userId: Int, chat: Int): Document? {
        return users_collection!!.find(Document().append("user_id", userId).append("chat_id", chat)).first()
    }

    /* Strings */

    fun addString(key: String, value: String, chat: Int, type: String = "default") {

        if (strings_collection!!.find(Document()
                .append("key", key)
                .append("chat_id", chat)
                .append("type", type)).first() == null) {
            strings_collection!!.insertOne(Document()
                    .append("chat_id", chat)
                    .append("type", type)
                    .append("key", key)
                    .append("value", value))
        } else {
            strings_collection!!.findOneAndReplace(
                    // find
                    Document()
                            .append("key", key)
                            .append("chat_id", chat)
                            .append("type", type),
                    // replace
                    Document()
                            .append("chat_id", chat)
                            .append("key", key)
                            .append("value", value)
                            .append("type", type)
            )
        }
    }

    fun getString(key: String, chat: Int, type: String = "default"): String {
        return try {
            strings_collection!!.find(Document().append("key", key).append("chat_id", chat).append("type", type)).first().getString("value")
        } catch (e: Exception) {
            ""
        }
    }

    /* Extra */

    fun getStringsList(chat: Int, type: String = "default", returnType: String = "value"): ArrayList<String> {

        val list = ArrayList<String>()

        strings_collection!!.find(Document().append("chat_id", chat).append("type", type)).iterator().use { cursor ->
            while (cursor.hasNext()) {
                list.add(cursor.next().getString(returnType))
            }
        }

        return list
    }

    /* Lists */

    fun getList(type: ListTypes, chat: Int): ArrayList<Int> {
        when (type) {
            ListTypes.ADMINS -> {
                val list = ArrayList<Int>()

                users_collection!!.find(Document().append("chat_id", chat).append("admin", true)).iterator().use { cursor ->
                    while (cursor.hasNext()) {
                        list.add(cursor.next().getInteger("user_id"))
                    }
                }

                return list
            }
            ListTypes.BANNED -> {
                val list = ArrayList<Int>()

                users_collection!!.find(Document().append("chat_id", chat).append("banned", true)).iterator().use { cursor ->
                    while (cursor.hasNext()) {
                        list.add(cursor.next().getInteger("user_id"))
                    }
                }

                return list
            }
            ListTypes.LIST_PIDOR -> {
                val list = ArrayList<Int>()

                users_collection!!.find(Document().append("chat_id", chat).append("is_pidor", true)).iterator().use { cursor ->
                    while (cursor.hasNext()) {
                        list.add(cursor.next().getInteger("user_id"))
                    }
                }

                return list
            }
            else -> {
                return ArrayList()
            }
        }
    }

    fun getPidorStats(chat: Int): HashMap<Int, Int> {

        val map = HashMap<Int, Int>()

        users_collection!!.find(Document().append("chat_id", chat).append("is_pidor", true)).iterator().use { cursor ->
            while (cursor.hasNext()) {
                val user = cursor.next()

                map.put(user.getInteger("user_id"), user.getInteger("pidor"))
            }
        }

        return map
    }
}