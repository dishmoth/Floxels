/*
 *  HoopPainter.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.NumberUtils;

// class for drawing circles
public class HoopPainter {

  // number of points to draw around a circle
  final private int mNumPoints;
  
  // pairs of [dx0,dy0,dx1,dy1...] at steps around a circle 
  private float mUnitCircle[];
  
  // array to hold vertex data for the mesh
  private float mVertices[];
  
  // mesh object
  private Mesh mMesh;
  
  // reference to the texture
  private Texture mTexture;
  
  // region of the texure to use (texture units)
  private float mU0,
                mU1,
                mV0,
                mV1;
  
  // thickness of the circle (in pixels)
  private float mThickness;
  
  // constructor
  public HoopPainter(int tileWidth) {
    
    mNumPoints = 64;
    
    mUnitCircle = new float[2*(mNumPoints+1)];
    for ( int k = 0 ; k <= mNumPoints ; k++ ) {
      double theta = 2.0*Math.PI*(k%mNumPoints)/(float)mNumPoints;
      mUnitCircle[2*k]   = (float)Math.cos(theta);
      mUnitCircle[2*k+1] = -(float)Math.sin(theta);
    }
    
    mVertices = new float[5*2*(mNumPoints+1)];

    mMesh = new Mesh(false, 2*(mNumPoints+1), 0,
                     new VertexAttribute( 
                               VertexAttributes.Usage.Position, 2,
                               ShaderProgram.POSITION_ATTRIBUTE ),
                     new VertexAttribute( 
                               VertexAttributes.Usage.ColorPacked, 4,
                               ShaderProgram.COLOR_ATTRIBUTE ),
                     new VertexAttribute( 
                               VertexAttributes.Usage.TextureCoordinates, 2,
                               ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );
    
  } // constructor
  
  // specify the patch of texture to use
  public void setTexture(TextureRegion tex) {
    
    assert( mTexture == null );
    mTexture = tex.getTexture();
    
    mU0 = tex.getU();
    mU1 = tex.getU2();
    mV0 = tex.getV();
    mV1 = tex.getV2();
    
    mThickness = tex.getRegionHeight();
    
  } // setTexture()
  
  // replace the texture (following game pause/resume)
  public void resetTexture(Texture texture) { mTexture = texture; }
  
  // draw a circle
  // (note: the SpriteBatch's shader, etc. is reused, which is a little dodgy)
  void drawHoop(SpriteBatch batch, float x, float y, float r, float alpha) {
    
    batch.flush();
    
    assert( r > 0.0f );
    
    x = x*Env.tileWidth() + Env.gameOffsetX();
    y = y*Env.tileWidth() + Env.gameOffsetY();
    r *= Env.tileWidth();
    float dr = 0.5f*mThickness;
    
    float r0 = Math.max(0.0f, r-dr),
          r1 = r+dr;

    int colBits = ((int)(255 * alpha) << 24) | (255<<16) | (255<<8) | (255);
    float colour = NumberUtils.intToFloatColor(colBits);
    
    int i = 0;
    for ( int k = 0 ; k <= mNumPoints ; k++ ) {
      float dx = mUnitCircle[2*k],
            dy = mUnitCircle[2*k+1];
      
      mVertices[i++] = x + r0*dx;
      mVertices[i++] = y + r0*dy;
      mVertices[i++] = colour;
      mVertices[i++] = mU0;
      mVertices[i++] = mV0;
      
      mVertices[i++] = x + r1*dx;
      mVertices[i++] = y + r1*dy;
      mVertices[i++] = colour;
      mVertices[i++] = mU1;
      mVertices[i++] = mV1;
    }
    
    mMesh.setVertices(mVertices);
    
    mMesh.render(batch.getShader(), GL20.GL_TRIANGLE_STRIP);
    
  } // drawHoop()
  
} // class HoopPainter
