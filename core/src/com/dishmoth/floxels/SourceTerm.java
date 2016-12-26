/*
 *  SourceTerm.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

// implementer contributes to the Poisson source terms 
public interface SourceTerm {
  
  public void addToSource(int floxelType, float source[][], int refineFactor);

} // interface SourceTerm
