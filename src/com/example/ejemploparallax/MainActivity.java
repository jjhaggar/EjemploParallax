package com.example.ejemploparallax;


import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import tv.ouya.console.api.OuyaController;
import android.util.Log;
import android.view.KeyEvent;


public class MainActivity extends SimpleBaseGameActivity {
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 1920;
	private static final int CAMERA_HEIGHT = 1080;	
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private ITexture mParallaxLayerBackTexture;
	private ITexture mParallaxLayerMidTexture;
	private ITexture mParallaxLayerFrontTexture;

	private ITextureRegion mParallaxLayerBackTextureRegion;
	private ITextureRegion mParallaxLayerMidTextureRegion;
	private ITextureRegion mParallaxLayerFrontTextureRegion;
	
	private Sprite parallaxLayerBackSprite;
	private Sprite parallaxLayerMidSprite;
	private Sprite parallaxLayerFrontSprite;
	
	private ITexture mPersonajeTexture;
	private TiledTextureRegion mPersonajeTextureRegion;
	private AnimatedSprite personaje;
	
	private ParallaxBackground2d parallaxLayer; 
	
	private float desplazamientoParallaxVertical = 0;
	private float desplazamientoParallaxHorizontal = 0;
	
	private Camera mCamera;

	@Override
	public EngineOptions onCreateEngineOptions() {
		
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); 
		
