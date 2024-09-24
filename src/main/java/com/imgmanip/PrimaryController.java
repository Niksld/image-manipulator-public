package com.imgmanip;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.MenuItem;

public class PrimaryController {
    protected int timesFlipped; // počet otočení obrázku
    protected double saturation;
    ColorAdjust clrAdjust = new ColorAdjust();

    @FXML
    private ImageView imgView; // Image panel kde se zobrazuje obrázek
    @FXML
    private Image loadedImage; // původní obrázek
    @FXML
    private Image modifiedImage;
    @FXML
    private Button imageFileButton;
    @FXML
    private MenuItem exitButton;

    @FXML
    protected void originalImage() {
        imgView.setImage(loadedImage);
        ColorAdjust clrAdjust = new ColorAdjust();
        clrAdjust.setSaturation(0);
        imgView.setEffect(clrAdjust);
        imgView.setRotate(0);
    }

    @FXML
    protected void modifiedImage() {
        imgView.setImage(modifiedImage);
        clrAdjust.setSaturation(saturation);
        imgView.setEffect(clrAdjust);
        imgView.setRotate(180 * timesFlipped);
    }

    @FXML
    protected void pickImage() {
        Window window = this.imageFileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");

        // Nastavení filtru obrázků
        FileChooser.ExtensionFilter extFilterAll = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterPNG, extFilterJPG, extFilterAll);

        // Spuštění FileChooseru
        File selectedFile = fileChooser.showOpenDialog(window);

        // Načtění obrázku do loadedImage
        if (selectedFile != null) {
            String path = selectedFile.toURI().toString();
            loadedImage = new Image(path);
            modifiedImage = loadedImage;
            imgView.setImage(loadedImage);
        }
        exitButton.setDisable(false);
    }

    @FXML
    protected boolean saveImage() {
        // Kontrola jestli je potřeba vůbec něco ukládat -> pokud není žádný obrázek
        // načtený tak nic neukládáme
        if (loadedImage == null) {
            return true;
        }

        Window window = imgView.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        // Nastavení filtrů pro souboru
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        // Zobrazení dialogu
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try {
                // Převod JavaFx.Image na BufferedImage
                // Jelikož jsme měli potíže s dalšíma packagema jako swing atd tak
                // jsme museli využít tuto metodu, jinak má swing přímo funkci pro konvertování
                // JavaFX.Image na Image
                Image image = imgView.getImage();

                int width = (int) image.getWidth();
                int height = (int) image.getHeight();

                // Rekonstrukce obrázku v imageView do BufferedImage... pixel po pixelu...
                PixelReader pixelReader = image.getPixelReader();
                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int argb = pixelReader.getArgb(x, y);
                        bufferedImage.setRGB(x, y, argb);
                    }
                }

                // Uložení obrázku do souboru.
                // BUG #1: .bmp nefunguje - netuším proč
                String format = "jpg";
                String fileName = file.getName();

                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    format = "jpg";
                } else if (fileName.endsWith(".png")) {
                    format = "png";
                } else if (fileName.endsWith(".bmp")) {
                    format = "bmp";
                }

                ImageIO.write(bufferedImage, format, file);
                return true;
            } catch (IOException e) {
                // Chybová hláška, pokud se ukládání nepodaří.
                Alert a = new Alert(AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("A Read/Write Exception has occured.");
                a.setContentText("Exception cause:\n" + e.getCause());
                a.showAndWait();
            }
        }
        return false;
    }

    @FXML
    protected void negativeFilter() { // Inverzní filtr
        Image image = loadedImage;
        PixelReader reader = image.getPixelReader();
        if (loadedImage != modifiedImage) {
            reader = modifiedImage.getPixelReader();
        }

        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        // Přepsání každého pixelu po každém pixelu a inverze.
        // Opět limitace JavaFX.Image - Rewrite maybe?
        WritableImage wImage = new WritableImage(w, h);
        PixelWriter writer = wImage.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x, y, color.invert());
            }
        }
        modifiedImage = wImage;
        imgView.setImage(wImage);
    }

    @FXML
    protected void mirrorImage() { // Zrcadlení
        imgView.setRotationAxis(Rotate.Y_AXIS);
        imgView.setRotate(180 * ++timesFlipped);
    }

    @FXML
    protected void desaturate() { // Desaturace
        saturation -= 0.2;
        clrAdjust.setSaturation(saturation);
        imgView.setEffect(clrAdjust);
    }

    @FXML
    protected void saturate() { // Saturace
        saturation += 0.2;
        clrAdjust.setSaturation(saturation);
        imgView.setEffect(clrAdjust);
    }

    @FXML
    protected void exit() { // řesí exit programu
        if (loadedImage != null) { // pokud jsme nic nenačetli, není třeba se ptát a můžeme jít pryč
            var result = showSaveAndQuitDialog().get().getButtonData();
            if (result != null) {
                if (result == ButtonBar.ButtonData.YES) { // Tlačítko ULOŽIT (Save)
                    if (saveImage()) { // vrací true pouze pokud se obrázek úspěšně uložil
                        System.exit(0);
                    }
                } else if (result == ButtonBar.ButtonData.NO) { // Pokud nechceme uložit, můžeme jít rovnou pryč.
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        }
    }

    @FXML
    protected void exitWithoutSaving() {
        System.exit(0);
    }

    @FXML
    protected void saveAndExit() {
        if (saveImage()) {
            System.exit(0);
        }
    }

    private static Optional<ButtonType> showSaveAndQuitDialog() { // Pomocná metoda pro dialog okno při odcházení s
                                                                  // neuloženým obrázkem
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Save before exit?");
        alert.setContentText("Do you want to save the current image before exiting?");
        // Vytvoření a přidání tlačítek do dialogu
        ButtonType save = new ButtonType("Save and Exit", ButtonBar.ButtonData.YES);
        ButtonType dontSave = new ButtonType("Exit without saving", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(save, dontSave, ButtonType.CANCEL);
        return alert.showAndWait();
    }

    private static Optional<ButtonType> AboutDialog() { // Pomocná metoda pro dialog okno About us
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setHeaderText("About us");
        alert.setContentText(
                "The Team:\n\n\tMichaelCZE\t\tTeam lead\n\tVenca1450\t\tLead programmer\n\tNiksld\t\t\tUI/UX designer\n\tGrimReapTM\t\tDocumentation, programmer");
        alert.getButtonTypes().setAll(ButtonType.OK);
        return alert.showAndWait();
    }

    @FXML
    protected void showAboutDialog() {
        AboutDialog();
    }
}
