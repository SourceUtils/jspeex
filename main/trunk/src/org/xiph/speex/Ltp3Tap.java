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
 * Class: Ltp3Tap.java                                                        *
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
 * Long Term Prediction Quantisation and Unquantisation (3Tap)
 */
public class Ltp3Tap
  extends Ltp
{
  private float   gain[];
  private int     gain_cdbk[];
  private int     gain_bits;
  private int     pitch_bits;
  private float   e[][];
  
  /**
   * Constructor
   */
  public Ltp3Tap(int gain_cdbk[], int gain_bits, int pitch_bits)
  {
    this.gain       = new float[3];
    this.gain_cdbk  = gain_cdbk;
    this.gain_bits  = gain_bits;
    this.pitch_bits = pitch_bits;
    this.e          = new float[3][128];
  }
    
  /**
   * Quantification
   */
  public final int quant(float[] target, float sw[], int sws, float[] ak, float[] awk1, float awk2[],
                         float[] exc, int es, int start, int end, float pitch_coef, int p, 
                         int nsf, Bits bits, float[] exc2, int e2s, float[] r, int complexity)
  {
    int i,j;
    int[] cdbk_index = new int[1];
    int pitch=0, best_gain_index=0;
    float[] best_exc;
    int best_pitch=0;
    float err, best_err=-1;
    int N;
    int[] nbest;
    float[] gains;

    N=complexity;
    if (N>10)
      N=10;

    nbest=new int[N];
    gains = new float[N];

    if (N==0 || end<start) {
      bits.pack(0, pitch_bits);
      bits.pack(0, gain_bits);
      for (i=0;i<nsf;i++)
        exc[es+i]=0;
      return start;
    }
    
    best_exc=new float[nsf];
    
    if (N>end-start+1)
      N=end-start+1;
    open_loop_nbest_pitch(sw, sws, start, end, nsf, nbest, gains, N);

    for (i=0;i<N;i++) {
      pitch=nbest[i];
      for (j=0;j<nsf;j++)
        exc[es+j]=0;
      err=pitch_gain_search_3tap(target, ak, awk1, awk2, exc, es, pitch, p, nsf,
                                 bits, exc2, e2s, r, cdbk_index);
      if (err<best_err || best_err<0) {
        for (j=0;j<nsf;j++)
          best_exc[j]=exc[es+j];
        best_err=err;
        best_pitch=pitch;
        best_gain_index=cdbk_index[0];
      }
    }
    
    bits.pack(best_pitch-start, pitch_bits);
    bits.pack(best_gain_index, gain_bits);
    for (i=0;i<nsf;i++)
      exc[es+i]=best_exc[i];

    return pitch;
  }

  /**
   * Unquantification
   */
  public final int unquant(float exc[], int es, int start, float pitch_coef,  
                           int nsf, float gain_val[], Bits bits)
  {
    int i, pitch, gain_index;
    
    pitch       = bits.unpack(pitch_bits);
    pitch      += start;
    gain_index  = bits.unpack(gain_bits);
    
    gain[0] = 0.015625f*(float)gain_cdbk[gain_index*3]+.5f;
    gain[1] = 0.015625f*(float)gain_cdbk[gain_index*3+1]+.5f;
    gain[2] = 0.015625f*(float)gain_cdbk[gain_index*3+2]+.5f;
    
    gain_val[0]=gain[0];
    gain_val[1]=gain[1];
    gain_val[2]=gain[2];

    for (i=0;i<3;i++)
    {
      int j, tmp1, tmp2, pp=pitch+1-i;

      tmp1=nsf;
      if (tmp1>pp)
        tmp1=pp;
      
      for (j=0;j<tmp1;j++)
        e[i][j]=exc[es+j-pp];

      tmp2=nsf;
      if (tmp2>pp+pitch)
        tmp2=pp+pitch;
      
      for (j=tmp1;j<tmp2;j++)
        e[i][j]=exc[es+j-pp-pitch];
      
      for (j=tmp2;j<nsf;j++)
        e[i][j]=0;
    }

    for (i=0;i<nsf;i++)
      exc[es+i]=gain[0]*e[2][i]+gain[1]*e[1][i]+gain[2]*e[0][i];

    return (pitch);
  }
  
  /**
   * Finds the best quantized 3-tap pitch predictor by analysis by synthesis
   */
  private float pitch_gain_search_3tap(float target[], /* Target vector */
                                       float ak[],     /* LPCs for this subframe */
                                       float awk1[],   /* Weighted LPCs #1 for this subframe */
                                       float awk2[],   /* Weighted LPCs #2 for this subframe */
                                       float exc[],    /* Excitation */
                                       int   es,
                                       int   pitch,    /* Pitch value */
                                       int   p,        /* Number of LPC coeffs */
                                       int   nsf,      /* Number of samples in subframe */
                                       Bits  bits,
                                       float[] exc2,
                                       int     e2s,
                                       float[] r,
                                       int[]   cdbk_index
                                       )
  {
    int i,j;
    float[][] x;
    //      float[][] e;
    float[] corr = new float[3];
    float[][] A = new float[3][3];
    int   gain_cdbk_size;
    float err1,err2;

    gain_cdbk_size=1<<gain_bits;

    x = new float[3][nsf];
    e = new float[3][nsf];

    for (i=2;i>=0;i--)
    {
      int pp=pitch+1-i;
      for (j=0;j<nsf;j++)
      {
        if (j-pp<0)
          e[i][j]=exc2[e2s+j-pp];
        else if (j-pp-pitch<0)
          e[i][j]=exc2[e2s+j-pp-pitch];
        else
          e[i][j]=0;
      }

      if (i==2)
        Filters.syn_percep_zero(e[i], 0, ak, awk1, awk2, x[i], nsf, p);
      else {
        for (j=0;j<nsf-1;j++)
          x[i][j+1]=x[i+1][j];
        x[i][0]=0;
        for (j=0;j<nsf;j++)
          x[i][j]+=e[i][0]*r[j];
      }
    }

    for (i=0;i<3;i++)
      corr[i]=inner_prod(x[i],0,target,0,nsf);

    for (i=0;i<3;i++)
      for (j=0;j<=i;j++)
        A[i][j]=A[j][i]=inner_prod(x[i],0,x[j],0,nsf);

    {
      float[] C = new float[9];
      int ptr=0;
      int best_cdbk=0;
      float best_sum=0;
      C[0]=corr[2];
      C[1]=corr[1];
      C[2]=corr[0];
      C[3]=A[1][2];
      C[4]=A[0][1];
      C[5]=A[0][2];
      C[6]=A[2][2];
      C[7]=A[1][1];
      C[8]=A[0][0];
      
      for (i=0;i<gain_cdbk_size;i++)
      {
        float sum=0;
        float g0,g1,g2;
        ptr = 3*i;
        g0=0.015625f*gain_cdbk[ptr]+.5f;
        g1=0.015625f*gain_cdbk[ptr+1]+.5f;
        g2=0.015625f*gain_cdbk[ptr+2]+.5f;

        sum += C[0]*g0;
        sum += C[1]*g1;
        sum += C[2]*g2;
        sum -= C[3]*g0*g1;
        sum -= C[4]*g2*g1;
        sum -= C[5]*g2*g0;
        sum -= .5f*C[6]*g0*g0;
        sum -= .5f*C[7]*g1*g1;
        sum -= .5f*C[8]*g2*g2;

        /* If true, force "safe" pitch values to handle packet loss better */
        if (false) {
          float tot = Math.abs(gain_cdbk[ptr+1]);
          if (gain_cdbk[ptr]>0)
            tot+=gain_cdbk[ptr];
          if (gain_cdbk[ptr+2]>0)
            tot+=gain_cdbk[ptr+2];
          if (tot>1)
            continue;
        }

        if (sum>best_sum || i==0)
        {
          best_sum=sum;
          best_cdbk=i;
        }
      }
      gain[0] = 0.015625f*gain_cdbk[best_cdbk*3]  + .5f;
      gain[1] = 0.015625f*gain_cdbk[best_cdbk*3+1]+ .5f;
      gain[2] = 0.015625f*gain_cdbk[best_cdbk*3+2]+ .5f;

      cdbk_index[0]=best_cdbk;
    }
    
    for (i=0;i<nsf;i++)
      exc[es+i]=gain[0]*e[2][i]+gain[1]*e[1][i]+gain[2]*e[0][i];
    
    err1=0;
    err2=0;
    for (i=0;i<nsf;i++)
      err1+=target[i]*target[i];
    for (i=0;i<nsf;i++)
      err2+=(target[i]-gain[2]*x[0][i]-gain[1]*x[1][i]-gain[0]*x[2][i])
            * (target[i]-gain[2]*x[0][i]-gain[1]*x[1][i]-gain[0]*x[2][i]);

    return err2;
  }
}
