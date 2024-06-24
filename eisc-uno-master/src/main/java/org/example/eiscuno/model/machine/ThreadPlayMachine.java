package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.ObserverPatron.Observer;
import org.example.eiscuno.model.ObserverPatron.Subject;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.util.ArrayList;
import java.util.List;

public class ThreadPlayMachine extends Thread implements Subject {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private List<Observer> observers;
    private volatile boolean isRunning;
    private Card lastPlayedCard;
    private Card lastHumanCard;
    /**
     * Constructs a ThreadPlayMachine object with the specified parameters.
     *
     * @param table           The table where the game is played.
     * @param machinePlayer   The player controlled by the machine.
     * @param tableImageView  The ImageView representing the current table card.
     */
    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
        this.observers = new ArrayList<>();
        this.isRunning = true;
        this.lastPlayedCard = null;
    }

    /**
     * Main operational method for the thread. Controls the gameplay loop for the machine player.
     */
    public void run() {
        while (isRunning) {
            if (hasPlayerPlayed) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (validatePlay()) {
                    putCardOnTheTable();
                }
                hasPlayerPlayed = false;
                notifyObservers(); // Notificar observadores después de que la máquina ha jugado
            }
        }
    }

    /**
     * Validates if the machine player can make a play based on the current card on the table.
     *
     * @return true if the machine can play a card, false otherwise.
     */
    public boolean validatePlay() {
        Card currentCardOnTheTable = this.table.getCurrentCardOnTheTable();

        // Verificar si la carta en la mesa es "+2" o "+4" y corresponde a lastHumanCard
        if ((currentCardOnTheTable.getValue().equals("+2") || currentCardOnTheTable.getValue().equals("+4")) &&
                !currentCardOnTheTable.equals(lastPlayedCard) && currentCardOnTheTable.equals(lastHumanCard)) {
            // No comer más cartas si la última carta en la mesa es la misma que jugó el humano
            return true;
        } else if (currentCardOnTheTable.getValue().equals("+2") && !currentCardOnTheTable.equals(lastPlayedCard)) {
            drawCard(2);
            System.out.println("The machine player has " + machinePlayer.getCardsPlayer().size() + " cards");
            return false;
        } else if (currentCardOnTheTable.getValue().equals("+4") && !currentCardOnTheTable.equals(lastPlayedCard)) {
            drawCard(4);
            System.out.println("The machine player has " + machinePlayer.getCardsPlayer().size() + " cards");
            return false;
        } else if (currentCardOnTheTable.getValue().equals("SKIP") && !currentCardOnTheTable.equals(lastPlayedCard)) {
            return false;
        }

        // Si la carta en la mesa no es +2, +4 o SKIP, verificar si hay una carta válida para jugar
        Card validCard = getValidCardToPlay();
        if (validCard == null) {
            // Si no hay carta válida, tomar una del mazo
            putCardOnTheTable();
            return false;
        } else {
            //Hay una carta válida para jugar
            return true;
        }
    }
    /**
     * Draws a specified number of cards from the deck and adds them to the machine player's hand.
     *
     * @param cards The number of cards to draw.
     */
    public void drawCard(int cards) {
        for (int i = 0; i < cards; i++) {
            this.machinePlayer.addCard(new Deck().takeCard());
        }
    }

    /**
     * Plays a valid card from the machine player's hand onto the table.
     * Updates the table image and removes the card from the player's hand.
     */
    public void putCardOnTheTable() {
        Card card = getValidCardToPlay();

        if (card != null) {
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.getCardsPlayer().remove(card);
            lastPlayedCard = card;
            System.out.println("The machine player has " + machinePlayer.getCardsPlayer().size() + " cards");
            System.out.println("Se añadió " + tableImageView.getImage());

        } else {
            Card drawnCard = new Deck().takeCard();
            if (validCardToPlay(drawnCard)) {
                table.addCardOnTheTable(drawnCard);
                tableImageView.setImage(drawnCard.getImage());
                lastPlayedCard = drawnCard;

            } else {
                machinePlayer.addCard(drawnCard);
                System.out.println("The machine player has " + machinePlayer.getCardsPlayer().size() + " cards");
            }
        }
    }
    /**
     * Retrieves a valid card from the machine player's hand that can be played on the current table card.
     *
     * @return A valid card to play, or null if no valid card is found.
     */
    private Card getValidCardToPlay() {
        for (Card card : machinePlayer.getCardsPlayer()) {
            if (validCardToPlay(card)) {
                return card;
            }
        }
        return null;
    }
    /**
     * Checks if a specific card can be played on the current table card.
     *
     * @param card The card to check for validity.
     * @return true if the card can be played, false otherwise.
     */
    private boolean validCardToPlay(Card card) {
        Card cardOnTable = table.getCurrentCardOnTheTable();
        if (cardOnTable.equals(lastPlayedCard)) {
            return true;  // Puede jugar cualquier carta si es la misma que lastPlayedCard
        } else if (card.getValue().equals("WILD") || card.getValue().equals("+4")) {
            return true;  // Puede jugar carta WILD o +4 en cualquier situación
        } else {
            return card.getColor().equals(cardOnTable.getColor()) || card.getValue().equals(cardOnTable.getValue());
        }
    }
    /**
     * Sets whether the machine player has played a card.
     *
     * @param hasPlayerPlayed true if the machine has played a card, false otherwise.
     */
    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }
    /**
     * Checks if the machine player has played a card.
     *
     * @return true if the machine has played a card, false otherwise.
     */
    public boolean hasPlayerPlayed() {
        return hasPlayerPlayed;
    }
    /**
     * Stops the thread by setting the isRunning flag to false.
     */
    public void stopThread() {
        isRunning = false;
    }
    /**
     * Retrieves the last card played by the machine player.
     *
     * @return The last card played by the machine player.
     */
    public Card getLastPlayedCard() {
        return lastPlayedCard;
    }
    /**
     * Adds an observer to be notified of changes in the thread's state.
     *
     * @param observer The observer to add.
     */
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    /**
     * Removes an observer from the list of observers.
     *
     * @param observer The observer to remove.
     */
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    /**
     * Notifies all registered observers that a state change has occurred.
     */
    @Override
    public void notifyObservers() {
        System.out.println("ThreadPlayMachine: Notificando a los observadores...");
        for (Observer observer : observers) {
            observer.update(isRunning);
        }
    }
}