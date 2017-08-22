import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * 
 */

/**
 * @author WinnieTrandinh
 * Last modified: April 12, 2017
 * This program controls a Twitter bot under the screen name of BotV1_18_19_5
 * It follows users that request to follow the bot, and reads their tweets.
 * It checks for any duplicate tweets, and only uses new tweets.
 * Using the new tweet, it replaces certain keywords with other words or phrases, and retweets it back
 * towards the original sender by using "@senderScreenName".
 */
public class TwitterBot {

	/**
	 * @param args
	 */

	//this checks if the tweet is new by cross referencing with a text file containing all the past tweets
	//if tweet is new, it adds the new tweet into the history text file
	//takes two parameters:
		//fileName = name of history text file
		//tweetRaw = the tweet that it is checking; contains the tweet and the sender, connected by ",,,,,"
	//returns the tweet if it is new
	//returns null if the tweet is old
	public static String checkDuplicates(String fileName, String tweetRaw) {
		//creates arraylist to store booleans of whether a duplicate of the tweet is found or not
		List<Boolean> duplicate = new ArrayList<Boolean>();
		//splits the raw tweet into the tweet and the sender
		String [] tweetSplit = tweetRaw.split(",,,,,");
		//stores the tweet in tweetRefined string
		String tweetRefined = tweetSplit[0];

		//string to store the content of a line in the text file
		String line = null;
		
        try {
            //sets up the file reader
            FileReader fileReader = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(fileReader);

            //reads the first line of text file
            if ( (line = reader.readLine() ) == null) {
            	//first line is empty
            	//hence file is empty
            	//there cannot be any duplicates since file is empty
            	//writes in the new tweet by calling updateHistory
            	//parameters are as follows:
            		//fileName = name of text file
            		//tweetRefined = the tweet 
            	updateHistory(fileName, tweetRefined);
            } else {
            	//file is not empty
            	//checks for duplicates for the first line
            	if (tweetRefined.equals(line) == false ) {
            		//tweet is not equal to the first line of the text tile
        			//stores result of false into boolean array list
            		duplicate.add(false);
                } else {
                	//tweet is equal to first line of text file
                	//stores result of true into boolean array list
                	duplicate.add(true);
                }
            	
            	//reads the remaining lines of the text file
            	while ((line = reader.readLine()) != null) {
            		//while the next line is not empty
            		if (tweetRefined.equals(line) == false ) {
	                	//not a duplicate tweet
            			duplicate.add(false);
	                } else {
	                	//tweet is a duplicate
	                	duplicate.add(true);
	                }
            	}
            	
            	
            	if (duplicate.contains(true) == false) {
            		//boolean array list does not contain true
            		//indicates that there are no duplicates
            		//writes in new tweet by calling updateHistory
            		updateHistory(fileName, tweetRefined);
            	}

            }
            
            //closes text file
            reader.close();         
        }		//different errors that could occur during the reading/opening of text file
        catch(FileNotFoundException e) {
            System.out.println(
                "Unable to open file '" + fileName + "'");                
        }
        catch(IOException e) {
            System.out.println("Error reading file '" + fileName + "'");                  
        }
        
        //checks if there are any duplicates
        if (duplicate.contains(true)) {
        	//there is a duplicate, tweet is not new
        	//returns null value
        	return null;
        } else {
        	//new tweet
        	//returns tweet
        	return tweetRefined;
        }
        
	}
	
	
	//this writes in the new tweets in the text file
	//parameters are as follows:
		//fileName = name of text file
		//tweet = new tweet to be writen in text file
	public static void updateHistory(String fileName, String tweet) {
		//lets user know that there is a new tweet
		System.out.println("new tweet:");
    	try {
            //sets up file writer
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            //writes the tweet into the text file
            writer.write(tweet);
            writer.newLine();

            //closes text file
            writer.close();
        }
        catch(IOException e) {
        	//if could not write in file
            System.out.println("Error writing to file '" + fileName + "'");
        }
        
	}
	
	
	//this takes in tweet, replaces all the keywords with replacement words, and returns the modified tweet
	//parameters are as follows:
		//keywords = 2D array that contains all the keywords and its replacement words
		//sentence = the original tweet
		//screenName = the screen name of the sender
	//returns the modified tweet
	public static String replaceWords(String [] [] keywords, String sentence, String screenName) {
		//checks for multiple word keywords and replaces them
		//repeats for each keyword
		for (int i = 0; i < keywords.length; i++ ) {
			//ignores case of keyword
			Pattern ignoreCaseWord = Pattern.compile(keywords[i][0], Pattern.CASE_INSENSITIVE);
			//attempts to match the keyword with any part of the tweet
			Matcher keyword = ignoreCaseWord.matcher(sentence);
			if (keyword.find() == true && keywords[i][0].contains(" ") ) {
				//contains keyword and keyword is two or more words
				//stores the keyword as written in the original tweet
				//maintains its original capitalization
				String exactKeyword = keyword.group(0); 
				//splits tweet into string array using the keyword
				String [] sentences = sentence.split("(?i)" + keywords[i][0] );
				try {
					//joins content of string together using replacement word
					//changeCaps changes the capitalization of the replacement words to replicate the capitalization of the original words
					//checkCap checks the capitalization of the original words
					sentence = String.join(changeCaps(keywords[i][1], checkCap(exactKeyword) ), sentences[0], sentences[1] );
					
				} catch (ArrayIndexOutOfBoundsException e) {
					//no text before or after keyword
					try {
						//recreates tweet with replacement words
						sentence = sentences[0] + changeCaps(keywords[i][1], checkCap(exactKeyword) );
					} catch (ArrayIndexOutOfBoundsException f) {
						//no text before and after keyword
						//replaces tweet with replacement words
						sentence = changeCaps(keywords[i][1], checkCap(sentence) );
					}
				}
			}
		}
		
		//checks for single word replacements
		//split sentence into words
		String [] words = sentence.split(" ");
		//constructs the new tweet
		sentence = "@" + screenName;
		
		//repeats for each word in the tweet
		//checks if it is equal to the keyword
		for (int i = 0; i < words.length; i ++) {
			for (int j = 0; j < keywords.length; j++) {
				//ignores capitalization of keyword
				Pattern ignoreCaseWord = Pattern.compile(keywords[j][0], Pattern.CASE_INSENSITIVE);
				//attempts to match keyword with word
				Matcher keyword = ignoreCaseWord.matcher(words[i]);
				if (keyword.find() == true) {
					//match is found
					//stores the exact keyword used in the tweet
					String exactKeyword = keyword.group(0);
					
					//checks that the word is actually the keyword by comparing lengths of word to length of replacement word
					//prevents finding a keyword inside another word, such as finding "I" in "white"
					if ( (exactKeyword.length() == 1 && words[i].length() == 1) || (exactKeyword.length() != 1) ) {
		//adds back punctuation
						//words before being replaced; words from original tweet
						char [] letters = words[i].toCharArray();
						//changes capitalization of replacement word
						words[i] = changeCaps(keywords[j][1], checkCap(exactKeyword) );
						
						//checks if the last character in the word is a letter or not
						if (Character.isLetter(letters[letters.length-1] ) == false ) {
							//if not a letter, adds back the end character
							words[i] += letters[letters.length-1];
						}
						
						//repeats for each character of the word
						for (int p = 2; p < letters.length+1; p++) {
							if (Character.isLetter(letters[letters.length-p] ) == false) {
								//specified character is not a letter
								char [] newLetters = new char [words[i].length()+p];
								char [] replacementWordLetters = words[i].toCharArray();
								//fills new array
								for (int k = 0; k < newLetters.length-p; k++) {
									newLetters[k] = replacementWordLetters[k];
								}
								//adds in punctuation/symbols and the rest of the word
								int u = p;
								while (u > 0) {
									newLetters[newLetters.length-u] = letters[letters.length-u];
									u--;
								}
								
								//converts char array into the string array "words"
								words[i] = new String (newLetters);
							}
						}
						
					}
				
				}
				
			}
				
			//place each word into new sentence with spaces in between
			sentence += " " + words[i];
			
		} 
		
		//fixes any errors in spacing
		if (sentence.contains("  ") ) {
			sentence = sentence.replaceAll("  ", " ");
		} 
		//fixes conflicts with replacement words
		if (sentence.contains("totally legal totally legal") ) {
			sentence = sentence.replaceAll("totally legal totally legal", "totally legal");
		}
		
		//returns the modified tweet
		return sentence;
	}
	
	
	//checks char limit of message
	//parameters are as follows:
		//text = the tweet to be tested
		//twitter = twitter object necessary to post tweet
	public static void checkLimit(String text, Twitter twitter) {
		if (text.length() <= 140) {
			//tweet is within size limit
			//prints out tweet for user
			System.out.println(text);
			
			try {
				//tweets out the new tweet
				twitter.updateStatus(text);
			} catch (TwitterException e) {
				//error in posting tweet
				System.out.println("could not post tweet");
			}
		} else {
			//tweet exceeds size limit
			//splits tweet into individual words
			String [] words = text.split(" ");
			//the string containing the first tweet to be sent, contains original sender's screen name
			String firstTweet = words[0];
			//the second tweet to be sent out
			String secondTweet = words[0];
			//counter variable
			int i = 1;
			while (i < words.length) {
				//runs for each word in the tweet
				if ( (firstTweet.length() + 1 + words[i].length() ) <= 140 ) {
					//word can be added onto tweet without exceeding limit
					//adds word into first tweet
					firstTweet += " " + words[i];
				} else {
					//adding another word exceeds the limit of the first tweet
					//adds word into second tweet
					secondTweet += " " + words[i];
				}
				i++;
			}
			//prints out each tweet for user
			System.out.println(firstTweet);
			System.out.println(secondTweet);
			
			try {
				//tweets out the tweet using two different tweets
				twitter.updateStatus(firstTweet);
				twitter.updateStatus(secondTweet);
			} catch (TwitterException e) {
				//could not post tweet
				System.out.println("could not post tweet");
			}
		}
		
	}
	
