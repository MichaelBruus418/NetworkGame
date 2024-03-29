package client.gui;

import client.model.GameEngine;
import client.model.Player;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;

public class ConnectScene {

    private GameEngine gameEngine;
    private String presetIp;
    private String presetName;

    // --- Input grid elements ---
    private TextField txtfIp;
    private TextField txtfName;
    private Button btnConnect;
    private Button btnStart;

    // --- Message grid elements ---
    private GridPane messageGrid;
    private Label lblMessage;
    private Label lblValue;

    // --- Player grid elements ---
    private GridPane playerGrid;

    public ConnectScene(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.presetIp = "10.10.138.22";
        this.presetName = "Mig";
    }

    public Scene createScene() {
        // --- Master Grid ---
        GridPane masterGrid = new GridPane();
        Scene scene = new Scene(masterGrid);
        masterGrid.setHgap(10);
        masterGrid.setVgap(10);
        masterGrid.setPadding(new Insets(0));
        masterGrid.getStylesheets().add("/client/gui/connectScene.css");
        masterGrid.getStyleClass().add("masterGrid");
        masterGrid.setGridLinesVisible(false);
        masterGrid.setAlignment(Pos.TOP_CENTER);

        // --- Add sub-grids ---
        masterGrid.add(this.createInputGrid(), 0, 0);
        this.messageGrid = this.createMessageGrid();
        masterGrid.add(this.messageGrid, 0, 1);
        this.playerGrid = this.createPlayerGrid();
        masterGrid.add(this.playerGrid, 0, 2);

        return scene;
    }

    public void updatePlayers() {
        // Remove all nodes exept legends from players grid.
        int cols = this.playerGrid.getColumnCount();
        int size = this.playerGrid.getChildren().size();
        if (size > cols) {
            this.playerGrid.getChildren().remove(cols, size);
        }

        HashMap<Integer, Player> players = this.gameEngine.getPlayers();
        int clientId = this.gameEngine.getPlayerId();

        // Make sure this player is first
        this.addPlayerRow(players.get(clientId));

        // --- Add row pr. player ---
        for (Player p : players.values()) {
            if (p.getPlayerId() != clientId) {
                this.addPlayerRow(p);
            }
        }
    }

    public void updateCountdown(int countdown) {
        this.messageGrid.getStyleClass().add("countdown");
        this.lblMessage.setText("Starting game in:");
        this.lblValue.setText(" " + String.valueOf(countdown));
    }

    private GridPane createInputGrid() {
        // --- Grid ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0));
        grid.getStyleClass().add("inputGrid");
        grid.setGridLinesVisible(false);
        grid.setAlignment(Pos.TOP_CENTER);

        RowConstraints rowA = new RowConstraints();
        RowConstraints rowB = new RowConstraints();
        rowB.setPrefHeight(60);

        grid.getRowConstraints().addAll(rowA, rowA, rowB);

        // --- Game server IP ---
        Label lblIp = new Label("Game Server IP: ");
        grid.add(lblIp, 0, 0);
        this.txtfIp = new TextField(this.presetIp);
        grid.add(this.txtfIp, 1, 0);

        // --- Player name ---
        Label lblName = new Label("Player name: ");
        grid.add(lblName, 0, 1);
        this.txtfName = new TextField(this.presetName);
        grid.add(this.txtfName, 1, 1);

        // --- Connect btn ---
        this.btnConnect = new Button("Connect");
        this.btnConnect.getStyleClass().add("btnConnect");
        grid.add(this.btnConnect, 0, 2, 2, 1);
        this.btnConnect.setOnAction(event -> this.connectAction());
        GridPane.setHalignment(this.btnConnect, HPos.CENTER);

        // --- Start btn ---
        this.btnStart = new Button("Start Game");
        this.btnStart.getStyleClass().add("btnStart");
        grid.add(this.btnStart, 0, 3, 2, 1);
        this.btnStart.setOnAction(event -> this.readyAction());
        this.btnStart.setDisable(true);
        GridPane.setHalignment(this.btnStart, HPos.CENTER);

        return grid;
    }

