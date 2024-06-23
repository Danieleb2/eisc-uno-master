package org.example.eiscuno.controller;


import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.example.eiscuno.model.Observer;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController implements Observer {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private Button salirButton;

    @FXML
    private Button deckButton;

    @FXML
    private Button unoButton;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    private static final String CARD_BACK_IMAGE_PATH = "/org/example/eiscuno/images/deck.png"; // Asegúrate de que esta ruta sea correcta

    @FXML
    public void initialize() {
        salirButton.setOnAction(this::handleSalirButtonAction);
        initVariables();
        this.gameUno.startGame();
        printCardsHumanPlayer();

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView);
        threadPlayMachine.addObserver(this);
        threadPlayMachine.start();

        updateMachineCards(7); // Suponiendo que empiezas con 7 cartas
    }

    private void handleSalirButtonAction(ActionEvent event) {
        closeGame();
    }
    public void closeGame(){
        System.exit(0);
    }

    /**
     * Handles the event of taking a card from the deck and passing the turn
     * @param event
     */
    @FXML
    private void onHandleTakeCard(ActionEvent event) {
        if(threadPlayMachine.hasPlayerPlayed()){
            System.out.println("you cannot take a card");
        }else {
            this.threadPlayMachine.setHasPlayerPlayed(true);
            System.out.println("Se precionó el boton");
            humanPlayer.addCard(deck.takeCard());
            printCardsHumanPlayer();
        }
    }

    @FXML
    private void onHandleUno(ActionEvent event) {
        if (threadPlayMachine.isAlive() && threadPlayMachine.hasPlayerPlayed()) {
            System.out.println("No puedes presionar UNO mientras la máquina está jugando.");
            return;
        }
        machinePlayer.addCard(this.deck.takeCard());
    }

    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                try {
                    if (canPlayCard(card)) {
                        gameUno.playCard(card);
                        tableImageView.setImage(card.getImage());
                        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        threadPlayMachine.setHasPlayerPlayed(true);
                        printCardsHumanPlayer();
                    } else {
                        System.out.println("No puedes colocar esa carta");
                        threadPlayMachine.setHasPlayerPlayed(true);
                    }
                } catch (IndexOutOfBoundsException e) {
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                    threadPlayMachine.setHasPlayerPlayed(true);
                    printCardsHumanPlayer();
                }
            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    /**
     * Makes sure that it is valid to play a card
     *
     * @param card Represents the card to valid
     * @return
     */
    private boolean canPlayCard(Card card) {
        Card currentCardOnTheTable = this.table.getCurrentCardOnTheTable();

        if (currentCardOnTheTable.getValue().equals("+2") && card.getValue().equals("+2")) {
            return true;
        }

        if(currentCardOnTheTable.getValue().equals("SKIP")|| currentCardOnTheTable.getValue().equals("+2")){
            return false;
        }

//        if (currentCardOnTheTable.getValue().equals("+4")
//                || currentCardOnTheTable.getValue().equals("SKIP")
//                || currentCardOnTheTable.getValue().equals("+2")) {
//            return false;
//        }

        return currentCardOnTheTable.getValue().equals(card.getValue())
                || currentCardOnTheTable.getColor().equals(card.getColor()) || currentCardOnTheTable.getValue().contains("WILD");
    }

    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    public void updateMachineCards(int numberOfCards) {
        gridPaneCardsMachine.getChildren().clear(); // Limpia el GridPane
        for (int i = 0; i < numberOfCards; i++) {
            ImageView cardBack = new ImageView(new Image(getClass().getResourceAsStream(CARD_BACK_IMAGE_PATH)));
            cardBack.setFitHeight(100); // Ajusta según el tamaño deseado
            cardBack.setFitWidth(80); // Ajusta según el tamaño deseado
            gridPaneCardsMachine.add(cardBack, i % 7, i / 7); // Coloca la carta en el GridPane
        }
    }

    @Override
    public void update(boolean isThreadRunning) {

        deckButton.setDisable(false);
        unoButton.setDisable(false);


    }
}
