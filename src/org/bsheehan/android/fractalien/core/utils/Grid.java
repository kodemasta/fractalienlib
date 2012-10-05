package org.bsheehan.android.fractalien.core.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLES11;
import android.opengl.GLES20;

/**
 * A grid is a topologically rectangular array of vertices.
 * 
 * This grid class is customized for the vertex data required for this example.
 * 
 * The vertex and index data are held in VBO objects because on most GPUs VBO
 * objects are the fastest way of rendering static vertex and index data.
 * 
 */

public class Grid {
	// Size of vertex data elements in bytes:
	final static int FLOAT_SIZE = 4;
	final static int CHAR_SIZE = 2;

	// Vertex structure:
	// float x, y, z;
	// float nx, ny, nx;

	final static int VERTEX_SIZE = 8 * FLOAT_SIZE;
	final static int VERTEX_NORMAL_BUFFER_INDEX_OFFSET = 3;
	final static int VERTEX_TEXTURE_BUFFER_INDEX_OFFSET = 6;
	private int mVertexBufferObjectId;
	private int mElementBufferObjectId;

	// These buffers are used to hold the vertex and index data while
	// constructing the grid. Once createBufferObjects() is called
	// the buffers are nulled out to save memory.

	private ByteBuffer mVertexByteBuffer;
	private FloatBuffer mVertexBuffer;
	private CharBuffer mIndexBuffer;

	private int mW;
	private int mH;
	private int mIndexCount;

	public Grid(int w, int h) {
		if (w < 0 || w >= 65536) {
			throw new IllegalArgumentException("w");
		}
		if (h < 0 || h >= 65536) {
			throw new IllegalArgumentException("h");
		}
		if (w * h >= 65536) {
			throw new IllegalArgumentException("w * h >= 65536");
		}

		mW = w;
		mH = h;
		int size = w * h;

		mVertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_SIZE * size)
				.order(ByteOrder.nativeOrder());
		mVertexBuffer = mVertexByteBuffer.asFloatBuffer();

		int quadW = mW - 1;
		int quadH = mH - 1;
		int quadCount = quadW * quadH;
		int indexCount = quadCount * 6;
		mIndexCount = indexCount;
		mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(
				ByteOrder.nativeOrder()).asCharBuffer();

		/*
		 * Initialize triangle list mesh.
		 * 
		 * [0]-----[ 1] ... | / | | / | | / | [w]-----[w+1] ... | |
		 */

		{
			int i = 0;
			for (int y = 0; y < quadH; y++) {
				for (int x = 0; x < quadW; x++) {
					char a = (char) (y * mW + x);
					char b = (char) (y * mW + x + 1);
					char c = (char) ((y + 1) * mW + x);
					char d = (char) ((y + 1) * mW + x + 1);

					mIndexBuffer.put(i++, a);
					mIndexBuffer.put(i++, c);
					mIndexBuffer.put(i++, b);

					mIndexBuffer.put(i++, b);
					mIndexBuffer.put(i++, c);
					mIndexBuffer.put(i++, d);
				}
			}
		}
	}

	public void set(int i, int j, float x, float y, float z, float nx,
			float ny, float nz, float ty, float tx) {
		if (i < 0 || i >= mW) {
			throw new IllegalArgumentException("i");
		}
		if (j < 0 || j >= mH) {
			throw new IllegalArgumentException("j");
		}

		int index = mW * j + i;

		mVertexBuffer.position(index * VERTEX_SIZE / FLOAT_SIZE);
		mVertexBuffer.put(x);
		mVertexBuffer.put(y);
		mVertexBuffer.put(z);
		mVertexBuffer.put(nx);
		mVertexBuffer.put(ny);
		mVertexBuffer.put(nz);
		mVertexBuffer.put(tx);
		mVertexBuffer.put(ty);

	}

	public void createBufferObjects(GL gl) {
		checkGLError(gl);
		// Generate a the vertex and element buffer IDs
		int[] vboIds = new int[2];
		GL11 gl11 = (GL11) gl;
		gl11.glGenBuffers(2, vboIds, 0);
		mVertexBufferObjectId = vboIds[0];
		mElementBufferObjectId = vboIds[1];

		// Upload the vertex data
		gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
		mVertexByteBuffer.position(0);
		gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexByteBuffer.capacity(),
				mVertexByteBuffer, GL11.GL_STATIC_DRAW);

		gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
		mIndexBuffer.position(0);
		gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity()
				* CHAR_SIZE, mIndexBuffer, GL11.GL_STATIC_DRAW);

		// We don't need the in-memory data any more
		mVertexBuffer = null;
		mVertexByteBuffer = null;
		mIndexBuffer = null;
		checkGLError(gl);
	}

	public void draw(GL10 gl) {
		checkGLError(gl);
		//android.opengl.GLES11 gl11 = (android.opengl.GLES11) gl;

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
		GLES11.glVertexPointer(3, GLES11.GL_FLOAT, VERTEX_SIZE, 0);

		gl.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glNormalPointer(GLES11.GL_FLOAT, VERTEX_SIZE,
				VERTEX_NORMAL_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE,
				VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

		GLES11.glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
		
		GLES11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount,
				GL10.GL_UNSIGNED_SHORT, 0);
		
		 
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
		GLES11.glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, 0);
		checkGLError(gl);
	}

	static void checkGLError(GL gl) {
		int error = ((GL10) gl).glGetError();
		if (error != GL10.GL_NO_ERROR) {
			throw new RuntimeException("GLError 0x"
					+ Integer.toHexString(error));
		}
	}
}
