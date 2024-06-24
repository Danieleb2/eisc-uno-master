package org.example.eiscuno.model.WinnerAlert;

import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;

public class Winner implements IWinner{
    @Override
    public void showMessageWinner(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.setGraphic(new ImageView(String.valueOf(getClass().getResource("/org/example/eiscuno/images/imageWinner.jpg"))));
        alert.showAndWait();
    }
}
