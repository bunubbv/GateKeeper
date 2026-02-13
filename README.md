**GateKeeper** is a lightweight plugin made for semi-private Minecraft servers, like those run by schools or small friend groups, where you want to keep access limited to people you trust. Instead of using the traditional whitelist system, new players have to answer a security question the first time they join. If they get it right, they’re permanently verified and can play on the server. You can customize the question, the answer and the time limit.

## Configuration
* The plugin creates a config file at plugins/GateKeeper/config.yml.
* Default settings:

```yaml
answer: "16"
question: "Evaluate the definite integral of 4 with respect to x, from 0 to 4."
welcomeMessage: "Welcome! Please answer the following question to join the server:"
correctMessage: "Correct! You're now verified and ready to play."
incorrectMessage: "Incorrect answer. Please try again."
kickMessage: "You didn't answer in time. Please try again later."
kickDelay: 3 # minute(s)
```
 
* After editing the config, run /gk reload to apply changes without restarting the server.

## Permissions
* gatekeeper.bypass:
  * default: op
* gatekeeper.reload:
  * default: op
* gatekeeper.revoke:
  * default: op

## Commands
* /gk reload
* /gk revoke \<username>
* /gk bypass \<username>
