package org.davelogapps.cineconcertmanager.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Data;
import org.davelogapps.cineconcertmanager.util.Constants;

@Data
public class PromoImageManager {
    private final ImageView promoImageView;

    public PromoImageManager(String imagePath) {
        promoImageView = new ImageView(new Image(imagePath));
        promoImageView.setFitWidth(Constants.SCREEN_WIDTH);
        promoImageView.setFitHeight(Constants.SCREEN_HEIGHT);
    }

    public Pane getImagePane() {
        return new Pane(promoImageView);
    }

    public void showImage() {
        promoImageView.setVisible(true);
    }

    public void hideImage() {
        promoImageView.setVisible(false);
    }
}
