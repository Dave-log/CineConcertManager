package org.davelogapps.cineconcertmanager.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Data;
import org.davelogapps.cineconcertmanager.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class PromoImageManager {
    private final List<Image> promoImages = new ArrayList<>();
    private int currentIndex = 0;

    public PromoImageManager(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                promoImages.add(new Image(file.toURI().toString()));
            }
        }

        if (promoImages.isEmpty()) {
            System.err.println("Aucun habillage trouvé dans : " + directoryPath);
        }
    }

    public Pane getNextPromoPane() {
        if (promoImages.isEmpty()) return new Pane();

        Image image = promoImages.get(currentIndex);
        currentIndex = (currentIndex + 1) % promoImages.size();

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setFitWidth(Constants.SCREEN_WIDTH);
        imageView.setFitHeight(Constants.SCREEN_HEIGHT);

        Pane pane = new Pane(imageView);
        imageView.fitWidthProperty().bind(pane.widthProperty());
        imageView.fitHeightProperty().bind(pane.heightProperty());

        return pane;
    }
}
