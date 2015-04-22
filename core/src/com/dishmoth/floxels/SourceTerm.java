/*
 *  SourceTerm.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// implementer contributes to the Poisson source terms 
public interface SourceTerm {
  
  public void addToSource(int floxelType, float source[][], int refineFactor);

} // interface SourceTerm
