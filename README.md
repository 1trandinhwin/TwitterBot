# TwitterBot
Twitter Bot program using Twitter4J library.

Code is in Java 8 and runs in Eclipse Version Oxygen.

This program controls a Twitter bot under the screen name of BotV1_18_19_5
It follows users that request to follow the bot, and reads their tweets.
It checks for any duplicate tweets, and only uses new tweets.
Using the new tweet, it replaces certain keywords with other words or phrases, and retweets it back
towards the original sender by using "@senderScreenName".