    private GridPane createMessageGrid() {
        // --- Grid ---
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("messageGrid");
        grid.setGridLinesVisible(false);
        grid.setAlignment(Pos.TOP_CENTER);
        GridPane.setMargin(grid, new Insets(20, 0, 0, 0));

        // --- Elements ---
        this.lblMessage = new Label("Connected players");
        this.lblMessage.getStyleClass().add("lblMessage");
        grid.add(this.lblMessage, 0, 0);

        this.lblValue = new Label("");
        this.lblValue.getStyleClass().add("lblValue");
        grid.add(this.lblValue, 1, 0);

        return grid;
    }

    private GridPane createPlayerGrid() {
        // --- Grid ---
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("playerGrid");
        grid.setGridLinesVisible(false);
        GridPane.setMargin(grid, new Insets(10, 0, 0, 0));
        //grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(55);
        col2.setPercentWidth(20);
        col3.setPercentWidth(25);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        Pane pn;
        int col = 0;
        // --- Legends --
        pn = new Pane();
        Label lblName = new Label("Player");

        pn.getChildren().add(lblName);
        pn.getStyleClass().add("legendPane");
        grid.add(pn, col++, 0);

        pn = new Pane();
        Label lblColor = new Label("Color");
        pn.getChildren().add(lblColor);
        pn.getStyleClass().add("legendPane");
        grid.add(pn, col++, 0);

        pn = new Pane();
        Label lblStart = new Label("Ready");
        pn.getChildren().add(lblStart);
        pn.getStyleClass().add("legendPane");
        grid.add(pn, col++, 0);

        return grid;
    }

    private void addPlayerRow(Player p) {
        int row = this.playerGrid.getRowCount();
        GridPane grid = this.playerGrid; // Short hand.

        Pane pn;
        int col = 0;
        String parity = row % 2 == 0 ? "even" : "odd";
        // --- Player info ---
        pn = new Pane();
        Label lblName = new Label(p.getPlayerName());
        pn.getChildren().add(lblName);
        pn.getStyleClass().add("playerPaneRow");
        pn.getStyleClass().add(parity);
        grid.add(pn, col++, row);

        pn = new Pane();
        Label lblColor = new Label(this.capitalize(p.getColor()));
        pn.getChildren().add(lblColor);
        pn.getStyleClass().add("playerPaneRow");
        pn.getStyleClass().add(parity);
        pn.getStyleClass().add(p.getColor());
        grid.add(pn, col++, row);

        pn = new Pane();
        String state = p.isReady() ? "OK" : "Pending";
        Label lblState = new Label(state);
        pn.getChildren().add(lblState);
        pn.getStyleClass().add("playerPaneRow");
        pn.getStyleClass().add(parity);
        grid.add(pn, col++, row);
    }

    private void connectAction() {
        String serverIp = this.txtfIp.getText().trim();
        String playerName = this.txtfName.getText().trim();

        // Disable connection elements
        txtfIp.setDisable(true);
        txtfName.setDisable(true);
        btnConnect.setDisable(true);

        try {
            // Throws Exception on connection failure.
            this.gameEngine.connectAction(serverIp, playerName);
            // Connnection established.
            btnStart.setDisable(false);
        }
        catch (UnknownHostException | ConnectException e) {
            // --- IP adress unresolved/non-responsive ---
            Alert alert = AlertFactory.serverConnectFailure(e);
            alert.showAndWait();
            // Re-enable connection elements
            txtfIp.setDisable(false);
            txtfName.setDisable(false);
            btnConnect.setDisable(false);
        }
        catch (UnsupportedOperationException | IOException e) {
            // --- Something went wrong that we can not recover from ---
            // UnsupportedOperationException: Connection already established
            // IOException: Failure to create I/O streams
            Alert alert = AlertFactory.unrecoverableError(e);
            alert.showAndWait();
            System.exit(0);
        }
        catch (Exception e) {
             // Exception: For catching exceptions we didn't think of.
            Alert alert = AlertFactory.unrecoverableError(e);
            alert.showAndWait();
            System.exit(0);
        }

    }

    private void readyAction() {
        this.gameEngine.readyAction();
        btnStart.setDisable(true);
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

}
