/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: LU.java                                                             *
 *                                                                            *
 * Author: James LAWRENCE                                                     *
 * Modified by: Marc GIMPEL                                                   *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: March 2003                                                           *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

/* Copyright (C) 2002 Jean-Marc Valin 

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:
   
   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   
   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   
   - Neither the name of the Xiph.org Foundation nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.xiph.speex;

/**
 * Abstract class that is the base for the various LSP Quantisation and
 * Unquantisation methods.
 */
public abstract class LspQuant
{
  public static final int MAX_LSP_SIZE       = 20;

  public static final int NB_CDBK_SIZE       = 64;
  public static final int NB_CDBK_SIZE_LOW1  = 64;
  public static final int NB_CDBK_SIZE_LOW2  = 64;
  public static final int NB_CDBK_SIZE_HIGH1 = 64;
  public static final int NB_CDBK_SIZE_HIGH2 = 64;

  protected float[] quant_weight;

  /**
   * Constructor
   */
  protected LspQuant()
  {
    quant_weight = new float[MAX_LSP_SIZE];
  }

  /**
   * Quantification
   */
  public abstract void quant(float lsp[], float qlsp[], int order, Bits bits); 
  
  /**
   * Unquantification
   */
  public abstract void unquant(float lsp[], int order, Bits bits); 
  
  /**
   *
   */
  protected void unpackPlus(float lsp[], int tab[], Bits bits, float k, int ti, int li)
  {
    int id=bits.unpack(6), i=0;
    for (;i<ti;i++)
      lsp[i+li] += k * (float)tab[id*ti+i];
  }
  
  /**
   * LSP quantification
   * Note: x is modified
   */
  protected static int lsp_quant(float[] x, int xs, int[] cdbk, int nbVec, int nbDim)
  {
    int i,j;
    float dist, tmp;
    float best_dist=0;
    int best_id=0;
    int ptr=0;
    for (i=0;i<nbVec;i++)
    {
      dist=0;
      for (j=0;j<nbDim;j++)
      {
        tmp=(x[xs+j]-cdbk[ptr++]);
        dist+=tmp*tmp;
      }
      if (dist<best_dist || i==0)
      {
        best_dist=dist;
        best_id=i;
      }
    }

    for (j=0;j<nbDim;j++)
      x[xs+j] -= cdbk[best_id*nbDim+j];
    
    return best_id;
  }

  /**
   * Note: x is modified
   */
  protected static int lsp_weight_quant(float[] x, int xs, float[] weight, int ws, int[] cdbk, int nbVec, int nbDim)
  {
    int i,j;
    float dist, tmp;
    float best_dist=0;
    int best_id=0;
    int ptr=0;
    for (i=0; i<nbVec; i++) {
      dist=0;
      for (j=0; j<nbDim; j++) {
        tmp=(x[xs+j]-cdbk[ptr++]);
        dist+=weight[ws+j]*tmp*tmp;
      }
      if (dist<best_dist || i==0) {
        best_dist=dist;
        best_id=i;
      }
    }
    for (j=0; j<nbDim; j++)
      x[xs+j] -= cdbk[best_id*nbDim+j];
    return best_id;
  }
}