	//checks for caps
	//parameter is as follows:
		//text = the word/phrase to be tested
	//returns an int array representing how the word/phrase is capitalized
	//int array contains one of the following:
		//0 = no caps
		//1 = first letter is caps
		//2 = all caps
	public static int [] checkCap(String text) {
		//creates array list that will store the capitalization of each letter
		ArrayList<Integer> caps = new ArrayList<Integer>(0);
		//splits phrase into individual words
		String [] words = text.split(" ");
		
		//repeats for each word
		for (int i = 0; i < words.length; i++) {
			//converts word into char array
			char [] letters = words[i].toCharArray();
			if (Character.isUpperCase(letters[0]) == true) {
				if (Character.isUpperCase(letters[letters.length - 1] ) ) {
					//first and last letter is caps, indicates all caps
					caps.add(2);
				} else { 
					//first letter is caps
					caps.add(1);
				}
			} else {
				//no caps
				caps.add(0);
			}
		}
		
		//changes array list into int array
		int [] capsArray = new int [caps.size() ];
		for (int i = 0; i < capsArray.length; i++) {
			capsArray[i] = caps.get(i);
		}

		//returns int array
		return capsArray;
	}
	
	//changes replacement word/phrase to replicate capitalization of original word/phrase
	//parameters are as follows:
		//text = word/phrase to be modified
		//caps = array of ints indicating the capitalization of the original word/phrase
	public static String changeCaps(String text, int [] caps) {	
		//splits phrase into individual words
		String [] words = text.split(" ");
		//creates string to contain the modified word
		String newWord = "";
		
		//repeats for each word
		for (int i = 0; i < words.length; i++) {
			//stores value inside an index of words into a string
			String word = words[i];
			
			try {
				if (caps [i] == 2) {									//if all caps
					word = word.toUpperCase();							//change to all caps
				} else if (caps [i] == 1) {								//if first letter is caps
					word = word.toLowerCase();							//changes all to lower case
					char [] letters = word.toCharArray();				//changes string to char array
					letters[0] = Character.toUpperCase(letters[0] );	//changes first letter to caps
					word = new String (letters);						//changes array back into string
				} else if (caps [i] == 0) {								//if no caps
					word = word.toLowerCase();							//change all to lower case
				}
			} catch (ArrayIndexOutOfBoundsException e) {				//if replacement phrase is longer than keyword
				if (caps[0] == 2) {										//use caps from first word
					word = word.toUpperCase();
				} else if (caps [0] == 1) {	
					word = word.toLowerCase();
					char [] letters = word.toCharArray();
					letters[0] = Character.toUpperCase(letters[0] );
					word = new String (letters);
				} else if (caps [0] == 0) {
					word = word.toLowerCase();
				}
			}
			//builds new words
			newWord += " " + word;
		}
		//returns word after modifications
		return newWord;
	}
	

