package gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class Header extends HBox {

    private Button btnDarkMode;
    private boolean isDarkMode = false;
    private final Runnable onThemeToggle;

    public Header(Runnable onThemeToggle) {
        this.onThemeToggle = onThemeToggle;
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10, 20, 10, 20));
        this.getStyleClass().add("header-bar");

        initComponents();
    }

    private void initComponents() {
        // Title on the left
        Text title = new Text("CalmEx");
        title.getStyleClass().add("header-title");

        // Spacer to push button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dark mode toggle button
        btnDarkMode = new Button("🌙");
        btnDarkMode.getStyleClass().add("dark-mode-toggle");
        btnDarkMode.setOnAction(e -> toggleTheme());

        this.getChildren().addAll(title, spacer, btnDarkMode);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        btnDarkMode.setText(isDarkMode ? "🌞" : "🌙");
        onThemeToggle.run();
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }
}