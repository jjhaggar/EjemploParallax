package com.example.ejemploparallax;


import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import com.example.ejemploparallax.ParallaxBackground2d.ParallaxBackground2dEntity;

import tv.ouya.console.api.OuyaController;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;


public class MainActivity extends SimpleBaseGameActivity  {
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 1920;
	private static final int CAMERA_HEIGHT = 1080;	
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	// Necesitamos 10 texturas para el fondo de la fase de saltos
	
	private ITexture mITe_bg01_statues, mITe_bg01_bamboo, mITe_bg02_forest1, mITe_bg03_forest2, mITe_bg04_mount, mITe_bg05_pagoda,
					 mITe_bg06_clouds, mITe_bg07_lake, mITe_bg08_fuji, mITe_bg09_sky;
	
	
	// Necesitamos 16 regiones de texturas para el fondo de la fase de saltos (para poder repetirlas, algunas texturas se dividen en 3)
	
	private ITextureRegion mITR_bg01_statues, mITR_bg01_bamboo_low, mITR_bg01_bamboo_mid, mITR_bg01_bamboo_high, 
						   mITR_bg02_forest1_low, mITR_bg02_forest1_mid, mITR_bg02_forest1_high, 
						   mITR_bg03_forest2_low, mITR_bg03_forest2_mid, mITR_bg03_forest2_high, 
						   mITR_bg04_mount, mITR_bg05_pagoda, mITR_bg06_clouds, mITR_bg07_lake, mITR_bg08_fuji, mITR_bg09_sky;
	
	// De las 16 regiones de textura salen bastantes sprites más
	private Sprite mSpr_bg01_statues, 
					mSpr_bg01_bamboo_low1, mSpr_bg01_bamboo_mid1_a, mSpr_bg01_bamboo_mid1_b, mSpr_bg01_bamboo_mid1_c, mSpr_bg01_bamboo_high1, 
					mSpr_bg01_bamboo_low2, mSpr_bg01_bamboo_mid2_a, mSpr_bg01_bamboo_mid2_b, mSpr_bg01_bamboo_mid2_c, mSpr_bg01_bamboo_high2, // 2º tronco de bambú
					mSpr_bg02_forest1_low, mSpr_bg02_forest1_mid1, mSpr_bg02_forest1_mid2, mSpr_bg02_forest1_high, 
					mSpr_bg03_forest2_low, mSpr_bg03_forest2_mid, mSpr_bg03_forest2_high, 
					mSpr_bg04_mount, mSpr_bg05_pagoda, mSpr_bg06_clouds, mSpr_bg07_lake, mSpr_bg08_fuji, mSpr_bg09_sky;
	
	
	private float fFPL01, fFPL02, fFPL03, fFPL04, fFPL05, fFPL06, fFPL07, fFPL08, fFPL09; // fFactorParallaxLayer 
	
	
	// Sólo necesitaríamos variables en los objetos ParallaxBackground2dEntity a los que vamos a cambiar alguna propiedad
	private ParallaxBackground2dEntity pBE01, pBE02;//...Estoy pensando que símplemete se podría cambiar la visibilidad del sprite, pero también sería una chapuza así que lo dejo así ^^U
	
	
	private ParallaxBackground2d parallaxLayer; 
	
	private float desplazamientoParallaxVertical = 0;
	private float desplazamientoParallaxHorizontal = 0;
	
	private final int repBamboo = 9; // Veces que se repite el cuerpo del bambu
	
