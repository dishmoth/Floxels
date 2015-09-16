/*
 *  Clusters.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.Arrays;

// utility for sorting particles into clusters
public class Clusters {

  // cluster scores range from 0 to maxClusterScore()
  static public int maxClusterScore() { return 100; }
  
  // the object we're clustering for
  private final Flow mOwner;
  
  // total number of particles
  private final int mMaxClusterSize;
  
  // constant scaling factor for determining cluster scores
  private final double mClusterScoreScale;
  
  // how much the cluster grid is subdivided compared to the base grid
  private final int mSubdivisions;
  
  // dimensions of the cluster grid
  private final int mXSize,
                    mYSize;
  
  // dimensions of the base grid
  private final int mBaseXSize,
                    mBaseYSize;

  // the cluster grid
  // initially holds the count of particles in the cell
  // subsequently holds the size rating for the local cluster
  private final int mData[][];
  
  // workspace
  private int mClusters[];
  
  // index of next cluster to assign
  private int mNextCluster;
  
  // flag for checking that the class is being used correctly
  private boolean mClustersReady;
  
  // constructor
  public Clusters(Flow owner, int subdivisions, int numParticles) {
    
    assert( owner != null );
    assert( subdivisions >= 1 );
    
    mOwner = owner;
    mSubdivisions = subdivisions;
    
    mBaseXSize = mOwner.baseXSize();
    mBaseYSize = mOwner.baseYSize();
    
    mXSize = mSubdivisions*mBaseXSize;
    mYSize = mSubdivisions*mBaseYSize;
    
    mMaxClusterSize = Math.round(0.8f*numParticles);
    mClusterScoreScale = maxClusterScore() / Math.log(mMaxClusterSize);
    
    mData = new int[mYSize][mXSize];
    mClusters = new int[100];
    
    reset();
    
  } // constructor
  
  // clear the data ready for calculating new clusters
  public void reset() {
    
    for ( int iy = 0 ; iy < mYSize ; iy++ ) {
      for ( int ix = 0 ; ix < mXSize ; ix++ ) {
        mData[iy][ix] = 0;
      }
    }
    mNextCluster = 1;
    mClustersReady = false;
    
  } // reset()

  // add a position to be clustered
  public void addPoint(float x, float y) {
    
    assert( !mClustersReady );
    
    assert( x >= 0.0f && x < mBaseXSize );
    assert( y >= 0.0f && y < mBaseYSize );
    
    final int ix = (int)(mSubdivisions*x),
              iy = (int)(mSubdivisions*y);
    
    assert( ix >= 0 && ix < mXSize );
    assert( iy >= 0 && iy < mYSize );
    
    mData[iy][ix] += 1;
    
  } // addPoint()
  
  // retrieve the cluster size rating for a particle
  public int getClusterScore(float x, float y) {
    
    assert( mClustersReady );
    
    assert( x >= 0.0f && x < mBaseXSize );
    assert( y >= 0.0f && y < mBaseYSize );
    
    final int ix = (int)(mSubdivisions*x),
              iy = (int)(mSubdivisions*y);
    
    assert( ix >= 0 && ix < mXSize );
    assert( iy >= 0 && iy < mYSize );
    
    return mData[iy][ix];
    
  } // getClusterScore()
  
  // assign a cluster a score based on its size
  public int clusterScore(int size) {
    
    if ( size == 0 ) return 0;
    int score = (int)Math.round( mClusterScoreScale * Math.log(size) );
    return Math.min( maxClusterScore(), score );
    
  } // clusterScore()
  
  // calculate clusters for the particles
  public void makeClusters() {
    
    //print("makeClusters():");
    
    assert( !mClustersReady );
    final float baseWalls[][][] = mOwner.walls();
    
    for ( int jy = 0 ; jy < mYSize ; jy++ ) {
      for ( int jx = 0 ; jx < mXSize ; jx++ ) {
        
        final int num = mData[jy][jx]; 
        if ( num == 0 ) continue;

        final int ky = jy/mSubdivisions,
                  kx = jx/mSubdivisions;
        float walls[] = baseWalls[ky][kx];
        
        final boolean west = (walls[Env.WEST]==Flow.OPEN),
                      east = (walls[Env.EAST]==Flow.OPEN),
                      north = (walls[Env.NORTH]==Flow.OPEN);
        final boolean nw = ( kx > 0 && ky > 0 )
                  && ( (west && baseWalls[ky][kx-1][Env.NORTH]==Flow.OPEN)
                    || (north && baseWalls[ky-1][kx][Env.WEST]==Flow.OPEN) );
        final boolean ne = ( kx < mBaseXSize-1 && ky > 0 )
                  && ( (east && baseWalls[ky][kx+1][Env.NORTH]==Flow.OPEN)
                    || (north && baseWalls[ky-1][kx][Env.EAST]==Flow.OPEN) );

        final int iy = jy - ky*mSubdivisions,
                  ix = jx - kx*mSubdivisions;
            
        int wIndex = ( ix > 0 || west ) ? mData[jy][jx-1] : 0,
            nIndex = ( iy > 0 || north ) ? mData[jy-1][jx] : 0,
            nwIndex = ( (ix > 0 || west) 
                     && (iy > 0 || north)
                     && (ix > 0 || iy > 0 || nw) ) 
                      ? mData[jy-1][jx-1] : 0,
            neIndex = ( (ix < mSubdivisions-1 || east) 
                     && (iy > 0 || north)
                     && (ix < mSubdivisions-1 || iy > 0 || ne) ) 
                      ? mData[jy-1][jx+1] : 0;
        
        //System.out.println("["+jy+"]["+jx+"] num=" + num + " nw="+nwIndex
        //                   + " n="+nIndex+" ne="+neIndex+" w="+wIndex);
                      
        int cluster = nwIndex;
        if ( nIndex > 0 ) {
          if ( cluster != 0 ) cluster = mergeClusters(cluster, nIndex);
          else cluster = nIndex;
        }
        if ( neIndex > 0 ) {
          if ( cluster != 0 ) cluster = mergeClusters(cluster, neIndex);
          else cluster = neIndex;
        }
        if ( wIndex > 0 ) {
          if ( cluster != 0 ) cluster = mergeClusters(cluster, wIndex);
          else cluster = wIndex;
        }
            
        if ( cluster > 0 ) {
          addToCluster(cluster, num);
        } else {
          cluster = newCluster(num);
        }
        //System.out.println("cluster=" + cluster);
        mData[jy][jx] = cluster;
                      
      } // for (jx)
    } // for (jy)
    
    for ( int iy = 0 ; iy < mYSize ; iy++ ) {
      for ( int ix = 0 ; ix < mXSize ; ix++ ) {
        int cluster = mData[iy][ix];
        if ( cluster == 0 ) continue;
        while ( mClusters[cluster] < 0 ) cluster = -mClusters[cluster];
        final int size = mClusters[cluster];
        mData[iy][ix] = clusterScore(size);
      }
    }
    
    mClustersReady = true;
    
  } // makeClusters()

  // create a new cluster with the specified number of members
  private int newCluster(int num) {

    //print("newCluster(" + num + ") before:");

    assert( num > 0 );
    final int cluster = mNextCluster++;
    if ( cluster >= mClusters.length ) {
      mClusters = Arrays.copyOf(mClusters, 2*mClusters.length);
    }
    mClusters[cluster] = num;

    //print("newCluster() after:");
    return cluster;
    
  } // newCluster()
  
  // add members to a cluster
  private void addToCluster(int cluster, int num) {

    //print("addToCluster(" + cluster + "," + num + ") before:");

    assert( cluster < mNextCluster );
    assert( num > 0 );
    while ( mClusters[cluster] < 0 ) {
      final int next = -mClusters[cluster];
      assert( next < cluster );
      cluster = next;
    }
    mClusters[cluster] += num;
    
    //print("addToCluster() after:");

  } // addToCluster()
  
  // combine clusters
  private int mergeClusters(int cluster1, int cluster2) {
    
    assert( cluster1 > 0 && cluster1 < mNextCluster );
    assert( cluster2 > 0 && cluster2 < mNextCluster  );
    if ( cluster1 == cluster2 ) return cluster1;
    
    //print("mergeClusters(" + cluster1 + "," + cluster2 + ") before:");

    int clusterA = cluster1,
        clusterB = cluster2;
    
    while ( mClusters[clusterA] < 0 ) {
      final int next = -mClusters[clusterA];
      assert( next < clusterA );
      clusterA = next;
    }
    while ( mClusters[clusterB] < 0 ) {
      final int next = -mClusters[clusterB]; 
      assert( next < clusterB );
      clusterB = next;
    }

    int mainCluster = Math.min(clusterA, clusterB),
        otherCluster = Math.max(clusterA, clusterB);
    assert( mClusters[mainCluster] > 0 );
    assert( mClusters[otherCluster] > 0 );
    if ( mainCluster != otherCluster ) {
      mClusters[mainCluster] += mClusters[otherCluster];
      mClusters[otherCluster] = -mainCluster;
    }

    if ( cluster1 != mainCluster ) mClusters[cluster1] = -mainCluster;
    if ( cluster2 != mainCluster ) mClusters[cluster2] = -mainCluster;
    
    //print("mergeClusters() after:");
    assert( mainCluster > 0 && mainCluster < mNextCluster );
    return mainCluster;
    
  } // mergeClusters()
  
  //private void print(String title) {
  //  System.out.println(title);
  //  for ( int k = 1 ; k < mNextCluster ; k++ ) {
  //    System.out.print(k + ":" + mClusters[k] + " ");
  //  }
  //  System.out.println("");
  //}
  
} // class Clusters
