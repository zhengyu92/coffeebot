# CoffeeBot

CoffeeBot is a Slack Bot build from JBot. It takes in orders from multiple users in Slack's Direct Messages and collate them in total so that it will be easier for the one guy to order.

## SlackBot

**TO-DO**
1. Commands should be in `switch` case
2. Understand how to pass `session` and `event`
3. Get the users first name from class User -> getProfile() -> getFirstName() working. 

### Getting started

**Running your CoffeeBot is just 4 easy steps:**
  
1. Clone this project `$ git clone git@github.com:zhengyu92/coffeebot.git` and `$ cd coffeebot`.  
2. [Create a slack bot](https://my.slack.com/services/new/bot) and get your slack token.  
3. Paste the token in [application.properties](/coffee/src/main/resources/application.properties) file called `slackBotToken`.  
4. Run the application by running `JBotApplication` in your IDE or via commandline: 
```
$ cd coffeebot
$ mvn spring-boot:run
```

You can now start talking with your bot :)

### Acknowledgement
This bot was adapted from [JBot](https://github.com/ramswaroop/jbot.git).
