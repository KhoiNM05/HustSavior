package io.github.HustSavior.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.HustSavior.utils.ResolutionManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ResolutionControl {
    private Label resolutionLabel;
    private TextButton btn1080p, btn900p, btn720p;
    private ResolutionManager resolutionManager;
    private Table parentTable;

    public ResolutionControl() {
        resolutionLabel = new Label("Resolution:", new Skin(Gdx.files.internal("UI/resolution/resolutionLabel.json")));
        Skin skin = new Skin(Gdx.files.internal("UI/resolution/resolutionLabel.json"));
        
        btn1080p = new TextButton("1920x1080", skin);
        btn900p = new TextButton("1600x900", skin);
        btn720p = new TextButton("1280x720", skin);
        
        resolutionManager = ResolutionManager.getInstance();
        
        setupButtonListeners();
    }

    public void addToTable(Table table) {
        this.parentTable = table;
        updateTableLayout();
    }

    private void updateTableLayout() {
        if (parentTable == null) return;
        
        parentTable.clear();
        parentTable.add().height(parentTable.getHeight() * 0.2f).row();
        parentTable.add(resolutionLabel).pad(20).colspan(3);
        parentTable.row();
        
        float buttonPad = 30;
        parentTable.add(btn1080p).width(250).height(60).pad(buttonPad);
        parentTable.add(btn900p).width(250).height(60).pad(buttonPad);
        parentTable.add(btn720p).width(250).height(60).pad(buttonPad);
        parentTable.row();
    }

    private void setupButtonListeners() {
        btn1080p.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resolutionManager.setResolution("1920x1080");
                updateTableLayout();
            }
        });
        
        btn900p.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resolutionManager.setResolution("1600x900");
                updateTableLayout();
            }
        });
        
        btn720p.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resolutionManager.setResolution("1280x720");
                updateTableLayout();
            }
        });
    }

    public String getCurrentResolution() {
        return resolutionManager.getCurrentResolution();
    }
}
