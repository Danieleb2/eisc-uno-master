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

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
        this.observers = new ArrayList<>();
        this.isRunning = true;
        this.lastPlayedCard = null;
    }

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

    public boolean validatePlay() {
        Card currentCardOnTheTable = this.table.getCurrentCardOnTheTable();
        if (currentCardOnTheTable.getValue().equals("+2") && !currentCardOnTheTable.equals(lastPlayedCard)) {
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

    public void drawCard(int cards){
        for (int i=0;i<cards;i++){
            this.machinePlayer.addCard(new Deck().takeCard());
        }
    }

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

    private Card getValidCardToPlay() {
        for (Card card : machinePlayer.getCardsPlayer()) {
            if (validCardToPlay(card)) {
                return card;
            }
        }
        return null;
    }

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
    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }

    public boolean hasPlayerPlayed() {
        return hasPlayerPlayed;
    }

    public void stopThread() {
        isRunning = false;
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        System.out.println("ThreadPlayMachine: Notificando a los observadores...");
        for (Observer observer : observers) {
            observer.update(isRunning);
        }
    }
}