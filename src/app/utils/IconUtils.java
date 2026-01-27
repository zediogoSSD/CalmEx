package src.app.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.swing.filechooser.FileSystemView;
import javax.swing.Icon;
import java.awt.image.BufferedImage;
import java.io.File;

public class IconUtils {
    public static Image carregarIcone(String caminho) {
        try {
            if (caminho == null || caminho.isEmpty()) return null;
            File ficheiro = new File(caminho);
            if (!ficheiro.exists()) return null;

            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(ficheiro);
            BufferedImage bImg = new BufferedImage(
                    icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB
            );
            icon.paintIcon(null, bImg.getGraphics(), 0, 0);
            return SwingFXUtils.toFXImage(bImg, null);
        } catch (Exception e) {
            return null;
        }
    }
}