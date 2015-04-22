/*
 *  Perlin2D.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.*;

// noise generator (classical two-dimensional Perlin noise, made periodic)
public class Perlin2D {

  // number of dimensions
  static private final int kNumDims = 2;
  
  // maximum (absolute) returned value
  static private final float kMaxVal = 0.5f*(float)Math.sqrt(kNumDims) + 1e-7f;
  
  // a flattened list of random unit vectors
  private final float mGradient[];
  
  // random permutation of the gradient indices
  private final int mIndex[];

  // period on which the noise repeats
  private final int mPeriod;
  
  // constructor
  public Perlin2D(long seed, int period) {

    Random rand = new Random(seed);

    assert( period > 0 );
    mPeriod = period;
    
    final int numGradients = mPeriod;
    mGradient = new float[kNumDims*numGradients];
    for ( int k = 0 ; k < numGradients ; k++ ) {
      float x, y;
      float d2;
      do {
        x = 2.0f*rand.nextFloat() - 1.0f;
        y = 2.0f*rand.nextFloat() - 1.0f;
        d2 = x*x + y*y;
      } while ( d2 > 1.0f || d2 < 0.01 );
      float d = (float)Math.sqrt(d2);
      mGradient[kNumDims*k  ] = x/d;
      mGradient[kNumDims*k+1] = y/d;
    }
    
    mIndex = new int[numGradients];
    ArrayList<Integer> randomOrder = new ArrayList<Integer>(numGradients);
    for ( int k = 0 ; k < numGradients ; k++ ) randomOrder.add(k);
    Collections.shuffle(randomOrder, rand);
    for ( int k = 0 ; k < numGradients ; k++ ) mIndex[k] = randomOrder.get(k);
    
  } // constructor

  // maximum (absolute) value that may be returned 
  static public float maximum() { return kMaxVal; }
  
  // amount in each dimension before the noise pattern repeats
  public float period() { return mPeriod; }
  
  // evaluate the noise value at a two-dimensional position 
  public float value(float x, float y) {
    
    final int ix = (int)Math.floor(x),
              iy = (int)Math.floor(y);
    
    final float fx = x - ix,
                fy = y - iy;
    
    // cubic interpolation:
    //final float tx = (3.0f - 2.0f*fx)*fx*fx,
    //            ty = (3.0f - 2.0f*fy)*fy*fy;

    // quintic interpolation:
    final float tx = (6*fx*fx - 15*fx + 10)*fx*fx*fx,
                ty = (6*fy*fy - 15*fy + 10)*fy*fy*fy;
    
    final float v00 = gridVal(ix,  iy,   fx,  fy  ),
                v01 = gridVal(ix,  iy+1, fx,  fy-1),
                v10 = gridVal(ix+1,iy,   fx-1,fy  ),
                v11 = gridVal(ix+1,iy+1, fx-1,fy-1);

    final float v0 = (1-ty)*v00 + ty*v01,
                v1 = (1-ty)*v10 + ty*v11;
    
    final float v = (1-tx)*v0 + tx*v1;
    assert( v >= -kMaxVal && v <= +kMaxVal );
    
    return v;
    
  } // Function.value()
  
  // the noise contribution from an individual grid point
  private float gridVal(int ix, int iy, float dx, float dy) {
    
    final int a = mIndex[ Env.fold(ix, mPeriod) ],
              b = mIndex[ Env.fold((a + iy), mPeriod) ];    
    final int offset = kNumDims*b;
    
    final float dot = dx*mGradient[offset]
                    + dy*mGradient[offset+1];
    return dot;
    
  } // gridVal();
  
} // class Perlin2D
