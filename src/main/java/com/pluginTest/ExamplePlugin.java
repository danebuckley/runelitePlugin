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
import java.nio.file.Files;
import java.nio.file.Paths;
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

	@Override
	protected void startUp() throws Exception
	{
		java.util.List<String> allWords = Files.readAllLines(Paths.get("C:\\Users\\DaneB\\IdeaProjects\\runelitePluginTest\\src\\main\\java\\com\\pluginTest\\wordleList.txt"));
		String randomWord = getRandomWord(allWords);
		curWord = randomWord;
	}

	@Override
	protected void shutDown() throws Exception
	{

	}

	private static String getRandomWord(java.util.List<String> words) {
		Random random = new Random();
		int randomIndex = random.nextInt(words.size());
		return words.get(randomIndex);
	}
	
	@Subscribe
	private void onChatMessage(ChatMessage event) {
		String message = event.getMessage();

		if (message.startsWith("!Guess")) {
			extractGuess(message);
			sendMessageWithColor(curWord);
		}
	}

	private void extractGuess(String message) {
		Pattern pattern = Pattern.compile("!Guess\\s+(\\w+)");
		Matcher matcher = pattern.matcher(message);

		if (matcher.matches()) {
			if (numGuesses > 3) {
				sendMessageWithColor("You've lost! Sorry!");
				numGuesses = 0;
			} else {
				String guess = matcher.group(1);

				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", guess, null);
				numGuesses++;
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
		}
	}

	private void sendMessageWithColor(String message) {
		ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append(Color.RED, message); //Set the color for the rest of the message

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