	private boolean autoScroll = false;
	
	
	// Bucle de actualización
	private float actualizacionesPorSegundo = 60.0f;
	final private IUpdateHandler bucleActualizaciones = new TimerHandler(1 / actualizacionesPorSegundo, true, new ITimerCallback() {
		@Override
		public void onTimePassed(final TimerHandler pTimerHandler) {
			parallaxLayer.setParallaxValue(	parallaxLayer.mParallaxValueX + desplazamientoParallaxHorizontal, 
											parallaxLayer.mParallaxValueY + desplazamientoParallaxVertical);
			
			int altoBambu = 77*repBamboo; // a ojimetro
			
			if (parallaxLayer.mParallaxValueY < 0) {autoScrollUp();}
			if (parallaxLayer.mParallaxValueY > 820) { autoScrollDown();}
			
			if (parallaxLayer.mParallaxValueY > 0 && parallaxLayer.mParallaxValueY < 50 && pBE01.getmRepeatY()){ // este "if" es innecesario en la fase rel, lo tengo sólo para pruebas
				pBE01.setmRepeatY(false);
				mSpr_bg01_bamboo_mid1_b.setY( mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() );
				pBE02.setmRepeatY(false);
				mSpr_bg01_bamboo_mid2_b.setY( mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid2_a.getHeight() );
				Log.v("parallaxLayer.mParallaxValueY ", "no repite" );
			}
			
			if (parallaxLayer.mParallaxValueY >= 50 && parallaxLayer.mParallaxValueY < altoBambu && !pBE01.getmRepeatY()){ // altoBambu era 200
				pBE01.setmRepeatY(true);
				mSpr_bg01_bamboo_mid1_b.setY( mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-2) );
				pBE02.setmRepeatY(true);
				mSpr_bg01_bamboo_mid2_b.setY( mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid2_a.getHeight()*(repBamboo-2) );
				Log.v("parallaxLayer.mParallaxValueY ", "repite" );
			}
			
			if (parallaxLayer.mParallaxValueY >= altoBambu && pBE01.getmRepeatY()){
				pBE01.setmRepeatY(false);
				Log.v("parallaxLayer.mParallaxValueY ", "no repite" );
				pBE02.setmRepeatY(false);
			}
		}
	});
	
	private Camera mCamera;
	private Scene scene;
	
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
		
		// Textura de las estatuas de piedra
		this.mITe_bg01_statues = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_1_stone_statues.png");
		this.mITR_bg01_statues = TextureRegionFactory.extractFromTexture(this.mITe_bg01_statues);
		this.mITe_bg01_statues.load();

		// Texturas del bambú en el que rebotan los protagonistas
		// ^ 91 px (mITR_bg01_bamboo_high)
		// | 921 px (mITR_bg01_bamboo_mid)
		// v 68 px (mITR_bg01_bamboo_low)
		this.mITe_bg01_bamboo = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_1_bamboo.png");
		this.mITR_bg01_bamboo_high= TextureRegionFactory.extractFromTexture(this.mITe_bg01_bamboo, 0, 0, 89, 91);
		this.mITR_bg01_bamboo_mid = TextureRegionFactory.extractFromTexture(this.mITe_bg01_bamboo, 0, 91, 89, 921);
		this.mITR_bg01_bamboo_low  = TextureRegionFactory.extractFromTexture(this.mITe_bg01_bamboo, 0, 1012, 89, 68);
		this.mITe_bg01_bamboo.load();
		

		// Texturas del bosque de bambú más cercano
		// ^ 44 px (mITR_bg01_bamboo_high)
		// | 718 px (mITR_bg01_bamboo_mid)
		// v 318 px (mITR_bg01_bamboo_low) 1920 
		this.mITe_bg02_forest1 = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_2_bamboo_forest_1.png");
		this.mITR_bg02_forest1_high= TextureRegionFactory.extractFromTexture(this.mITe_bg02_forest1, 0, 0, 1920, 44);
		this.mITR_bg02_forest1_mid = TextureRegionFactory.extractFromTexture(this.mITe_bg02_forest1, 0, 44, 1920, 718);
		this.mITR_bg02_forest1_low = TextureRegionFactory.extractFromTexture(this.mITe_bg02_forest1, 0, 763, 1920, 318);//0, 762, 1920, 1080, 318px
		this.mITe_bg02_forest1.load();
		
		// Texturas del bosque de bambú lejano
		// ^ 80 px (mITR_bg01_bamboo_high)
		// | 536 px (mITR_bg01_bamboo_mid)
		// v 464 px (mITR_bg01_bamboo_low) 1920 
		this.mITe_bg03_forest2 = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_3_bamboo_forest_2.png");
		this.mITR_bg03_forest2_high= TextureRegionFactory.extractFromTexture(this.mITe_bg03_forest2, 0, 0, 1920, 80);
		this.mITR_bg03_forest2_mid = TextureRegionFactory.extractFromTexture(this.mITe_bg03_forest2, 0, 80, 1920, 536);
		this.mITR_bg03_forest2_low = TextureRegionFactory.extractFromTexture(this.mITe_bg03_forest2, 0, 80+536, 1920, 464);//0, 762, 1920, 1080, 318px
		this.mITe_bg03_forest2.load();
		
		// Textura de la montaña cercana
		this.mITe_bg04_mount= new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_4_mount.png");
		this.mITR_bg04_mount = TextureRegionFactory.extractFromTexture(this.mITe_bg04_mount);
		this.mITe_bg04_mount.load();

		// Textura de la pagoda
		this.mITe_bg05_pagoda = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_5_pagoda.png");
		this.mITR_bg05_pagoda = TextureRegionFactory.extractFromTexture(this.mITe_bg05_pagoda);
		this.mITe_bg05_pagoda.load();
		
		// Textura de las nubes
		this.mITe_bg06_clouds = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_6_clouds.png");
		this.mITR_bg06_clouds = TextureRegionFactory.extractFromTexture(this.mITe_bg06_clouds);
		this.mITe_bg06_clouds.load();

		// Textura del lago
		this.mITe_bg07_lake = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_7_lake.png");
		this.mITR_bg07_lake = TextureRegionFactory.extractFromTexture(this.mITe_bg07_lake);
		this.mITe_bg07_lake.load();
		
		// Textura del monte Fuji
		this.mITe_bg08_fuji = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_8_mount_fuji.png");
		this.mITR_bg08_fuji = TextureRegionFactory.extractFromTexture(this.mITe_bg08_fuji);
		this.mITe_bg08_fuji.load();
		
		// Textura del cielo
		this.mITe_bg09_sky = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/trial_jump/jump_bg_9_sky.png");
		this.mITR_bg09_sky= TextureRegionFactory.extractFromTexture(this.mITe_bg09_sky);
		this.mITe_bg09_sky.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		scene = new Scene();
		
		
		
		// Asignamos posición y factor parallax de los sprites que crearemos a continuación
		
		// Posición
		int pBX1 = 350; // posición bambú ancho 1
		int pBX2 = 1300; // posición bambú ancho 2
		int pMY = 800; // posición montaña cercana
		int pPX = 1400; int pPY = 1200;  // posición pagoda
		int pCY = 1600; // posición nubes
		int pLY = 400; // posicion lago
		int pFY = 850; // posición fuji
		
		// Factor de las capas Parallax
		fFPL01 =-10.0f; // Bambu rebotable   
		fFPL02 = -9.0f; // Bosque bambu cercano
		fFPL03 = -5.5f; // Bosque bambu lejano // HABRÍA QUE CREAR OTRO BOSQUE de BAMBÚ MÁS
		fFPL04 = -4.5f; // montaña cercana
		fFPL05 = -4.0f; // pagoda
		fFPL06 = -2.0f; // nubes
		fFPL07 = -2.0f; // lago
		fFPL08 = -1.8f; // m. fuji
		fFPL09 = -0.5f; // cielo
		
		
		
		// Creamos los sprites de las entidades parallax
		
		// Sprite Cielo
		mSpr_bg09_sky= new Sprite(0, 0, this.mITR_bg09_sky, vertexBufferObjectManager);
		mSpr_bg09_sky.setOffsetCenter(0, 0); // Si no hacemos esto, los sprites tienen su offset en el centro, así los colocamos abajo a la izquierda de la imagen
		mSpr_bg09_sky.setPosition(0, 0);

		// Sprite M. Fuji
		mSpr_bg08_fuji = new Sprite(0, 0, this.mITR_bg08_fuji, vertexBufferObjectManager);
		mSpr_bg08_fuji.setOffsetCenter(0, 0);
		mSpr_bg08_fuji.setPosition(0, pFY);

		// Sprite Lago
		mSpr_bg07_lake= new Sprite(0, 0, this.mITR_bg07_lake, vertexBufferObjectManager);
		mSpr_bg07_lake.setOffsetCenter(0, 0);
		mSpr_bg07_lake.setPosition(0, pLY);

		// Sprite nubes
		mSpr_bg06_clouds = new Sprite(0, 0, this.mITR_bg06_clouds, vertexBufferObjectManager);
		mSpr_bg06_clouds.setOffsetCenter(0, 0);
		mSpr_bg06_clouds.setPosition(0, pCY);
		
		// Sprite Pagoda
		mSpr_bg05_pagoda = new Sprite(0, 0, this.mITR_bg05_pagoda, vertexBufferObjectManager);
		mSpr_bg05_pagoda.setOffsetCenter(0, 0);
		mSpr_bg05_pagoda.setPosition(pPX, pPY);
		
		// Sprite Montaña cercana
		mSpr_bg04_mount= new Sprite(0, 0, this.mITR_bg04_mount, vertexBufferObjectManager);
		mSpr_bg04_mount.setOffsetCenter(0, 0);
		mSpr_bg04_mount.setPosition(0, pMY);
		
		// Sprites Bosque Bambu lejos
		mSpr_bg03_forest2_low = new Sprite(0, 0, this.mITR_bg03_forest2_low, vertexBufferObjectManager);
		mSpr_bg03_forest2_low.setOffsetCenter(0, 0);
		mSpr_bg03_forest2_low.setPosition(0, 0);
		mSpr_bg03_forest2_mid = new Sprite(0, 0, this.mITR_bg03_forest2_mid, vertexBufferObjectManager);
		mSpr_bg03_forest2_mid.setOffsetCenter(0, 0);
		mSpr_bg03_forest2_mid.setPosition(0, mSpr_bg03_forest2_low.getHeight()); 
		mSpr_bg03_forest2_high = new Sprite(0, 0, this.mITR_bg03_forest2_high, vertexBufferObjectManager);
		mSpr_bg03_forest2_high.setOffsetCenter(0, 0);
		mSpr_bg03_forest2_high.setPosition(0, mSpr_bg03_forest2_low.getHeight() + mSpr_bg03_forest2_mid.getHeight() );  		

		// Sprites Bosque Bambu cerca
		mSpr_bg02_forest1_low = new Sprite(0, 0, this.mITR_bg02_forest1_low, vertexBufferObjectManager);
		mSpr_bg02_forest1_low.setOffsetCenter(0, 0);
		mSpr_bg02_forest1_low.setPosition(0, 0);
		mSpr_bg02_forest1_mid1 = new Sprite(0, 0, this.mITR_bg02_forest1_mid, vertexBufferObjectManager);
		mSpr_bg02_forest1_mid1.setOffsetCenter(0, 0);
		mSpr_bg02_forest1_mid1.setPosition(0, mSpr_bg02_forest1_low.getHeight()); 
		mSpr_bg02_forest1_mid2 = new Sprite(0, 0, this.mITR_bg02_forest1_mid, vertexBufferObjectManager);
		mSpr_bg02_forest1_mid2.setOffsetCenter(0, 0);
		mSpr_bg02_forest1_mid2.setPosition(0, mSpr_bg02_forest1_low.getHeight() + mSpr_bg02_forest1_mid1.getHeight() ); 
		mSpr_bg02_forest1_high = new Sprite(0, 0, this.mITR_bg02_forest1_high, vertexBufferObjectManager);
		mSpr_bg02_forest1_high.setOffsetCenter(0, 0);
		mSpr_bg02_forest1_high.setPosition(0, mSpr_bg02_forest1_low.getHeight() + mSpr_bg02_forest1_mid1.getHeight()*2 );  		
		
		// Sprites Rebounding Bamboo trunk (left)
		mSpr_bg01_bamboo_low1 = new Sprite(0, 0, this.mITR_bg01_bamboo_low, vertexBufferObjectManager);
		mSpr_bg01_bamboo_low1.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_low1.setPosition(pBX1, 0);
		mSpr_bg01_bamboo_mid1_a = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid1_a.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid1_a.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight()); //68);
		mSpr_bg01_bamboo_mid1_b = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid1_b.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid1_b.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() ); 
		mSpr_bg01_bamboo_mid1_c = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid1_c.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid1_c.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-1) ); 
		mSpr_bg01_bamboo_high1 = new Sprite(0, 0, this.mITR_bg01_bamboo_high, vertexBufferObjectManager);
		mSpr_bg01_bamboo_high1.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_high1.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight()+ mSpr_bg01_bamboo_mid1_a.getHeight()*repBamboo); // 989);  		
		
		// Sprites Rebounding Bamboo trunk (right)
		mSpr_bg01_bamboo_low2 = new Sprite(0, 0, this.mITR_bg01_bamboo_low, vertexBufferObjectManager);
		mSpr_bg01_bamboo_low2.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_low2.setPosition(pBX2, 0);
		mSpr_bg01_bamboo_mid2_a = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid2_a.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid2_a.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight()); //68);
		mSpr_bg01_bamboo_mid2_b = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid2_b.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid2_b.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() ); 
		mSpr_bg01_bamboo_mid2_c = new Sprite(0, 0, this.mITR_bg01_bamboo_mid, vertexBufferObjectManager);
		mSpr_bg01_bamboo_mid2_c.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_mid2_c.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-1) ); 
		mSpr_bg01_bamboo_high2 = new Sprite(0, 0, this.mITR_bg01_bamboo_high, vertexBufferObjectManager);
		mSpr_bg01_bamboo_high2.setOffsetCenter(0, 0);
		mSpr_bg01_bamboo_high2.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight()+ mSpr_bg01_bamboo_mid1_a.getHeight()*repBamboo); // 989);  		
		
		// Sprites Statues 
		mSpr_bg01_statues = new Sprite(0, 0, this.mITR_bg01_statues, vertexBufferObjectManager);
		mSpr_bg01_statues.setOffsetCenter(0, 0);
		mSpr_bg01_statues.setPosition(0, 0);
		
		
		
		// Creamos el fondo parallax y a continuación le asignamos las entidades parallax
		parallaxLayer = new ParallaxBackground2d(0, 0, 0); 
		
		// Cielo
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL09, fFPL09, mSpr_bg09_sky, false, false));
				
		// Fuji
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL08, fFPL08, mSpr_bg08_fuji, false, false));
		
		// Bosque de bambú lejano
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL07, fFPL07, mSpr_bg07_lake, false, false));
				
		// Nubes
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL06, fFPL06, mSpr_bg06_clouds, false, false));
		
		// Pagoda
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL05, fFPL05, mSpr_bg05_pagoda, false, false));
		
		// Montaña
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL04, fFPL04, mSpr_bg04_mount, false, false));
		
		// Bosque de bambú lejano
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_low, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_mid, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_high, false, false));

		// Bosque de bambú cercano
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_low, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_mid1, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_mid2, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_high, false, false));
		
		// Bambu rebotable izquierdo
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_low1, false, false));
		pBE01 = new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_a, false, false);
		parallaxLayer.attachParallaxEntity(pBE01); 
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_b, false, false)); //pBE01);
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_c, false, false)); 
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_high1, false, false));
		// Bambu rebotable derecho		
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_low2, false, false));
		pBE02 = new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_a, false, false);
		parallaxLayer.attachParallaxEntity(pBE02);
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_b, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_c, false, false));
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_high2, false, false));
		// Estatuas
		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_statues, false, false));

		
		// Añadimos el fondo parallax a la escena
		scene.setBackground(parallaxLayer); 
		
		// Registramos el manejador de actualizaciones
		scene.registerUpdateHandler(bucleActualizaciones);
		
		// Iniciamos el autoscroll, para que pueda verse en dispositivos sin controles Ouya
		autoScroll = true;
		autoScrollUp();
		
		
		return scene;
		
	}
	
	public void stopBucle(){
		scene.unregisterUpdateHandler(bucleActualizaciones);
	}
	
	// ------------------------------------------------------------------------------------------------------------------------
	// --------------- CONTROLES OUYA (pasa de esto ¿ok? de hecho de aquí en adelante puedes borrar si quieres) ---------------
	// ------------------------------------------------------------------------------------------------------------------------
	
	public void moverPersonaje(){ // Prueba chunga, cada vez que pulsamos derecha, movemos el sprite 10 pixels
		// personaje.setPosition(personaje.getX()+10, personaje.getY());
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
			// desplazamientoParallaxHorizontal = -1;
			break;
		case OuyaController.BUTTON_DPAD_RIGHT:
			// desplazamientoParallaxHorizontal = 1;
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
			Log.v("Pulsado", "O");
			pBE01.tooglemRepeatY();
			pBE02.tooglemRepeatY();
			Log.v("mRepeatY=", ""+pBE01.getmRepeatY());
			break;
		case OuyaController.BUTTON_U:
			Log.v("parallaxLayer.mParallaxValueY ", ""+parallaxLayer.mParallaxValueY );
			break;
		case OuyaController.BUTTON_Y:
			break;
		case OuyaController.BUTTON_A:
			autoScroll = !autoScroll;
			Log.v("autoScroll", ""+autoScroll);
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

    public void autoScrollUp(){
    	if (autoScroll)
    		desplazamientoParallaxVertical = 1;    	
    }
    
    public void autoScrollDown(){
    	if (autoScroll)
    		desplazamientoParallaxVertical = -1;    	
    }
    
}