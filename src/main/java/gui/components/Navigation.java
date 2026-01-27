package src.app.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class Navigation extends HBox {

    private Button btnPrev, btnDetails, btnNext;
    private LocalDate currentViewWeek;
    private final Runnable onDateChanged;
    private final Runnable onDetailsClicked;

    public Navigation(Runnable onDateChanged, Runnable onDetailsClicked) {
        this.onDateChanged = onDateChanged;
        this.onDetailsClicked = onDetailsClicked;
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);

        initButtons();

        // Initial State
        this.currentViewWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        updateButtonsVisibility();
    }

    private void initButtons() {
        btnPrev = new Button("< Semana Anterior");
        btnPrev.getStyleClass().add("botao-grande");
        btnPrev.setOnAction(e -> {
            currentViewWeek = currentViewWeek.minusWeeks(1);
            updateButtonsVisibility();
            onDateChanged.run();
        });

        btnDetails = new Button("Detalhes");
        btnDetails.getStyleClass().add("botao-grande");
        btnDetails.setOnAction(e -> onDetailsClicked.run());

        btnNext = new Button("Semana Seguinte >");
        btnNext.getStyleClass().add("botao-grande");
        btnNext.setVisible(false);
        btnNext.setOnAction(e -> {
            currentViewWeek = currentViewWeek.plusWeeks(1);
            updateButtonsVisibility();
            onDateChanged.run();
        });

        this.getChildren().addAll(btnPrev, btnDetails, btnNext);
    }

    private void updateButtonsVisibility() {
        LocalDate realMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate pastLimit = realMonday.minusWeeks(3);

        btnPrev.setVisible(currentViewWeek.isAfter(pastLimit));
        btnNext.setVisible(currentViewWeek.isBefore(realMonday));
    }

    public LocalDate getCurrentDate() {
        return currentViewWeek;
    }
}