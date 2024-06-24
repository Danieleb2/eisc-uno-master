package org.example.eiscuno.controller;


import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.example.eiscuno.model.ObserverPatron.Observer;
import org.example.eiscuno.model.WinnerAlert.Winner;
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

    private boolean unoButtonPressed = false;
    private PauseTransition unoTimer;
    private Card lastPlayedCardByHuman;

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

        updateMachineCards(4); // Suponiendo que empiezas con 6 cartas

        // Inicializar el temporizador
        unoTimer = new PauseTransition(Duration.seconds(2));
        unoTimer.setOnFinished(event -> {
            if (!unoButtonPressed) {
                addTwoCardsToHumanPlayer();
            }
        });
    }

    private void addTwoCardsToHumanPlayer() {
        gameUno.eatCard(humanPlayer, 2);
        printCardsHumanPlayer();
        System.out.println("No se presionó UNO a tiempo. Se agregaron dos cartas.");
    }

    public void humanPressUnoButton() {
        unoButtonPressed = true;
    }

    public void startUnoTimer() {
        unoButtonPressed = false;
        unoTimer.playFromStart();
    }

    private void handleSalirButtonAction(ActionEvent event) {
        closeGame();
    }

    public void closeGame() {
        System.exit(0);
    }

    @FXML
    private void onHandleTakeCard(ActionEvent event) {
        if (threadPlayMachine.hasPlayerPlayed()) {
            System.out.println("you cannot take a card");
        } else {
            this.threadPlayMachine.setHasPlayerPlayed(true);
            System.out.println("Se precionó el boton");
            humanPlayer.addCard(deck.takeCard());
            printCardsHumanPlayer();
        }
    }

    @FXML
    private void onHandleUno(ActionEvent event) {
        if (machinePlayer.getCardsPlayer().size() == 1) {
            gameUno.eatCard(machinePlayer, 2);
            System.out.println("Machine has " + machinePlayer.getCardsPlayer().size() + " cards");
        }
        if (humanPlayer.getCardsPlayer().size() == 1) {
            unoButtonPressed = true;
            System.out.println("UNO presionado a tiempo.");
            if (unoTimer.getStatus() == PauseTransition.Status.RUNNING) {
                unoTimer.stop();
            }
        }
        humanPressUnoButton();
    }

    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
        this.lastPlayedCardByHuman = null;
    }

    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                System.out.println("Card with value " + card.getValue());
                try {
                    if (canPlayCard(card)) {
                        gameUno.playCard(card);
                        tableImageView.setImage(card.getImage());
                        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        threadPlayMachine.setHasPlayerPlayed(true);
                        lastPlayedCardByHuman = card;
                        printCardsHumanPlayer();
                    } else {
                        System.out.println("No puedes colocar esa carta");
                    }
                } catch (IndexOutOfBoundsException e) {
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                    threadPlayMachine.setHasPlayerPlayed(true);
                    lastPlayedCardByHuman = card;
                    printCardsHumanPlayer();
                }
            });
            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
        if (humanPlayer.getCardsPlayer().size() == 1) {
            startUnoTimer();
        }
        showWinner();
    }


    /**
     * Verifies if the card matches the conditions in order to be played
     * @param card is the card to analise
     * @return true if the card can be played, false if the card can´t be played
     */
    private boolean canPlayCard(Card card) {
        Card currentCardOnTheTable = this.table.getCurrentCardOnTheTable();

        // La carta "+2" solo puede ser seguida por otra "+2"
        if (currentCardOnTheTable.getValue().equals("+2") && !currentCardOnTheTable.equals(lastPlayedCardByHuman) && !card.getValue().equals("+2")) {
            return false;
        }

        // La carta "SKIP" solo puede ser seguida por otra "SKIP"
        if (currentCardOnTheTable.getValue().equals("SKIP") && !currentCardOnTheTable.equals(lastPlayedCardByHuman) && !card.getValue().equals("SKIP")) {
            return false;
        }

        // La carta "WILD" es válida para cualquier jugada y permite que cualquier carta la siga
        if (currentCardOnTheTable.getValue().equals("WILD") || card.getValue().equals("WILD")) {
            return true;
        }

        // Si la carta actual en el tablero coincide con la última carta jugada, cualquier carta del mismo color o valor es válida
        if (currentCardOnTheTable.equals(lastPlayedCardByHuman)) {
            return true;
        }

        // Cualquier carta con el mismo valor o color es válida
        return currentCardOnTheTable.getValue().equals(card.getValue())
                || currentCardOnTheTable.getColor().equals(card.getColor())||card.getValue().equals("+4");
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
        for (int i = 0; i < 4; i++) {
            ImageView cardBack = new ImageView(new Image(getClass().getResourceAsStream(CARD_BACK_IMAGE_PATH)));
            cardBack.setFitHeight(110); // Ajusta según el tamaño deseado
            cardBack.setFitWidth(100); // Ajusta según el tamaño deseado
            gridPaneCardsMachine.add(cardBack, i, 0); // Coloca la carta en el GridPane
        }
    }

    public void showWinner() {
        if (this.humanPlayer.getCardsPlayer().size() == 0) {
            unoTimer.stop();
            String tittle = "WINNER";
            String header = "";
            String content = "¡Felicidades has ganado el juego!";
            Winner alertBox = new Winner();
            alertBox.showMessageWinner(tittle, header, content);
            closeGame();
        }
    }

    @Override
    public void update(boolean isThreadRunning) {
        deckButton.setDisable(false);
        unoButton.setDisable(false);
    }
}
