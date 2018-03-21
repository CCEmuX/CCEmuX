package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

public class ConfigAdapter extends BaseGDXAdapter {
	private static final String WINDOW_TITLE = "CCEmuX Config";
	
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	
	private final EmuConfig config;
	
	private Stage stage;
	
	private VisTextButton backButton;
	private VisLabel pageLabel;
	
	public ConfigAdapter(GDXPlugin plugin, EmuConfig config) {
		super(plugin);
		this.config = config;
	}
	
	@Override
	void setupConfig(Lwjgl3WindowConfiguration config) {
		super.setupConfig(config);
		
		config.setResizable(false);
		config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
	}
	
	@Override
	public void create() {
		super.create();
		
		if (!VisUI.isLoaded()) VisUI.load();
		
		setTitle(WINDOW_TITLE);
		
		initialiseStage();
	}
	
	private void initialiseStage() {
		stage = new Stage(new ScreenViewport());
		inputMultiplexer.addProcessor(stage);
		
		VisTable root = new VisTable(true);
		
		initialiseSplitPane(root);
		
		root.setFillParent(true);
		root.top().left();
		stage.addActor(root);
	}
	
	private void initialiseSplitPane(VisTable container) {
		VisTable leftPane = new VisTable(true);
		initialiseLeftPane(leftPane);
		leftPane.top().left();
		
		VisTable rightPane = new VisTable(true);
		initialiseRightPane(rightPane);
		rightPane.top().left();
		
		VisSplitPane pane = new VisSplitPane(leftPane, rightPane, false);
		container.add(pane).top().left().grow();
	}
	
	private void initialiseLeftPane(VisTable container) {
		container.add(new VisLabel("left"));
	}
	
	private void initialiseRightPane(VisTable container) {
		initialiseTopBar(container);
	}
	
	private void initialiseTopBar(VisTable container) {
		VisTable topBar = new VisTable(true);
		
		initialiseBackButton(topBar);
		initialisePageLabel(topBar);
		
		topBar.top().left();
		container.add(topBar).pad(8).growX().top().left().row();
	}
	
	private void initialiseBackButton(VisTable container) {
		backButton = new VisTextButton("Back");
		container.add(backButton).left();
	}
	
	private void initialisePageLabel(VisTable container) {
		pageLabel = new VisLabel("AAAAAAAAAAAAAAa");
		container.add(pageLabel).left();
	}
	
	@Override
	public void render() {
		super.render();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act();
		stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		
		if (stage != null) stage.getViewport().update(width, height);
	}
}
