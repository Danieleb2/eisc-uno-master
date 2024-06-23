package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.Observer;
import org.example.eiscuno.model.Subject;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ThreadPlayMachine extends Thread implements Subject {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private List<Observer> observers;
    private volatile boolean isRunning;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
        this.observers = new ArrayList<>();
        this.isRunning = true;
    }

    public void run() {
        while (isRunning) {
            if (hasPlayerPlayed) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                putCardOnTheTable();
                hasPlayerPlayed = false;
                notifyObservers(); // Notificar observadores después de que la máquina ha jugado
            }
        }
    }

    public void putCardOnTheTable() {
        Card card = getValidCardToPlay();

        if (card != null) {
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.getCardsPlayer().remove(card);
            System.out.println("The machine player has "+machinePlayer.getCardsPlayer().size()+" cards");
            System.out.println("Se añadió " + tableImageView.getImage());
        } else {
            Card drawnCard = new Deck().takeCard();
            if (validCardToPlay(drawnCard)) {
                table.addCardOnTheTable(drawnCard);
                tableImageView.setImage(drawnCard.getImage());
            } else {
                machinePlayer.addCard(drawnCard);
                System.out.println("The machine player has "+machinePlayer.getCardsPlayer().size()+" cards");
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
        if (cardOnTable.getValue().contains("skip")) {
            return false;
        } else if (card.getColor().equals(cardOnTable.getColor()) || card.getValue().equals(cardOnTable.getValue()) || card.getColor().equals("NON_COLOR")) {
            return true;
        }
        return false;
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

