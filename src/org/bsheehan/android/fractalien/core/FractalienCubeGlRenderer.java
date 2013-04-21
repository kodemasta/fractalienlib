package org.bsheehan.android.fractalien.core;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import org.bsheehan.android.fractalien.core.function.IteratedFunctionFactory.FractalType;
import org.bsheehan.android.fractalien.core.utils.ESShader;
import org.bsheehan.android.fractalien.core.utils.ESShapes;
import org.bsheehan.android.fractalien.core.utils.ESTransform;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.SystemClock;

/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name FractalienGlRenderer
 * 
 * @description Extends Android GLSurfaceViewRender for customizing the OpenGl
 *              scene displayed by the Activity.
 */
@TargetApi(8)
public class FractalienCubeGlRenderer implements GLSurfaceView.Renderer {

	private float endAngle;

	// class UpdateTextureTask extends AsyncTask<Integer, Void, Void> {
	// private final WeakReference<FractalienCubeGlRenderer> rendererRef;
	//
	// public UpdateTextureTask(FractalienCubeGlRenderer renderer) {
	// // Use a WeakReference to ensure the ImageView can be garbage collected
	// rendererRef = new WeakReference<FractalienCubeGlRenderer>(renderer);
	// }
	//
	// // Decode image in background.
	// @Override
	// protected Void doInBackground(Integer... params) {
	// rendererRef.get().updateTexture(params[0]);
	// return null;
	// }
	//
	// // Once complete, see if ImageView is still around and set bitmap.
	// @Override
	// protected void onPostExecute(Void foo) {
	// }
	// }
	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public FractalienCubeGlRenderer(Context context) {

		// This is the GLSL vertex shader program that will be assigned to the
		// OpenGl
		// graphics pipeline for GPU optimized performance.
		this.vShaderStr = "attribute vec4 a_position;   \n"
				+ "uniform mat4 u_mvpMatrix;    \n"
				+ "attribute vec2 a_texCoord;   \n"
				+ "varying vec2 v_texCoord;     \n"
				+ "void main()                  \n"
				+ "{                            \n"
				+ "   gl_Position = u_mvpMatrix * a_position; \n"
				+ "   v_texCoord = a_texCoord;  \n"
				+ "}                            \n";

		// This is the GLSL fragment shader program that will be assigned to the
		// OpenGl
		// graphics pipeline for GPU optimized performance.
		this.fShaderStr = "precision mediump float;                         \n"
				+ "varying vec2 v_texCoord;                            \n"
				+ "uniform sampler2D s_texture;                        \n"
				+ "void main()                                         \n"
				+ "{                                                   \n"
				+ "  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
				+ "}                                                   \n";

		// create the cube vertices for rendering
		this.cubePrimative.genSquare(1.0f);

	}

