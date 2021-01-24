/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.gwt;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.net.OpenBrowserExample;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.Arrays;
import java.util.Comparator;

public class GwtTestWrapper extends GdxTest {
	Stage ui;
	Table container;
	Skin skin;
	BitmapFont font;
	GdxTest test;
	boolean dispose = false;

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.app.log("GdxTestGwt", "Setting up for " + tests.length + " tests.");

		ui = new Stage(new ExtendViewport(480, 320));
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		container = new Table();
		ui.addActor(container);
		container.debug();
		Table table = new Table();
		ScrollPane scroll = new ScrollPane(table);
		container.add(scroll).expand().fill();
		container.setFillParent(true);
		table.pad(10).defaults().expandX().space(4);
		Arrays.sort(tests, new Comparator<Instancer>() {
			@Override
			public int compare (Instancer o1, Instancer o2) {
				return o1.instance().getClass().getSimpleName().compareTo(o2.instance().getClass().getSimpleName());
			}
		});
		for (final Instancer instancer : tests) {
			table.row();
			TextButton button = new TextButton(instancer.instance().getClass().getSimpleName(), skin);
			button.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					((InputWrapper)Gdx.input).multiplexer.removeProcessor(ui);
					test = instancer.instance();
					Gdx.app.log("GdxTestGwt", "Clicked on " + test.getClass().getName());
					test.create();
					test.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
			});
			table.add(button).expandX().fillX();
		}
		container.row();
		container.add(new Label("Click on a test to start it, press ESC to close it.", new LabelStyle(font, Color.WHITE))).pad(5, 5,
			5, 5);

		Gdx.input = new InputWrapper(Gdx.input) {
			@Override
			public boolean keyUp (int keycode) {
				if (keycode == Keys.ESCAPE) {
					if (test != null) {
						Gdx.app.log("GdxTestGwt", "Exiting current test.");
						dispose = true;
					}
				}
				return false;
			}

			@Override
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				if (screenX < Gdx.graphics.getWidth() / 10.0 && screenY < Gdx.graphics.getHeight() / 10.0) {
					if (test != null) {
						dispose = true;
					}
				}
				return false;
			}
		};
		((InputWrapper)Gdx.input).multiplexer.addProcessor(ui);

		Gdx.app.log("GdxTestGwt", "Test picker UI setup complete.");
	}

	public void render () {
		if (test == null) {
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			ScreenUtils.clear(0, 0, 0, 0);
			ui.act(Gdx.graphics.getDeltaTime());
			ui.draw();
		} else {
			if (dispose) {
				test.pause();
				test.dispose();
				test = null;
				Gdx.graphics.setVSync(true);
				InputWrapper wrapper = ((InputWrapper)Gdx.input);
				wrapper.multiplexer.addProcessor(ui);
				wrapper.multiplexer.removeProcessor(wrapper.lastProcessor);
				wrapper.lastProcessor = null;
				dispose = false;
			} else {
				test.render();
			}
		}
	}

	public void resize (int width, int height) {
		ui.getViewport().update(width, height, true);
		if (test != null) {
			test.resize(width, height);
		}
	}

	class InputWrapper extends InputAdapter implements Input {
		Input input;
		InputProcessor lastProcessor;
		InputMultiplexer multiplexer;

		public InputWrapper (Input input) {
			this.input = input;
			this.multiplexer = new InputMultiplexer();
			this.multiplexer.addProcessor(this);
			input.setInputProcessor(multiplexer);
		}

		@Override
		public float getAccelerometerX () {
			return input.getAccelerometerX();
		}

		@Override
		public float getAccelerometerY () {
			return input.getAccelerometerY();
		}

		@Override
		public float getAccelerometerZ () {
			return input.getAccelerometerZ();
		}

		@Override
		public float getGyroscopeX () {
			return input.getGyroscopeX();
		}

		@Override
		public float getGyroscopeY () {
			return input.getGyroscopeY();
		}

		@Override
		public float getGyroscopeZ () {
			return input.getGyroscopeZ();
		}

		@Override
		public int getMaxPointers () {
			return input.getMaxPointers();
		}

		@Override
		public int getX () {
			return input.getX();
		}

		@Override
		public int getX (int pointer) {
			return input.getX(pointer);
		}

		@Override
		public int getDeltaX () {
			return input.getDeltaX();
		}

		@Override
		public int getDeltaX (int pointer) {
			return input.getDeltaX(pointer);
		}

		@Override
		public int getY () {
			return input.getY();
		}

		@Override
		public int getY (int pointer) {
			return input.getY(pointer);
		}

		@Override
		public int getDeltaY () {
			return input.getDeltaY();
		}

		@Override
		public int getDeltaY (int pointer) {
			return input.getDeltaY(pointer);
		}

		@Override
		public boolean isTouched () {
			return input.isTouched();
		}

		@Override
		public boolean justTouched () {
			return input.justTouched();
		}

		@Override
		public boolean isTouched (int pointer) {
			return input.isTouched(pointer);
		}

		@Override
		public float getPressure () {
			return input.getPressure();
		}

		@Override
		public float getPressure (int pointer) {
			return input.getPressure(pointer);
		}

		@Override
		public boolean isButtonPressed (int button) {
			return input.isButtonPressed(button);
		}

		@Override
		public boolean isKeyPressed (int key) {
			return input.isKeyPressed(key);
		}

		@Override
		public boolean isKeyJustPressed (int key) {
			return input.isKeyJustPressed(key);
		}

		@Override
		public boolean isButtonJustPressed (int button) {
			return input.isButtonJustPressed(button);
		}

		@Override
		public void getTextInput (TextInputListener listener, String title, String text, String hint) {
			input.getTextInput(listener, title, text, hint);
		}

		@Override
		public void getTextInput (TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {
			input.getTextInput(listener, title, text, hint, type);
		}

		@Override
		public void setOnscreenKeyboardVisible (boolean visible) {
			input.setOnscreenKeyboardVisible(visible);
		}

		@Override
		public void setOnscreenKeyboardVisible (boolean visible, OnscreenKeyboardType type) {
			input.setOnscreenKeyboardVisible(visible, type);
		}

		@Override
		public void vibrate (int milliseconds) {
			input.vibrate(milliseconds);
		}

		@Override
		public void vibrate (long[] pattern, int repeat) {
			input.vibrate(pattern, repeat);
		}

		@Override
		public void cancelVibrate () {
			input.cancelVibrate();
		}

		@Override
		public float getAzimuth () {
			return input.getAzimuth();
		}

		@Override
		public float getPitch () {
			return input.getPitch();
		}

		@Override
		public float getRoll () {
			return input.getRoll();
		}

		@Override
		public void getRotationMatrix (float[] matrix) {
			input.getRotationMatrix(matrix);
		}

		@Override
		public long getCurrentEventTime () {
			return input.getCurrentEventTime();
		}

		@Override
		public void setCatchBackKey (boolean catchBack) {
			input.setCatchBackKey(catchBack);
		}

		@Override
		public boolean isCatchBackKey () {
			return input.isCatchBackKey();
		}

		@Override
		public void setCatchMenuKey (boolean catchMenu) {
			input.setCatchMenuKey(catchMenu);
		}

		@Override
		public boolean isCatchMenuKey () {
			return input.isCatchMenuKey();
		}

		@Override
		public void setCatchKey (int keycode, boolean catchKey) {
			input.setCatchKey(keycode, catchKey);
		}

		@Override
		public boolean isCatchKey (int keycode) {
			return input.isCatchKey(keycode);
		}

		@Override
		public void setInputProcessor (InputProcessor processor) {
			multiplexer.removeProcessor(lastProcessor);
			multiplexer.addProcessor(processor);
			lastProcessor = processor;
		}

		@Override
		public InputProcessor getInputProcessor () {
			return input.getInputProcessor();
		}

		@Override
		public boolean isPeripheralAvailable (Peripheral peripheral) {
			return input.isPeripheralAvailable(peripheral);
		}

		@Override
		public int getRotation () {
			return input.getRotation();
		}

		@Override
		public Orientation getNativeOrientation () {
			return input.getNativeOrientation();
		}

		@Override
		public void setCursorCatched (boolean catched) {
			input.setCursorCatched(catched);
		}

		@Override
		public boolean isCursorCatched () {
			return input.isCursorCatched();
		}

		@Override
		public void setCursorPosition (int x, int y) {
			input.setCursorPosition(x, y);
		}
	}

	interface Instancer {
		public GdxTest instance ();
	}

	Instancer[] tests = {new Instancer() {
		public GdxTest instance () {
			return new AccelerometerTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new ActionTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new ActionSequenceTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new AlphaTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new AnimationTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new AnnotationTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new AssetManagerTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new AtlasIssueTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BigMeshTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BitmapFontAlignmentTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BitmapFontFlipTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BitmapFontTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BitmapFontMetricsTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BlitTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new Box2DCharacterControllerTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new Box2DTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new Box2DTestCollection();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new BufferUtilsTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new ClipboardTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new ColorTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new ComplexActionTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new CustomShaderSpriteBatchTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new DecalTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new DisplayModeTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new LabelScaleTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new EdgeDetectionTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new FilesTest();
		}
	}, new Instancer() {
		public GdxTest instance () {
			return new FilterPerformanceTest();
		}
	},
// new Instancer() {public GdxTest instance(){return new FlickScrollPaneTest();}}, // FIXME this messes up stuff, why?
		new Instancer() {
			public GdxTest instance () {
				return new FrameBufferTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new DownloadTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new FramebufferToTextureTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GestureDetectorTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GLProfilerErrorTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GroupCullingTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GroupFadeTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GwtInputTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GwtWindowModeTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new I18NSimpleMessageTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ImageScaleTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ImageTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new IndexBufferObjectShaderTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new IntegerBitmapFontTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new InterpolationTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new InverseKinematicsTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new IsometricTileTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new KinematicBodyTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new LifeCycleTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new LabelTest();
			}
		},
		// new Instancer() {public GdxTest instance(){return new MatrixJNITest();}}, // No purpose
		new Instancer() {
			public GdxTest instance () {
				return new MeshShaderTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new MipMapTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ModelCacheTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new MultitouchTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new MusicTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new OpenBrowserExample();
			}
// }, new Instancer() { public GdxTest instance () { return new NoncontinuousRenderingTest(); } // FIXME doesn't compile due to
// the use of Thread
		}, new Instancer() {
			public GdxTest instance () {
				return new ParallaxTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ParticleEmitterTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new PixelsPerInchTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new PixmapPackerTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new PixmapTest();
			}
		},
		// new Instancer() {public GdxTest instance(){return new PixmapBlendingTest();}}, // FIXME no idea why this doesn't work
		new Instancer() {
			public GdxTest instance () {
				return new PreferencesTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ProjectiveTextureTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new RotationTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ReflectionCorrectnessTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new Scene2dTest();
			}

// new Instancer() {public GdxTest instance(){return new RunnablePostTest();}}, // Goes into infinite loop
// new Instancer() {public GdxTest instance(){return new ScrollPaneTest();}}, // FIXME this messes up stuff, why?
// new Instancer() {public GdxTest instance(){return new ShaderMultitextureTest();}}, // FIXME fucks up stuff
		}, new Instancer() {
			public GdxTest instance () {
				return new ShadowMappingTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ShapeRendererTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SimpleAnimationTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SimpleDecalTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SimpleStageCullingTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SortedSpriteTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SpriteBatchShaderTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SpriteCacheOffsetTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SpriteCacheTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SoundTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new StageTest();
			}
		},
		// new Instancer() {public GdxTest instance(){return new StagePerformanceTest();}}, // FIXME borks out
		new Instancer() {
			public GdxTest instance () {
				return new TableTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextButtonTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextButtonTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextureAtlasTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TiledMapObjectLoadingTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new UITest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new VertexBufferObjectShaderTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new YDownTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new SuperKoalio();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new ReflectionTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TiledMapAtlasAssetManagerTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TimeUtilsTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new GWTLossyPremultipliedAlphaTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new QuadTreeFloatTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new QuadTreeFloatNearestTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextAreaTest();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextAreaTest2();
			}
		}, new Instancer() {
			public GdxTest instance () {
				return new TextAreaTest3();
			}
		} // these may have issues with tab getting intercepted by the browser
	};
}
