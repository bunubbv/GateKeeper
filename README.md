**PassiveWhitelist** is a lightweight plugin made for semi-private Minecraft servers, like those run by schools or small friend groups, where you want to keep access limited to people you trust. Instead of using the traditional whitelist system, new players have to answer a security question the first time they join. If they get it right, they’re permanently verified and can play on the server. You can customize the question, the answer and the time limit.

## Configuration
* The plugin creates a config file at plugins/PassiveWhitelist/config.yml.
* Default settings:

```yaml
answer: "bunubbv"
question: "<aqua>What is the nickname of the person who hosts our server?</aqua>"
welcomeMessage: "<yellow>Welcome! Please answer the following question to play the server:</yellow>"
correctMessage: "<green>Correct! You're now verified and ready to play.</green>"
incorrectMessage: "<red>Incorrect answer. Please try again.</red>"
kickMessage: "<red>You didn't answer in time. Please try again later.</red>"
kickDelay: 3 # minute(s)
```
 
* After editing the config, run /psw reload to apply changes without restarting the server.

## Permissions
* psw.bypass:
  * default: op
* psw.reload:
  * default: op
* psw.revoke:
  * default: op

## Commands
* /psw reload
* /psw revoke \<username>
* /psw bypass \<username>