	/**
	 * This method is called to change update the texture state with the latest
	 * fractal bitmap.
	 * 
	 * @return textureMap ID updated
	 */
	private int updateTexture(int index) {

		// glTexImage2D accepts a level integer parameter for mip mapping.
		// Currently we are not using hierarchical texturing so set this to
		// zero.
		// final int level = 0;

		// set the texture ID to bind future texture attribute operations to.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIds[index]);

		// Log.i("bob", "texture bound " + this.fractal.getWidth() + " " +
		// this.fractal.getHeight());

		synchronized (FractalienCubeGlRenderer.this.lock) {
			// this call assigns texture pixels to the OpenGl texture memory
			// TODO: investigate glTexSubImage2D for performance boost.
			if (textured[index] == false) {
				GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
						this.fractal.getWidth(), this.fractal.getHeight(), 0,
						GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
						this.fractal.getBufferColors());
			} else
				GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0,
						this.fractal.getWidth(), this.fractal.getHeight(),
						GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
						this.fractal.getBufferColors());
		}

		if (textured[index] == false) {
			// set the texture interpolation to higher quality
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			// lets not repeat the texture. just one fractal bitmap per cube
			// side
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// This fixes a strange artifact of texture mapping where the
			// texture appears to warp
			// when rotated in the projection matrix for the scene.
			final FloatBuffer largest = FloatBuffer.allocate(1);
			GLES20.glGetFloatv(GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
					largest);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest.get());
		}

		return this.textureIds[index];
	}

	// CAlled when surface is created at startup.
	// Initialize the shader and program object
	//
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		// Load the shaders and get a linked program object
		this.programObject = ESShader.loadProgram(this.vShaderStr,
				this.fShaderStr);

		// Generate texture IDs for texture mapping
		GLES20.glGenTextures(textureIdMax, this.textureIds, 0);

		// Get the attribute locations
		this.positionLoc = GLES20.glGetAttribLocation(this.programObject,
				"a_position");
		this.texCoordLoc = GLES20.glGetAttribLocation(this.programObject,
				"a_texCoord");

		// Get the uniform locations
		this.mvpLoc = GLES20.glGetUniformLocation(this.programObject,
				"u_mvpMatrix");

		// Starting rotation angle for the cube
		this.currentAngle = 0.0f;

		gl.glCullFace(GL10.GL_BACK);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glDisable(GLES20.GL_DITHER);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// Load the vertex data
		GLES20.glVertexAttribPointer(this.positionLoc, 3, GLES20.GL_FLOAT,
				false, 0, this.cubePrimative.getVertices());

		GLES20.glVertexAttribPointer(this.texCoordLoc, 2, GLES20.GL_FLOAT,
				false, 0, this.cubePrimative.getTexCoords());

		// Use the program object
		GLES20.glUseProgram(this.programObject);

		GLES20.glEnableVertexAttribArray(this.positionLoc);
		GLES20.glEnableVertexAttribArray(this.texCoordLoc);

		// Fire off the producer/consumer threads that update the fractal RGB
		// buffer, and set a flag
		// for the rendering thread to update the texture map.
		launchThreads();
	}

	private int getColor(int i) {
		// Random rand = new Random();
		switch (i) {
		case 0:
			return Color.WHITE;
		case 1:
			return Color.YELLOW;
		case 2:
			return Color.RED;
		case 3:
			return Color.GREEN;
		case 4:
			return Color.BLUE;
		case 5:
			return Color.CYAN;
		}

		return Color.CYAN;
	}

	/**
	 * Two threads are launched. One that generates fractal bitmaps, and one
	 * that checks to see if they are created.
	 * 
	 * In order to avoid user wait time to see a texture, we show low resolution
	 * textures increasing the resolution each step until a full res texture is
	 * created. Until we get a full resolution texture, we stick with the same
	 * fractal region in the complex plane.
	 * 
	 * Each time a particular resolution is generated, we notify the consuming
	 * thread that a textxure is available. This thread sets a flag in the
	 * OpenGl rendering thread that allows the texture memory to map to the
	 * newly produced fractal bitmap.
	 */
	private void launchThreads() {
		this.fractal = FractalFactory.createFractal(FractalType.JULIASET);
		this.testFractal = FractalFactory.createFractal(FractalType.JULIASET);
		this.killThreads = false;

		// go forth and create fractal bitmaps
		this.produceFractalThread = new Thread(new Runnable() {

			private int dim = 512;
			private int testDim = 32;

			@Override
			public void run() {
				FractalienCubeGlRenderer.this.testFractal.setDims(testDim,
						testDim);

				synchronized (FractalienCubeGlRenderer.this.lock) {
					FractalienCubeGlRenderer.this.fractal.setDims(this.dim,
							this.dim);
				}

				FractalienCubeGlRenderer.this.initialized = false;
				for (int i = 0; i < textureIdMax; ++i) {
					synchronized (FractalienCubeGlRenderer.this.lock) {
						fractal.assignColor(getColor(i));
					}
					FractalienCubeGlRenderer.this.produced = true;
					try {
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						// if we interrupt the sleeping thread an
						// exception will get us here.
						return;
					}
				}

				while (!FractalienCubeGlRenderer.this.killThreads) {
					synchronized (FractalienCubeGlRenderer.this.lock) {
						if (!FractalienCubeGlRenderer.this.produced) {
							FractalFactory.makeItCool(testFractal,
									testFractal.getFractalFunction()
											.getFractalConfig().isCentered,
									testDim, testDim);

							fractal.getFractalFunction().setFractalRegion(
									testFractal.getFractalFunction()
											.getFractalRegion());

							fractal.getFractalFunction().setInitialConditions(
									testFractal.getFractalFunction());

							FractalFactory
									.generateFullFractal(FractalienCubeGlRenderer.this.fractal);

							FractalienCubeGlRenderer.this.produced = true;
						}
					}
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						// if we interrupt the sleeping thread an
						// exception will get us here.
						return;
					}
					FractalienCubeGlRenderer.this.initialized = true;

				}
			}
		});

		// THIS THREAD IS FOR DEMONSTRATION PURPOSES. A CONSUMER THREAD TO MATCH
		// THE PRODUCER THREAD.
		// Patiently wait for notifications that new fractal bitmaps have been
		// created
		// Not currently used for this version. But may come in handy.
		// this.consumeFractalThread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// while (!FractalienCubeGlRenderer.this.killThreads) {
		// if (FractalienCubeGlRenderer.this.fractalProduced) {
		// synchronized (FractalienCubeGlRenderer.this.lock) {
		// try {
		// Log.d("bob", "consumed");
		//
		// // wait for notification from producer thread that a new pixel buffer
		// is ready.
		// FractalienCubeGlRenderer.this.lock.wait();
		// } catch (final InterruptedException e) {
		// // if we interrupt the waiting thread an exception will get us here.
		// return;
		// }
		// }
		// }
		// }
		// }
		// });

		// start the threads
		this.produceFractalThread.setPriority(Thread.MIN_PRIORITY);
		this.produceFractalThread.start();
		// NOT USEFUL AT THIS TIME. FOR DEMO PURPOSES ONLY
		// this.consumeFractalThread.start();
	}

	// OpenGl event driven call to render the surface
	public void onDrawFrame(GL10 glUnused) {

		if (produced) {
			updateTexture(textureIdIndex);
			textured[textureIdIndex] = true;
			textureIdIndex++;
			if (textureIdIndex == textureIdMax)
				textureIdIndex = 0; 
			produced = false;
			if (FractalienCubeGlRenderer.this.initialized){
				enableRotate = true;
				if (this.endAngle == 360.f || this.endAngle == 0.f)
					this.endAngle = 180.f;
				else
					this.endAngle = 360.f;
			}

		}

		// set to true by fractal producing thread
		final long curTime = SystemClock.uptimeMillis();
		if (this.lastTime == 0)
			this.lastTime = curTime;
		final long elapsedTime = curTime - this.lastTime;
		final float deltaTime = elapsedTime / 6.0f;
		this.lastTime = curTime;

		// Compute a rotation angle based on time to rotate the cube
		// This will keep the rotation the same on fast and slow platforms
		if (enableRotate) {
			this.currentAngle += deltaTime;
			if (this.currentAngle >= endAngle) {
				enableRotate = false;
				this.currentAngle = endAngle;
			}
			if (this.currentAngle >= 360.f) {
				this.currentAngle -= 360.0f;
			}
		}

		// Clear the color buffer
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		for (int i = 0; i < textureIdMax; ++i)
			if (textured[i])
				drawFrame(i);
	}

	private void drawFrame(int index) {
		// ///////////////////////////////////////////////////////////////////////
		// Bind the texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIds[index]);
		// Generate a model view matrix to rotate/translate the cube
		this.modelview.matrixLoadIdentity();
		// Translate away from the viewer
		// his.modelview.translate(0, 0, -.5f);
		// Rotate the cube
		float angle = this.currentAngle + angleInc * index;
		this.modelview.rotateY(angle);
		// Compute the final MVP by multiplying the
		// modelview and perspective matrices together
		this.mMVPMatrix.matrixMultiply(this.modelview.get(),
				this.perspective.get());

		// Load the MVP matrix
		GLES20.glUniformMatrix4fv(this.mvpLoc, 1, false,
				this.mMVPMatrix.getAsFloatBuffer());
		// Draw the cube
		GLES20.glDrawElements(GLES20.GL_TRIANGLES,
				this.cubePrimative.getNumIndices(), GLES20.GL_UNSIGNED_SHORT,
				this.cubePrimative.getIndices());
	}

	/**
	 * This will draw a single primitive into the 3D scene
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param texIndex
	 */
	// private void drawSingleShape(int i, int j, int k) {
	// update(i * 1.5f, j * 1.5f, k);
	// // Load the MVP matrix
	// GLES20.glUniformMatrix4fv(this.mvpLoc, 1, false,
	// this.mMVPMatrix.getAsFloatBuffer());
	// // Draw the cube
	// GLES20.glDrawElements(GLES20.GL_TRIANGLES,
	// this.cubePrimative.getNumIndices(), GLES20.GL_UNSIGNED_SHORT,
	// this.cubePrimative.getIndices());
	// }

	//
	// Handle surface changes
	//
	public void onSurfaceChanged(GL10 glUnused, int w, int h) {
		this.width = w;
		this.height = h;

		// Compute the window aspect ratio
		this.aspect = (float) this.width / (float) this.height;
		// Generate a perspective matrix with a 60 degree FOV
		this.perspective.matrixLoadIdentity();

		if (this.aspect > 1.0f)
			this.perspective.perspective(60.0f, this.aspect, 0.1f, 10.0f);
		else
			this.perspective.perspective(80.0f, this.aspect, 0.1f, 10.0f);

		// Set the viewport
		GLES20.glViewport(0, 0, this.width, this.height);
	}

	/**
	 * Update the Model View matrix with animated transformations (e.g.
	 * rotations)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	// private void update(float x, float y, float z) {
	// // Generate a model view matrix to rotate/translate the cube
	// this.modelview.matrixLoadIdentity();
	// // Translate away from the viewer
	// this.modelview.translate(x, y, -4f);
	// // Rotate the cube
	// this.modelview.rotate(this.currentAngle, 0.0f, 1.0f, 0.0f);
	// // Compute the final MVP by multiplying the
	// // modelview and perspective matrices together
	// this.mMVPMatrix.matrixMultiply(this.modelview.get(),
	// this.perspective.get());
	// }

	// For debug. Report OpenGl Error
	static void checkGLError(GL gl) {
		final int error = GLES20.glGetError();
		if (error != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("GLError 0x"
					+ Integer.toHexString(error));
		}
	}

	public void onDestroy() {
		// kill off threads
		this.killThreads = true;
		// delete shaders and program from GPU state.
		ESShader.cleanup();
	}

	public File getImage() {
		try {

			Bitmap bitmap = FractalFactory.convertToBitmap(fractal);

			File storageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"Fractal Factory");
			storageDir.mkdirs();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			File imageFile = File.createTempFile(timeStamp + "_", ".png",
					storageDir.getAbsoluteFile());

			FileOutputStream fout = new FileOutputStream(
					imageFile.getAbsolutePath());
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
			fout.flush();
			fout.close();

			return imageFile;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// Handle to a program object
	private int programObject;

	// Attribute locations
	private int positionLoc;
	private int texCoordLoc;

	// Additional member variables
	private int width;
	private int height;

	// Uniform locations
	private int mvpLoc;

	// Vertex data
	private final ESShapes cubePrimative = new ESShapes();

	// Rotation angle
	private float currentAngle;

	// MVP matrix
	private final ESTransform mMVPMatrix = new ESTransform();

	private long lastTime = 0;

	private Thread produceFractalThread;
	
	private boolean rendered = false;
	// private Thread consumeFractalThread;

	private final ESTransform perspective = new ESTransform();
	private final ESTransform modelview = new ESTransform();

	// Texture object handle
	final int textureIdMax = 2;
	final float angleInc = 360.0f / textureIdMax;
	private final int[] textureIds = new int[textureIdMax];
	volatile private boolean produced = false;
	private boolean[] textured = new boolean[textureIdMax];

	// this all important value specifies the final fractal bitmap resolution
	// that will generated. It is a factor of 2 to work with OpenGl texture
	// mapping
	// functions. TODO: This should be adjusted dynamically for device screen
	// res.
	private final int kMaxDim = 512;

	private float aspect;

	// The value of these variables will never be cached thread-locally:
	// all reads and writes will go straight to "main memory";
	volatile private IFractal fractal, testFractal;
	// volatile protected boolean fractalProduced = false;

	// lock used for synchronizing the fractal bitmap produce/consume threads
	private final Object lock = new Object();
	protected boolean killThreads;

	// GLSL shader language strings
	private final String vShaderStr;
	private final String fShaderStr;

	private int textureIdIndex = 0;
	private boolean enableRotate;

	private boolean initialized; 

}