	//main function of program
    public static void main(String... args) throws TwitterException{
    	//allows program to access twitterbot's account
    	Twitter twitter = TwitterFactory.getSingleton();
    	
		//2D array of strings that contain keywords and its replacement
		String [] [] keywords = { {"Trump", "Drumpf"}, {"I", "I, Donald Drumpf,"}, {"White House", "Bigly House"}, 
				{"illegal", "totally legal"}, {"rigged", "fair"}, {"Republican Party", "Minority Group"},
				{"Make America Great Again", "Make America White Again"}, {"leaks", "totally legal leaks done by my subordinates"},
				{"bot", "robot"} };
    	
    	// The name of the text file to open.
        String fileName = "History.txt";
    	
        //checks for all users that have a pending follower request to the bot account, and gets their IDs
    	IDs pendingFollowers = twitter.getIncomingFriendships(-1);
    	//stores IDs in a long array
    	long [] pendingFollowersIDs = pendingFollowers.getIDs();
    	//repeats for each pending follower request
    	for (int i = 0; i < pendingFollowersIDs.length; i++) {
    		//has bot follow the user that sends the request
    		twitter.createFriendship(pendingFollowersIDs[i]);
    	}
    	
    	//gets IDs of all of the people the bot is following
    	IDs friends = twitter.getFriendsIDs("BotV1_18_19_5", -1);
    	//stores IDs in long array
    	long [] friendIDs = friends.getIDs();
    	//creates a string that will contain all the users the bot is following
    	String friendNames = "Checking tweets from: ";
    	//repeats for each person the bot is following
    	for (int i = 0; i < friendIDs.length; i++) {
    		//gets user information of the friend through their ID
    		User user = twitter.showUser(friendIDs[i]);
    		//adds on the user's screen name into the string
    		friendNames += user.getScreenName() + ", ";
    	}
    	
    	//gets rid of the extra comma and the space at the end
    	char [] sentenceChars = friendNames.toCharArray();
    	friendNames = "";
    	for (int i = 0; i < sentenceChars.length - 2; i++) {
    		friendNames += sentenceChars[i];
    	}
    	//lets the user know who the bot is checking tweets from
    	System.out.println(friendNames);
    	
    	//creates array list to contain all the tweets that the bot finds
    	List<String> tweets = new ArrayList<String>();
    	
    	//repeats for each person the bot is following
    	for (int i = 0; i < friendIDs.length; i++) {
    		//gets user information through their ID
	    	User user = twitter.showUser(friendIDs[i]);
    		
	    	//creates array list to contain the status of the user
	    	List<Status> status = new ArrayList<Status>();
	    	
	    	//creates paging object
	    	//first parameter is the page number, second parameter is the amount per page
	    	Paging page = new Paging(1, 100);
	    	//adds all the statuses from the user (most recent 100) into the status array list
	    	status.addAll(twitter.getUserTimeline(friendIDs[i], page) );
	    	
	    	//repeats for each of statuses of user
	    	for (int j = 0; j < status.size(); j++) {
	    		//stores one element of array list into Status variable
	    		Status tweet = status.get(j);
	    		//adds onto the tweets array list; adds the following
	    			//the actual tweet of the status
	    			//the screen name of the sender
	    		//seperated by ",,,,,"
	    		tweets.add(tweet.getText() + ",,,,," + friendIDs[i]);
	    		
	    		//calls checkDuplicates and stores result into string
	    		String refinedTweet = checkDuplicates(fileName, tweets.get(tweets.size()-1) );
	    		if (refinedTweet != null) {
	    			//tweet is new
	    			//modifies tweet using keywords
	    			refinedTweet = replaceWords(keywords, refinedTweet, user.getScreenName());
	    			//checks limit of modified tweet and tweets it
	    			checkLimit(refinedTweet, twitter);
	    		} else {
	    			//tweet is a duplicate
	    			//prints to the console to let user know
	    			System.out.println("duplicate tweet");
	    		}
	    	}
    	}
    	
	}

}
