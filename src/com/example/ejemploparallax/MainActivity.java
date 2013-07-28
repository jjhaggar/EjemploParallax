package com.example.ejemploparallax;


import java.io.IOException;


import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
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


import android.view.KeyEvent;
import tv.ouya.console.api.OuyaController;


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
		
		
		// Ahora añadiremos la capa parallax (ParallaxLayer) y la añadiremos a la scene
		
		// En el paquete "org.andengine.entity.scene.background" de "AndEngine" sólo están las clases:
		// AutoParallaxBackground.java
		// Background.java
		// EntityBackground.java
		// IBackground.java
		// ParallaxBackground.java <----- Interesante, pero insuficiente (sólo funciona en horizontal)
		// RepeatingSpriteBackground.java
		// SpriteBackground.java
		
		// En el paquete "com.example.ejemploparallax.ParallaxLayer" están las clases que nos interesan 
		final ParallaxLayer parallaxLayer = new ParallaxLayer(mCamera, true);
		parallaxLayer.setParallaxChangePerSecond(10);
		parallaxLayer.setParallaxScrollFactor(1);
		// ¡Cuidado con la siguiente línea! ¡NO USAR ESTO!-->"org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity"
		ParallaxLayer.ParallaxEntity parEnt1 = new ParallaxLayer.ParallaxEntity(-5, parallaxLayerBackSprite, false);
		parallaxLayer.attachParallaxEntity(parEnt1);
		parallaxLayer.attachParallaxEntity(new ParallaxLayer.ParallaxEntity(-10, parallaxLayerMidSprite, false));
		parallaxLayer.attachParallaxEntity(new ParallaxLayer.ParallaxEntity(-120, parallaxLayerFrontSprite, false));
		
	    scene.attachChild(parallaxLayer);
	    
	    
		
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
		
		return scene;
	}
	
	
	
	
	// ------------------------------------------------------------------------------------------------------------------------
	// --------------- CONTROLES OUYA (pasa de esto ¿ok? de hecho de aquí en adelante puedes borrar si quieres) ---------------
	// ------------------------------------------------------------------------------------------------------------------------
	
	public void moverPersonaje(){ // Prueba chunga, cada vez que pulsamos derecha, movemos el sprite 10 pixels
		personaje.setPosition(personaje.getX()+10, personaje.getY());
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
			
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			moverPersonaje();
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
			
			break;
		case OuyaController.BUTTON_DPAD_DOWN:
			
			break;
		case OuyaController.BUTTON_DPAD_LEFT:
			
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			
			break;
			
		case OuyaController.BUTTON_MENU:
			
			break;
			
		default:
			break;
		}
        
        return handled || super.onKeyUp(keyCode, event);
    }

}