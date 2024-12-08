package io.github.HustSavior.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class GameTimer {
    private float totalTime;
    private final Label timerLabel;
    private final Table table;
    
    public GameTimer(Stage stage) {
        totalTime = 0;
        
        // Create label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont();
        
        // Create timer label
        timerLabel = new Label("00:00", labelStyle);
        timerLabel.setFontScale(2f);
        
        // Create table for positioning
        table = new Table();
        table.setFillParent(true);
        table.align(Align.topLeft);
        table.pad(20);
        table.add(timerLabel);
        
        stage.addActor(table);
    }
    
    public void update(float delta) {
        totalTime += delta;
        updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        int minutes = (int) (totalTime / 60);
        int seconds = (int) (totalTime % 60);
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }
    
    public void dispose() {
        timerLabel.getStyle().font.dispose();
    }
} 