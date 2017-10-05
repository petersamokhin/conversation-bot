# Conversation Bot
<img src="https://www.petersamokhin.com/files/conversation-bot/cover.png"></img>
#### Крутой бот для бесед ВКонтакте. Включает также функционал пары популярных ботов из Telegram.

#### Что бот может:

* Бот работает только в групповых чатах (беседах)
* Может администрировать беседу (на данный момент только если он является создателем чата)
* Может сохранять медиа по хештегам (по примеру **Group Butler** из Telegram)
* Может сопровождать приглашение/исключение пользователей из беседы сообщениями 
* Позволяет каждый день играть в игру, выбирая случайного человека из беседы и давая ему какое-нибудь звание (по примеру **Sublime Bot** из Telegram)
* Может сохранять какое-либо имя беседы и, если нужно, автоматически или по команде его восстанавливать
* Все действия и почти все ответы бота могут быть настроены

#### Как начать пользоваться ботом:

1. Написать боту **/start** [в личные сообщения](https://vk.com/conv_bot)
2. Следовать инструкциям:
    * Принять пришедшую заявку в друзья от бота
    * Написать боту **/create_chat** в личные сообщения
  
Готово. Будет создана беседа, в которой бот будет создателем и сможет выполнять функции администратора. Иначе можно просто добавить бота в беседу, но тогда он не будет ее создателем, не сможет исключать пользователей из беседы, а значит кикать и банить.

#### Список команд:
* **/list** `[admins|banned|extra]` — вывести список администраторов беседы/заблокированных пользователей/сохранённых медиа
* **/kick** `[ссылка|id|упоминание|пересланное сообщение]` — кикнуть пользователя из беседы. Выбор целевого пользователя в таких командах, как `/kick`, `/ban` и прочих, происходит одинаково, и сделан максимально удобно.
* **/ban** — забанить пользователя. В следующий раз, когда его пригласят в эту беседу, он будет моментально из неё исключен.
* **/unban** - снять блокировку.
* **/warn** — сделать пользователю предупреждение. После `5` предупреждений пользователь будет автоматически исключен из беседы.
* **/clear_warns** — обнулить список предупреждений пользователя.
* **/admin** — назначить пользователя администратором.
* **/remove_admin** — разжаловать пользователя.
* **/extra** `[хештег]` — сохранить прикрепления к сообщению как «`#хештег`» (если переслать сообщение, то медиа будет взято оттуда). После этого, если в чьём-нибудь сообщении будет содержаться `#хештег`, бот его перешлёт и прикрепит сохранённые медиа. Работает для фотографий, гифок и (если использовать команду, переслав голосовое сообщение) для голосовых сообщений.
* **/main_title** `[имя беседы]` — запомнить главное имя беседы. 
* **/reset_title** — восстановить сохранённое главное имя беседы.
* **/auto_reset_title** — при изменении беседы автоматически восстанавливать сохранённое главное имя беседы.
* **/welcome** — сохранить текст сообщения и прикрепленную фототграфию/гифку, и при вступлении или приглашении кого-либо в беседу, будет отправлено это сообщение. В тексте сообщения `${username}` будет заменено на упоминание вступившего пользователя.
* **/goodbye** — аналогично команде `/welcome`, но для вышедших или исключенных из беседы пользователей.
* **/me** — вывести статистику о себе (количество побед в игре, предупреждений, статус администратора и так далее).
* **/super_reg** — зарегистрироваться в игре.
* **/super_name** — установить звание для игры. Например, «молодец». Тогда ежедневно после `00:00` по `МСК` будет разыгрываться звание «молодца» дня. 
* **/superuser** — запустить игру. Если игра сегодня уже запускалась, будет выведено имя победителя.
* **/super_stats** — вывести имена `10` лучших игроков, количество их побед, а также общее количество игроков.
* **/help** — помощь.
