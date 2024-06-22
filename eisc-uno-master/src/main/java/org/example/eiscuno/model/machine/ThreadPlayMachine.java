package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.Observer;
import org.example.eiscuno.model.Subject;
import org.example.eiscuno.model.card.Card;
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
            }
        }
    }

    private void putCardOnTheTable() {
        int index = (int) (Math.random() * machinePlayer.getCardsPlayer().size());
        Card card = machinePlayer.getCard(index);

        System.out.println(card.getValue());
        System.out.println(card.getColor());

        if (this.table.getCurrentCardOnTheTable().getColor().equalsIgnoreCase(card.getColor())) {
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.removeCard(index);
        } else if (this.table.getCurrentCardOnTheTable().getValue().equalsIgnoreCase(card.getValue())) {
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.removeCard(index);
        } else {
            System.out.println("No puede colocar esta carta");
        }

        notifyObservers();
    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
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

