package com.petersamokhin.bots.adminchatbot.bot

/**
 * Text of commands, etc
 */

enum class Commands(val value: String) {
    LIST("/list"),
    KICK("/kick"),
    EXTRA("/extra"),
    HELP("/help"),
    BAN("/ban"), UNBAN("/unban"),
    WARN("/warn"), CLEAR_WARNINGS("/clear_warns"),
    ADD_ADMIN("/admin"), REMOVE_ADMIN("/remove_admin"),
    SET_MAIN_TITLE("/main_title"), RESET_TITLE("/reset_title"), AUTO_RESET_TITLE("/auto_reset_title"),
    WELCOME("/welcome"), GOODBYE("/goodbye"),
    PIDOR("/superuser"), ME("/me"), PIDORSTATS("/super_stats"), PIDOREG("/super_reg"), PIDORNAME("/super_name"),
    START_BOT("/start"), CREATE_CHAT("/create_chat")
}

enum class ListTypes {
    ADMINS,
    BANNED,
    LIST_PIDOR,
    LIST_EXTRA
}