package org.example.eiscuno.model.game;

import javafx.stage.Stage;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
/**
 * This class tests the GameUno functionality using JUnit5 and TestFX.
 */
class GameUnoTest extends ApplicationTest {
    /**
     * This method is required by TestFX to set up the JavaFX environment.
     *
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage){}
    /**
     * Tests the functionality of the GameUno class.
     */
    @Test
    public void test() {
        // Create human and machine players
        Player humanPlayer = new Player("HUMAN_PLAYER");
        Player machinePlayer = new Player("MACHINE_PLAYER");
        // Create a deck and a table
        Deck deck = new Deck();
        Table table = new Table();
        // Create a GameUno instance
        GameUno myGameUno = new GameUno(humanPlayer, machinePlayer, deck, table);
        // Each player draws 8 cards
        myGameUno.eatCard(humanPlayer, 8);
        myGameUno.eatCard(machinePlayer, 8);
        // Get the number of cards each player has
        int playerInitialCards = humanPlayer.getCardsPlayer().size();
        int machineInitialCards = machinePlayer.getCardsPlayer().size();
        // Assert that both players have the same number of cards
        Assertions.assertEquals(playerInitialCards, machineInitialCards);
    }
}