		EngineOptions engineOptions = new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), mCamera);
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		
		OuyaController.init(this); // Inicialización mando Ouya. Necesario para que se puedan escuchar los eventos analógicos
		
		return engineOptions;
	}

	@Override
	public void onCreateResources() throws IOException {

		this.mPersonajeTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/running_normal_small_square.png", TextureOptions.BILINEAR);
		this.mPersonajeTextureRegion = TextureRegionFactory.extractTiledFromTexture(this.mPersonajeTexture, 3, 2);
		this.mPersonajeTexture.load();

		this.mParallaxLayerBackTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/parallax_background_layer_back.png");
		this.mParallaxLayerBackTextureRegion = TextureRegionFactory.extractFromTexture(this.mParallaxLayerBackTexture);
		this.mParallaxLayerBackTexture.load();

		this.mParallaxLayerMidTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/parallax_background_layer_mid.png");
		this.mParallaxLayerMidTextureRegion = TextureRegionFactory.extractFromTexture(this.mParallaxLayerMidTexture);
		this.mParallaxLayerMidTexture.load();

		this.mParallaxLayerFrontTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/parallax_background_layer_front.png");
		this.mParallaxLayerFrontTextureRegion = TextureRegionFactory.extractFromTexture(this.mParallaxLayerFrontTexture);
		this.mParallaxLayerFrontTexture.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Scene scene = new Scene();
		
	
		// Creamos los sprites de las entidades parallax y las añadimos al fondo parallax
		parallaxLayerBackSprite = new Sprite(0, 0, this.mParallaxLayerBackTextureRegion, vertexBufferObjectManager);
		parallaxLayerBackSprite.setOffsetCenter(0, 0);

		parallaxLayerMidSprite = new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerMidTextureRegion.getHeight() - 80, this.mParallaxLayerMidTextureRegion, vertexBufferObjectManager);
		parallaxLayerMidSprite.setOffsetCenter(0, 0);

		parallaxLayerFrontSprite = new Sprite(0, 0, this.mParallaxLayerFrontTextureRegion, vertexBufferObjectManager);
		parallaxLayerFrontSprite.setOffsetCenter(0, 0);
		
		//
		// Método ParallaxBackground2d (extiende de Background, así que hay que añadirlo a la scene con "scene.setBackground(parallax)" 
		//
		
		// El scroll automático funciona bien, se iniciaría con una de las siguientes líneas
		parallaxLayer = new AutoHorizontalParallaxBackground(0, 0, 0, 10);
		// parallaxLayer = new AutoVerticalParallaxBackground(0, 0, 0, 10);
		// parallaxLayer = new AutoDiagonalParallaxBackground(0, 0, 0, 10);
		
		// El scroll no automático hay que hacerlo aumentando o disminuyendo el valor X o Y de parallax en el bucle de juego 
		
		// parallaxLayer = new ParallaxBackground2d(0, 0, 0);
		// parallaxLayer.offsetParallaxValue(0, 0); // aún no he comprobado esto
		
		// Inicializamos movimiento del fondo parallax a "pulsar derecha en pad" 
		// moverScroll(OuyaController.BUTTON_DPAD_RIGHT);
		
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(-5.0f,-1.2f, parallaxLayerBackSprite, true, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(-10.0f,-2.0f, parallaxLayerMidSprite, true, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(-30.0f,-3.0f, parallaxLayerFrontSprite, true, false));
		
		scene.setBackground(parallaxLayer); 
		
		
		
		// Creamos el sprite de un personaje y lo añadimos a la scene
		
		final float playerX = CAMERA_WIDTH * 0.5f;
		final float playerY = 5;
		
		personaje = new AnimatedSprite(playerX - 80, playerY, this.mPersonajeTextureRegion, vertexBufferObjectManager);
		personaje.setScaleCenterY(0);
		personaje.setOffsetCenterY(0);
		personaje.setScale(1.5f);
		personaje.animate(new long[]{180, 75, 150, 180, 75, 150}, 0, 5, true);
		
		scene.attachChild(personaje);
		
		// personaje.setAlpha(0); // esconder el personaje 
		
		
		// Bucle de actualización
		float actualizacionesPorSegundo = 60.0f;
		scene.registerUpdateHandler(new TimerHandler(1 / actualizacionesPorSegundo, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				parallaxLayer.setParallaxValue(	parallaxLayer.mParallaxValueX + desplazamientoParallaxHorizontal, 
												parallaxLayer.mParallaxValueY + desplazamientoParallaxVertical);
			}
		}));
		
		
		
		return scene;
	}
	
	
	
	
	// ------------------------------------------------------------------------------------------------------------------------
	// --------------- CONTROLES OUYA (pasa de esto ¿ok? de hecho de aquí en adelante puedes borrar si quieres) ---------------
	// ------------------------------------------------------------------------------------------------------------------------
	
	public void moverPersonaje(){ // Prueba chunga, cada vez que pulsamos derecha, movemos el sprite 10 pixels
		personaje.setPosition(personaje.getX()+10, personaje.getY());
	}
	
	public void moverScroll(int direccion){
		//Log.v("direccion", ""+direccion);
		switch (direccion) {
		case OuyaController.BUTTON_DPAD_UP:
			desplazamientoParallaxVertical = 1;
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			desplazamientoParallaxVertical = -1;
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			desplazamientoParallaxHorizontal = -1;
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			desplazamientoParallaxHorizontal = 1;
			break;
		default:
			break;
		}
	}
	
	public void detenerScroll(int direccion){
		switch (direccion) {
		case OuyaController.BUTTON_DPAD_UP:
			desplazamientoParallaxVertical = 0;
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			desplazamientoParallaxVertical = 0;
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			desplazamientoParallaxHorizontal = 0;
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			desplazamientoParallaxHorizontal = 0;
			break;
		default:
			break;
		}
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        switch (keyCode) {
		case OuyaController.BUTTON_O:
			break;
		case OuyaController.BUTTON_U:
			break;
		case OuyaController.BUTTON_Y:
			break;
		case OuyaController.BUTTON_A:
			break;
		case OuyaController.BUTTON_L1:
			break;
		case OuyaController.BUTTON_L3:
			break;
		case OuyaController.BUTTON_R1:
			break;
		case OuyaController.BUTTON_R3:
			break;
		case OuyaController.BUTTON_DPAD_UP:
			moverScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			moverScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			moverScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			// moverPersonaje();
			moverScroll(keyCode);
			break;
		case OuyaController.BUTTON_MENU:
			break;
		default:
			break;
		}
        return handled || super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean handled = OuyaController.onKeyUp(keyCode, event);
        
        switch (keyCode) {
		case OuyaController.BUTTON_O:
			break;
		case OuyaController.BUTTON_U:
			break;
		case OuyaController.BUTTON_Y:
			break;
		case OuyaController.BUTTON_A:
			break;
		case OuyaController.BUTTON_L1:
			break;
		case OuyaController.BUTTON_L3:
			break;
		case OuyaController.BUTTON_R1:
			break;
		case OuyaController.BUTTON_R3:
			break;
		case OuyaController.BUTTON_DPAD_UP:
			detenerScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			detenerScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			detenerScroll(keyCode);
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			detenerScroll(keyCode);
			break;
		case OuyaController.BUTTON_MENU:
			break;
		default:
			break;
		}
        return handled || super.onKeyUp(keyCode, event);
    }

}