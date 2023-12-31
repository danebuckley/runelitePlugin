package com.pluginTest;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayRenderer;
import java.awt.event.KeyEvent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Wordle"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	private int numGuesses = 0;
	public String curWord = "";
	public char[] chosenWordArray = {};

	int winCondition = 0;

	@Override
	protected void startUp() throws Exception
	{
		pickRandomWord();
	}

	@Override
	protected void shutDown() throws Exception
	{

	}

	private void pickRandomWord() throws IOException {
		//TODO: ADD A BUNCH OF OSRS WORDS TO THE TEXT FILE, WOULD BE FUN TO HAVE A MORE CUSTOM DOCUMENT

		java.util.List<String> allWords = Files.readAllLines(Paths.get("C:\\Users\\DaneB\\IdeaProjects\\runelitePluginTest\\src\\main\\java\\com\\pluginTest\\wordleList.txt"));
		String randomWord = getRandomWord(allWords);
		curWord = randomWord.toUpperCase();
		chosenWordArray = curWord.toCharArray();
	}

	private static String getRandomWord(java.util.List<String> words) {
		Random random = new Random();
		int randomIndex = random.nextInt(words.size());
		return words.get(randomIndex);
	}
	
	@Subscribe
	private void onChatMessage(ChatMessage event) throws IOException {
		String message = event.getMessage();

		if (message.startsWith("!Guess")) {
			extractGuess(message);
		}
	}

	private void extractGuess(String message) throws IOException {
		Pattern pattern = Pattern.compile("!Guess\\s+(\\w+)");
		Matcher matcher = pattern.matcher(message);

		if (matcher.matches()) {
			if (numGuesses > 3) { //I have no idea why this is 3, but it works.
				sendRedMessage("You've lost! Sorry!");
				sendYellowMessage("The word was " + curWord);
				pickRandomWord();
				numGuesses = 0;
			} else {
				String guess = matcher.group(1).toUpperCase();
				char[] curGuess = guess.toCharArray();

				if (curGuess.length != 5) {
					sendRedMessage("Your guess must be 5 letters long. Please try again.");
				} else {
					//TODO:
					//handle the duplicate scenario
					//example: TWINE -> TWEEN (MAKES TWO YELLOW E'S EVEN THOUGH IT ONLY NEEDS ONE)


					ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder();
					int found = 0;
					int curJ = 0;
					for (int i = 0; i < 5; i++) {
						for (int j = curJ; j < 5; j++) {
							if (curGuess[i] == chosenWordArray[j]) {
								if (i == j) {
									//print curGuess[i] green
									chatMessageBuilder.append(Color.GREEN, String.valueOf(curGuess[i]) + " ");
									found++;
									winCondition++;
									curJ++;
									j = 5;
								} else {
									//print curGuess[i] as yellow
									chatMessageBuilder.append(Color.YELLOW, String.valueOf(curGuess[i]) + " ");
									found++;
									j = 5;
								}
							}
						}
						if (found == 0) {
							chatMessageBuilder.append(Color.RED, String.valueOf(curGuess[i]) + " ");
						}

						found = 0;
					}

					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);

					if (winCondition == 5) {
						sendGreenMessage("Congratulations, you have completed today's OSRS Wordle!");
						//reset the game here
						winCondition = 0;
						numGuesses = 0;
						curJ = 0;
						pickRandomWord();
					} else {
						numGuesses++;
						winCondition = 0;
						curJ = 0;
					}
				}
			}
		}
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Welcome to Wordle!", null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please enter a 5 letter word!", null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", curWord, null);
		}
	}

	private void sendRedMessage(String message) {
		ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append(Color.RED, message); //Set the color for the rest of the message

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
	}

	private void sendYellowMessage(String message) {
		ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append(Color.YELLOW, message); //Set the color for the rest of the message

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
	}

	private void sendGreenMessage(String message) {
		ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append(Color.GREEN, message); //Set the color for the rest of the message

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
