package org.bsheehan.android.fractalien.core.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

public class ESTransform
{	
	public ESTransform()
	{
		this.mMatrixFloatBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public void scale(float sx, float sy, float sz)
	{
		this.mMatrix[0 * 4 + 0] *= sx;
		this.mMatrix[0 * 4 + 1] *= sx;
		this.mMatrix[0 * 4 + 2] *= sx;
		this.mMatrix[0 * 4 + 3] *= sx;

		this.mMatrix[1 * 4 + 0] *= sy;
		this.mMatrix[1 * 4 + 1] *= sy;
		this.mMatrix[1 * 4 + 2] *= sy;
		this.mMatrix[1 * 4 + 3] *= sy;

		this.mMatrix[2 * 4 + 0] *= sz;
		this.mMatrix[2 * 4 + 1] *= sz;
		this.mMatrix[2 * 4 + 2] *= sz;
		this.mMatrix[2 * 4 + 3] *= sz;
	}

	public void translate(float tx, float ty, float tz)
	{
		this.mMatrix[3 * 4 + 0] += (this.mMatrix[0 * 4 + 0] * tx + this.mMatrix[1 * 4 + 0] * ty + this.mMatrix[2 * 4 + 0] * tz);
		this.mMatrix[3 * 4 + 1] += (this.mMatrix[0 * 4 + 1] * tx + this.mMatrix[1 * 4 + 1] * ty + this.mMatrix[2 * 4 + 1] * tz);
		this.mMatrix[3 * 4 + 2] += (this.mMatrix[0 * 4 + 2] * tx + this.mMatrix[1 * 4 + 2] * ty + this.mMatrix[2 * 4 + 2] * tz);
		this.mMatrix[3 * 4 + 3] += (this.mMatrix[0 * 4 + 3] * tx + this.mMatrix[1 * 4 + 3] * ty + this.mMatrix[2 * 4 + 3] * tz);
	}

	public void rotate(float angle, float x, float y, float z)
	{
		float sinAngle, cosAngle;
		final float mag = (float)Math.sqrt((x * x + y * y + z * z));

		sinAngle = (float)Math.sin ( (angle * Math.PI / 180.0 ));
		cosAngle = (float)Math.cos ( (angle * Math.PI / 180.0 ));
		if ( mag > 0.0f )
		{
			float xx, yy, zz, xy, yz, zx, xs, ys, zs;
			float oneMinusCos;
			final float[] rotMat = new float[16];

			x /= mag;
			y /= mag;
			z /= mag;

			xx = x * x;
			yy = y * y;
			zz = z * z;
			xy = x * y;
			yz = y * z;
			zx = z * x;
			xs = x * sinAngle;
			ys = y * sinAngle;
			zs = z * sinAngle;
			oneMinusCos = 1.0f - cosAngle;

			rotMat[0 * 4 + 0] = (oneMinusCos * xx) + cosAngle;
			rotMat[0 * 4 + 1] = (oneMinusCos * xy) - zs;
			rotMat[0 * 4 + 2] = (oneMinusCos * zx) + ys;
			rotMat[0 * 4 + 3] = 0.0F; 

			rotMat[1 * 4 + 0] = (oneMinusCos * xy) + zs;
			rotMat[1 * 4 + 1] = (oneMinusCos * yy) + cosAngle;
			rotMat[1 * 4 + 2] = (oneMinusCos * yz) - xs;
			rotMat[1 * 4 + 3] = 0.0F;

			rotMat[2 * 4 + 0] = (oneMinusCos * zx) - ys;
			rotMat[2 * 4 + 1] = (oneMinusCos * yz) + xs;
			rotMat[2 * 4 + 2] = (oneMinusCos * zz) + cosAngle;
			rotMat[2 * 4 + 3] = 0.0F; 

			rotMat[3 * 4 + 0] = 0.0F;
			rotMat[3 * 4 + 1] = 0.0F;
			rotMat[3 * 4 + 2] = 0.0F;
			rotMat[3 * 4 + 3] = 1.0F;

			matrixMultiply( rotMat, this.mMatrix );
		}
	}
	
	final float PI_180 = (float) (Math.PI / 180.0); 
	public void rotateY(float angle)
	{
		float sinAngle, cosAngle;
		sinAngle = android.util.FloatMath.sin(angle * PI_180);
		cosAngle = android.util.FloatMath.cos(angle * PI_180);
		//if ( mag > 0.0f )
		{
			float yy, xs, ys, zs;
			float oneMinusCos;
			final float[] rotMat = new float[16];


			yy = 1.0f;
			xs = 0.0f;;
			ys = sinAngle;
			zs = 0.0f;
			oneMinusCos = 1.0f - cosAngle;

			rotMat[0 * 4 + 0] = cosAngle;
			rotMat[0 * 4 + 1] = -zs;
			rotMat[0 * 4 + 2] =  ys;
			rotMat[0 * 4 + 3] = 0.0F; 

			rotMat[1 * 4 + 0] = zs;
			rotMat[1 * 4 + 1] = (oneMinusCos * yy) + cosAngle;
			rotMat[1 * 4 + 2] = -xs;
			rotMat[1 * 4 + 3] = 0.0F;

			rotMat[2 * 4 + 0] = -ys;
			rotMat[2 * 4 + 1] =  xs;
			rotMat[2 * 4 + 2] = cosAngle;
			rotMat[2 * 4 + 3] = 0.0F; 

			rotMat[3 * 4 + 0] = 0.0F;
			rotMat[3 * 4 + 1] = 0.0F;
			rotMat[3 * 4 + 2] = 0.0F;
			rotMat[3 * 4 + 3] = 1.0F;

			matrixMultiply( rotMat, this.mMatrix );
		}
	}


	public void frustum( float left, float right, float bottom, float top, float nearZ, float farZ)
	{
		final float       deltaX = right - left;
		final float       deltaY = top - bottom;
		final float       deltaZ = farZ - nearZ;
		final float[]     frust = new float[16];

		if ( (nearZ <= 0.0f) || (farZ <= 0.0f) ||
				(deltaX <= 0.0f) || (deltaY <= 0.0f) || (deltaZ <= 0.0f) )
			return;

		frust[0 * 4 + 0] = 2.0f * nearZ / deltaX;
		frust[0 * 4 + 1] = frust[0 * 4 + 2] = frust[0 * 4 + 3] = 0.0f;

		frust[1 * 4 + 1] = 2.0f * nearZ / deltaY;
		frust[1 * 4 + 0] = frust[1 * 4 + 2] = frust[1 * 4 + 3] = 0.0f;

		frust[2 * 4 + 0] = (right + left) / deltaX;
		frust[2 * 4 + 1] = (top + bottom) / deltaY;
		frust[2 * 4 + 2] = -(nearZ + farZ) / deltaZ;
		frust[2 * 4 + 3] = -1.0f;

		frust[3 * 4 + 2] = -2.0f * nearZ * farZ / deltaZ;
		frust[3 * 4 + 0] = frust[3 * 4 + 1] = frust[3 * 4 + 3] = 0.0f;

		matrixMultiply(frust, this.mMatrix);
	}


	public void perspective(float fovy, float aspect, float nearZ, float farZ)
	{
		float frustumW, frustumH;

		frustumH = (float)Math.tan( fovy / 360.0 * Math.PI ) * nearZ;
		frustumW = frustumH * aspect;

		frustum( -frustumW, frustumW, -frustumH, frustumH, nearZ, farZ );
	}

	public void ortho(float left, float right, float bottom, float top, float nearZ, float farZ)
	{
		final float       deltaX = right - left;
		final float       deltaY = top - bottom;
		final float       deltaZ = farZ - nearZ;
		final float[]     orthoMat = makeIdentityMatrix();

		if ( (deltaX == 0.0f) || (deltaY == 0.0f) || (deltaZ == 0.0f) )
			return;


		orthoMat[0 * 4 + 0] = 2.0f / deltaX;
		orthoMat[3 * 4 + 0] = -(right + left) / deltaX;
		orthoMat[1 * 4 + 1] = 2.0f / deltaY;
		orthoMat[3 * 4 + 1] = -(top + bottom) / deltaY;
		orthoMat[2 * 4 + 2] = -2.0f / deltaZ;
		orthoMat[3 * 4 + 2] = -(nearZ + farZ) / deltaZ;

		matrixMultiply(orthoMat, this.mMatrix);
	}


	public void matrixMultiply(float[] srcA, float[] srcB)
	{
		final float[] tmp = new float[16];
		int         i;

		for (i=0; i<4; i++)
		{
			tmp[i * 4 + 0] =	(srcA[i * 4 + 0] * srcB[0 * 4 + 0]) +
			(srcA[i * 4 + 1] * srcB[1 * 4 + 0]) +
			(srcA[i * 4 + 2] * srcB[2 * 4 + 0]) +
			(srcA[i * 4 + 3] * srcB[3 * 4 + 0]) ;

			tmp[i * 4 + 1] =	(srcA[i * 4 + 0] * srcB[0 * 4 + 1]) + 
			(srcA[i * 4 + 1] * srcB[1 * 4 + 1]) +
			(srcA[i * 4 + 2] * srcB[2 * 4 + 1]) +
			(srcA[i * 4 + 3] * srcB[3 * 4 + 1]) ;

			tmp[i * 4 + 2] =	(srcA[i * 4 + 0] * srcB[0 * 4 + 2]) + 
			(srcA[i * 4 + 1] * srcB[1 * 4 + 2]) +
			(srcA[i * 4 + 2] * srcB[2 * 4 + 2]) +
			(srcA[i * 4 + 3] * srcB[3 * 4 + 2]) ;

			tmp[i * 4 + 3] =	(srcA[i * 4 + 0] * srcB[0 * 4 + 3]) + 
			(srcA[i * 4 + 1] * srcB[1 * 4 + 3]) +
			(srcA[i * 4 + 2] * srcB[2 * 4 + 3]) +
			(srcA[i * 4 + 3] * srcB[3 * 4 + 3]) ;
		}

		this.mMatrix = tmp;
	}

	public void matrixLoadIdentity()
	{
		for (int i = 0; i < 16; i++ )
			this.mMatrix[i] = 0.0f;

		this.mMatrix[0 * 4 + 0] = 1.0f;
		this.mMatrix[1 * 4 + 1] = 1.0f;
		this.mMatrix[2 * 4 + 2] = 1.0f;
		this.mMatrix[3 * 4 + 3] = 1.0f;
	}

	private float[] makeIdentityMatrix()
	{
		final float[] result = new float[16];

		for (int i = 0; i < 16; i++ )
			result[i] = 0.0f;

		result[0 * 4 + 0] = 1.0f;
		result[1 * 4 + 1] = 1.0f;
		result[2 * 4 + 2] = 1.0f;
		result[3 * 4 + 3] = 1.0f;

		return result;		
	}

	public FloatBuffer getAsFloatBuffer()
	{
		this.mMatrixFloatBuffer.put(this.mMatrix).position(0);
		return this.mMatrixFloatBuffer;		
	}

	public float[] get()
	{
		return this.mMatrix;
	}

	private float[] mMatrix = new float[16]; 
	private final FloatBuffer mMatrixFloatBuffer;

}