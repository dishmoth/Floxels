/*
 *  Perlin3D.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.util.*;

// noise generator (three-dimensional Perlin-esque noise)
public class Perlin3D {

  // number of dimensions
  static private final int kNumDims = 3;
  
  // maximum (absolute) returned value
  static private final float kMaxVal = 0.5f*(float)Math.sqrt(kNumDims) + 1e-7f;
  
  // number of unique random gradients
  static private final int kNumGradients = 256; 
  
  // a flattened list of random unit vectors
  private float mGradient[] = null;
  
  // random permutation of the gradient indices
  private int mIndex[] = null;
  
  // constructor
  public Perlin3D(long seed) {

    Random rand = new Random(seed);
    
    mGradient = new float[kNumDims*kNumGradients];
    for ( int k = 0 ; k < kNumGradients ; k++ ) {
      float x, y, z;
      float d2;
      do {
        x = 2.0f*rand.nextFloat() - 1.0f;
        y = 2.0f*rand.nextFloat() - 1.0f;
        z = 2.0f*rand.nextFloat() - 1.0f;
        d2 = x*x + y*y + z*z;
      } while ( d2 > 1.0f || d2 < 0.01 );
      float d = (float)Math.sqrt(d2);
      mGradient[kNumDims*k  ] = x/d;
      mGradient[kNumDims*k+1] = y/d;
      mGradient[kNumDims*k+2] = z/d;      
    }
    
    mIndex = new int[kNumGradients];
    ArrayList<Integer> randomOrder = new ArrayList<Integer>(kNumGradients);
    for ( int k = 0 ; k < kNumGradients ; k++ ) randomOrder.add(k);
    Collections.shuffle(randomOrder, rand);
    for ( int k = 0 ; k < kNumGradients ; k++ ) mIndex[k] = randomOrder.get(k);
    
  } // constructor

  // maximum (absolute) value that may be returned 
  static public float maximum() { return kMaxVal; }
  
  // amount in each dimension before the noise pattern repeats
  static public float period() { return kNumGradients; }
  
  // evaluate the noise value at a three-dimensional position 
  public float value(float x, float y, float z) {
    
    final int ix = (x >= 0) ? (int)x : (int)Math.floor(x),
              iy = (y >= 0) ? (int)y : (int)Math.floor(y),
              iz = (z >= 0) ? (int)z : (int)Math.floor(z);
    
    final float fx = x - ix,
                fy = y - iy,
                fz = z - iz;
    
    final float tx = (3.0f - 2.0f*fx)*fx*fx,
                ty = (3.0f - 2.0f*fy)*fy*fy,
                tz = (3.0f - 2.0f*fz)*fz*fz;

    final float v000 = gridVal(ix,  iy,  iz  , fx,  fy,  fz  ),
                v001 = gridVal(ix,  iy,  iz+1, fx,  fy,  fz-1),
                v010 = gridVal(ix,  iy+1,iz  , fx,  fy-1,fz  ),
                v011 = gridVal(ix,  iy+1,iz+1, fx  ,fy-1,fz-1),
                v100 = gridVal(ix+1,iy,  iz  , fx-1,fy  ,fz  ),
                v101 = gridVal(ix+1,iy,  iz+1, fx-1,fy  ,fz-1),
                v110 = gridVal(ix+1,iy+1,iz  , fx-1,fy-1,fz  ),
                v111 = gridVal(ix+1,iy+1,iz+1, fx-1,fy-1,fz-1);

    final float v00 = (1-tz)*v000 + tz*v001,
                v01 = (1-tz)*v010 + tz*v011,
                v10 = (1-tz)*v100 + tz*v101,
                v11 = (1-tz)*v110 + tz*v111;
    
    final float v0 = (1-ty)*v00 + ty*v01,
                v1 = (1-ty)*v10 + ty*v11;
    
    final float v = (1-tx)*v0 + tx*v1;
    assert( v >= -kMaxVal && v <= +kMaxVal );
    
    return v;
    
  } // Function.value()
  
  // the noise contribution from an individual grid point
  private float gridVal(int ix, int iy, int iz, 
                        float dx, float dy, float dz) {
    
    final int a = mIndex[ ix & 0xFF ],
              b = mIndex[ (a + iy) & 0xFF ],
              c = mIndex[ (b + iz) & 0xFF ];    
    final int offset = kNumDims*c;
    
    final float dot = dx*mGradient[offset]
                    + dy*mGradient[offset+1] 
                    + dz*mGradient[offset+2];
    return dot;
    
  } // gridVal();
  
} // class Perlin3D
