package org.bsheehan.android.fractalien.core.utils;

/**
 * NOT WRITTEN BY BSHEEHAN@BAYMOON.COM
 * 
 * Book:      OpenGL(R) ES 2.0 Programming Guide
 * @author:   Aaftab Munshi, Dan Ginsburg, Dave Shreiner
 * ISBN-10:   0321502795
 * ISBN-13:   9780321502797
 * Publisher: Addison-Wesley Professional
 * URLs:      http://safari.informit.com/9780321563835
 * 		      http://www.opengles-book.com
 **/

//ESShapes
//
// Utility class for generating shapes
//

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ESShapes {

	public int genSphere(int numSlices, float radius) {
		int i;
		int j;
		final int numParallels = numSlices;
		final int numVertices = (numParallels + 1) * (numSlices + 1);
		final int numIndices = numParallels * numSlices * 6;
		final float angleStep = ((2.0f * (float) Math.PI) / numSlices);

		// Allocate memory for buffers
		this.mVertices = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mNormals = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mTexCoords = ByteBuffer.allocateDirect(numVertices * 2 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mIndices = ByteBuffer.allocateDirect(numIndices * 2).order(
				ByteOrder.nativeOrder()).asShortBuffer();

		for (i = 0; i < numParallels + 1; i++) {
			for (j = 0; j < numSlices + 1; j++) {
				final int vertex = (i * (numSlices + 1) + j) * 3;

				this.mVertices.put(vertex + 0, (float) (radius
						* Math.sin(angleStep * i) * Math.sin(angleStep
								* j)));

				this.mVertices.put(vertex + 1, (float) (radius * Math.cos(angleStep
						* i)));
				this.mVertices.put(vertex + 2, (float) (radius
						* Math.sin(angleStep * i) * Math.cos(angleStep
								* j)));

				this.mNormals.put(vertex + 0, this.mVertices.get(vertex + 0) / radius);
				this.mNormals.put(vertex + 1, this.mVertices.get(vertex + 1) / radius);
				this.mNormals.put(vertex + 2, this.mVertices.get(vertex + 2) / radius);

				final int texIndex = (i * (numSlices + 1) + j) * 2;
				this.mTexCoords.put(texIndex + 0, (float) j / (float) numSlices);
				this.mTexCoords.put(texIndex + 1, (1.0f - i)
						/ (numParallels - 1));
			}
		}

		int index = 0;
		for (i = 0; i < numParallels; i++) {
			for (j = 0; j < numSlices; j++) {
				this.mIndices.put(index++, (short) (i * (numSlices + 1) + j));
				this.mIndices.put(index++, (short) ((i + 1) * (numSlices + 1) + j));
				this.mIndices.put(index++,
						(short) ((i + 1) * (numSlices + 1) + (j + 1)));

				this.mIndices.put(index++, (short) (i * (numSlices + 1) + j));
				this.mIndices.put(index++,
						(short) ((i + 1) * (numSlices + 1) + (j + 1)));
				this.mIndices.put(index++, (short) (i * (numSlices + 1) + (j + 1)));

			}
		}
		this.mNumIndices = numIndices;

		return numIndices;
	}
	
	public int genSquare(float scale) {
		int i;
		final int numVertices = 4;
		final int numIndices = 6;

		final float[] cubeVerts = {
				// SIDE 4
				-0.5f, -0.5f, -1.0f, 
				-0.5f, 0.5f, -1.0f,
				0.5f, 0.5f, -1.0f, 
				0.5f, -0.5f, -1.0f 
		};

		final float[] cubeNormals = {
				// SIDE 4
				0.0f, 0.0f, 1.0f, 
				0.0f, 0.0f, 1.0f, 
				0.0f, 0.0f, 1.0f, 
				0.0f, 0.0f, 1.0f,
		};

		final float[] cubeTex = { 

				0.0f, 0.0f, 
				0.0f, 1.0f, 
				1.0f, 1.0f, 
				1.0f, 0.0f,
		};

		// Allocate memory for buffers
		this.mVertices = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mNormals = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mTexCoords = ByteBuffer.allocateDirect(numVertices * 2 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mIndices = ByteBuffer.allocateDirect(numIndices * 2).order(
				ByteOrder.nativeOrder()).asShortBuffer();

		this.mVertices.put(cubeVerts).position(0);
		for (i = 0; i < numVertices; i++) {
			this.mVertices.put(i, this.mVertices.get(i) * scale);
		}

		this.mNormals.put(cubeNormals).position(0);
		this.mTexCoords.put(cubeTex).position(0);

		final short[] cubeIndices = { 0, 2, 1, 0, 3, 2, };

		this.mIndices.put(cubeIndices).position(0);
		this.mNumIndices = numIndices;
		return numIndices;
	}



	public int genCube(float scale) {
		int i;
		final int numVertices = 24;
		final int numIndices = 36;

		final float[] cubeVerts = {
				// SIDE 1
				-0.5f, -0.5f, -0.5f, 	//0 
				-0.5f, -0.5f, 0.5f, 	//1
				0.5f,-0.5f, 0.5f,		//2	
				0.5f,-0.5f, -0.5f,		//3
				// SIDE 2
				-0.5f, 0.5f, -0.5f, 	//4
				-0.5f, 0.5f, 0.5f, 		//5
				0.5f, 0.5f, 0.5f,		//6
				0.5f,0.5f, -0.5f,		//7
				// SIDE 3
				-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
				0.5f,
				-0.5f,
				-0.5f,
				// SIDE 4
				-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
				-0.5f,
				0.5f,
				// SIDE 5
				-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, -0.5f,
				// SIDE 6
				0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
				0.5f, -0.5f, };

		final float[] cubeNormals = {
				// SIDE 1
				0.0f, -1.0f, 0.0f, 
				0.0f, -1.0f, 0.0f, 
				0.0f,
				-1.0f,
				0.0f,
				0.0f,
				-1.0f,
				0.0f,
				// SIDE 2
				0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f,
				0.0f,
				1.0f,
				0.0f,
				// SIDE 3
				0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
				0.0f,
				0.0f,
				-1.0f,
				// SIDE 4
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f,
				1.0f,
				// SIDE 5
				-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
				0.0f, 0.0f,
				// SIDE 6
				1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, };

		final float[] cubeTex = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

				1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,

				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,

				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, };

		// Allocate memory for buffers
		this.mVertices = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mNormals = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mTexCoords = ByteBuffer.allocateDirect(numVertices * 2 * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		this.mIndices = ByteBuffer.allocateDirect(numIndices * 2).order(
				ByteOrder.nativeOrder()).asShortBuffer();

		this.mVertices.put(cubeVerts).position(0);
		for (i = 0; i < numVertices; i++) {
			this.mVertices.put(i, this.mVertices.get(i) * scale);
		}

		this.mNormals.put(cubeNormals).position(0);
		this.mTexCoords.put(cubeTex).position(0);

		final short[] cubeIndices = { 0, 2, 1, 0, 3, 2, 4, 5, 6, 4, 6, 7, 8, 9, 10,
				8, 10, 11, 12, 15, 14, 12, 14, 13, 16, 17, 18, 16, 18, 19, 20,
				23, 22, 20, 22, 21 };

		this.mIndices.put(cubeIndices).position(0);
		this.mNumIndices = numIndices;
		return numIndices;
	}

	public FloatBuffer getVertices() {
		return this.mVertices;
	}

	public FloatBuffer getNormals() {
		return this.mNormals;
	}

	public FloatBuffer getTexCoords() {
		return this.mTexCoords;
	}

	public ShortBuffer getIndices() {
		return this.mIndices;
	}

	public int getNumIndices() {
		return this.mNumIndices;
	}

	// Member variables
	private FloatBuffer mVertices;
	private FloatBuffer mNormals;
	private FloatBuffer mTexCoords;
	private ShortBuffer mIndices;
	private int mNumIndices;
}
