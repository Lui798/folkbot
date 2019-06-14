# folkbot
A simple discord bot that aims to link twitch chat with discord and provide Discord rich embed live notifications.
## Getting Stated
### Prerequisites
You need Java 8 or higher
### Config
```
#tdbot.conf
client={Twitch application client id}
liveChannel={Id of live notification channel} (Can be set with ?live)
user={User to get info from} (Lowercase, same as url)
token={Discord bot token}
prefix={Prefix before commands} (Default: ?)
```
### Commands
```
?live (Sets channel for live notifications to current channel)
?user {name} (Sets twitch user to the one given)
?clear {num} (Clears the given number of messages, useful for cleaning up the live chat)
```
### Running
You can optionally run it on another twitch user via commandline.
```
java -jar tdbot-1.0-SNAPSHOT-all.jar {user}
```
### Latest Release
You can grab the latest version [here](https://github.com/Lui798/twitch-discord-bot/releases/latest)
