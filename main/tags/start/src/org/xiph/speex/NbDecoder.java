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
 * Class: NbDecoder.java                                                      *
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
 * Narrowband Speex Decoder
 */
public class NbDecoder
  extends Decoder
{
   /*  ================================================
         PACKAGE STATIC DATA MEMBERS
       ================================================ */
    public static float[] exc_gain_quant_scal3 = {-2.794750f, -1.810660f, -1.169850f, -0.848119f, 
        -0.587190f, -0.329818f, -0.063266f, 0.282826f};

    public static float[] exc_gain_quant_scal1 = {-0.35f, 0.05f};
  
   /*  ================================================
         PRIVATE DATA MEMBERS
       ================================================ */

    private int    first;             /**< Is this the first frame? */
    private int    frameSize;         /**< Size of frames */
    private int    subframeSize;      /**< Size of sub-frames */
    private int    nbSubframes;       /**< Number of sub-frames */
    private int    windowSize;        /**< Analysis (LPC) window length */
    private int    lpcSize;           /**< LPC order */
    private int    bufSize;           /**< Buffer size */
    private int    min_pitch;         /**< Minimum pitch value allowed */
    private int    max_pitch;         /**< Maximum pitch value allowed */

    private float  frmBuf[];          /**< Input buffer (original signal) */
    private int    frmIdx;
    private float  excBuf[];          /**< Excitation buffer */
    private int    excIdx;            /**< Start of excitation frame */
    private float  innov[];           /**< Innovation for the frame */
    private float  qlsp[];            /**< Quantized LSPs for current frame */
    private float  old_qlsp[];        /**< Quantized LSPs for previous frame */
    private float  interp_qlsp[];     /**< Interpolated quantized LSPs */
    private float  interp_qlpc[];     /**< Interpolated quantized LPCs */
    private float  mem_sp[];          /**< Filter memory for synthesis signal */
    private float  pi_gain[];         /**< Gain of LPC filter at theta=pi (fe/2) */
    private float  awk1[], awk2[], awk3[];
    private float  lpc[];
    private float  innov2[];

    private SubMode submodes[];     /**< Sub-mode data */
    private int     submodeID;      /**< Activated sub-mode */

    /*Vocoder data*/
    private float  voc_m1;
    private float  voc_m2;
    private float  voc_mean;
    private int    voc_offset;

   /*  ================================================
         PACKAGE INSTANCE METHODS
       ================================================ */
    
    /**
     * Initialise
     */
    public void init()
    {
        first=1;
   
        /* Codec parameters, should eventually have several "modes"*/
        frameSize    = 160;
        windowSize   = frameSize*3/2;
        subframeSize = 40;
        nbSubframes  = frameSize/subframeSize;
        lpcSize      = 10;
        bufSize      = 640;
        min_pitch    = 17;
        max_pitch    = 144;

        submodes     = ModesNB.nbsubmodes;
        submodeID    = 5;

        frmBuf  = new float[bufSize];
        frmIdx  = bufSize - windowSize;
        excBuf  = new float[bufSize];
        excIdx  = bufSize - windowSize;
        innov = new float[frameSize];

        interp_qlpc = new float[(lpcSize+1)];
        qlsp = new float[lpcSize];
        old_qlsp = new float[lpcSize];
        interp_qlsp = new float[lpcSize];
        mem_sp = new float[5*lpcSize];

        awk1   =  new float[11];
        awk2   =  new float[11];
        awk3   =  new float[11];
        innov2 =  new float[40];
        lpc    =  new float[40];

        filters.init ();

        pi_gain = new float[nbSubframes];

        voc_m1=voc_m2=voc_mean=0;
        voc_offset=0;    
    }

    /**
     * Decode the given input.
     */
    public int decode(Bits bits, float out[])
    {
        int i, sub, pitch, ol_pitch=0, m;
        float pitch_gain[] = new float[3];
        float ol_gain=0.0f, ol_pitch_coef=0.0f;

        /* Search for next narrwoband block (handle requests, skip wideband blocks) */
        do {
          if (bits.unpack(1)!=0) /* Skip wideband block (for compatibility) */
          {
            // TODO - actually skip the wideband block
            return (1);
          }

          /* Get the sub-mode that was used */
          m = bits.unpack(4);
          if (m==15) /* We found a terminator */
          {
            return 1;
          } else if (m==14) /* Speex in-band request */
          {
            int ret = speexInbandRequest(bits);
            if (ret != 0)
              return ret;
          } else if (m==13) /* User in-band request */
          {
            int ret = userInbandRequest(bits);
            if (ret != 0)
              return ret;
          } else if (m>8) /* Invalid mode */
          {
            System.err.println("Invalid mode encountered: corrupted stream?");
            return 2;
          }
        }
        while (m>8);
        submodeID = m;

        /* Shift all buffers by one frame */
        System.arraycopy(frmBuf, frameSize, frmBuf, 0, bufSize-frameSize);
        System.arraycopy(excBuf, frameSize, excBuf, 0, bufSize-frameSize);

        /* If null mode (no transmission), just set a couple things to zero*/
        if (submodes[submodeID] == null)
        {
            filters.bw_lpc(.93f, interp_qlpc, lpc, 10);
      
            float innov_gain=0;
            for (i=0;i<frameSize;i++)
                innov_gain += innov[i]*innov[i];
            innov_gain=(float)Math.sqrt(innov_gain/frameSize);
            for (i=excIdx;i<excIdx+frameSize;i++)
            {
                excBuf[i]=3*innov_gain*( (float)Math.random() - .5f);
            }
            first=1;

            /* Final signal synthesis from excitation */
            filters.iir_mem2(excBuf, excIdx, lpc, frmBuf, frmIdx, frameSize, lpcSize, mem_sp);

            out[0] = frmBuf[frmIdx];
            for (i=1;i<frameSize;i++)
                out[i]=frmBuf[frmIdx+i];
            return (0);
        }

        /* Unquantize LSPs */
        submodes[submodeID].lsqQuant.unquant(qlsp, lpcSize, bits);

        /* Handle first frame and lost-packet case */
        if (first!=0)
        {
            for (i=0;i<lpcSize;i++)
                old_qlsp[i] = qlsp[i];
        }

        /* Get open-loop pitch estimation for low bit-rate pitch coding */
        if (submodes[submodeID].lbr_pitch!=-1)
        {
            ol_pitch = min_pitch + bits.unpack(7);
        }  
   
        if (submodes[submodeID].forced_pitch_gain!=0)
        {
            int quant= bits.unpack(4);
            ol_pitch_coef=0.066667f*quant;
        }
   
        /* Get global excitation gain */
        int qe  = bits.unpack(5);
        ol_gain = (float)Math.exp(qe/3.5);
    
        /* unpacks unused dtx bits */
        if (submodeID==1)
        {
            bits.unpack(4);
        }
    
        /*Loop on subframes */
        for (sub=0;sub<nbSubframes;sub++)
        {
            int spIdx, extIdx;
            float tmp;

            /* Original signal */
            spIdx  = frmIdx+subframeSize*sub;
        
            /* Excitation */
            extIdx = excIdx+subframeSize*sub;

            /* LSP interpolation (quantized and unquantized) */
            tmp = (1.0f + sub)/nbSubframes;
            for (i=0;i<lpcSize;i++)
                interp_qlsp[i] = (1-tmp)*old_qlsp[i] + tmp*qlsp[i];

            /* Make sure the LSP's are stable */
            m_lsp.enforce_margin(interp_qlsp, lpcSize, .002f);

            /* Compute interpolated LPCs (unquantized) */
            for (i=0;i<lpcSize;i++)
                interp_qlsp[i] = (float)Math.cos(interp_qlsp[i]);
            m_lsp.lsp2lpc(interp_qlsp, interp_qlpc, lpcSize);

            /* Compute enhanced synthesis filter */
            float r=.9f;   
            float k1,k2,k3;
      
            k1=submodes[submodeID].lpc_enh_k1;
            k2=submodes[submodeID].lpc_enh_k2;
            k3=(1-(1-r*k1)/(1-r*k2))/r;
            filters.bw_lpc(k1, interp_qlpc, awk1, lpcSize);
            filters.bw_lpc(k2, interp_qlpc, awk2, lpcSize);
            filters.bw_lpc(k3, interp_qlpc, awk3, lpcSize);
 
            /* Compute analysis filter at w=pi */
            tmp=1;
            pi_gain[sub]=0;
            for (i=0;i<=lpcSize;i++)
            {
                pi_gain[sub] += tmp*interp_qlpc[i];
                tmp = -tmp;
            }

            /* Reset excitation */
            for (i=0;i<subframeSize;i++)
                excBuf[extIdx+i]=0;

            /*Adaptive codebook contribution*/
            int pit_min, pit_max;
        
            /* Handle pitch constraints if any */
            if (submodes[submodeID].lbr_pitch != -1)
            {
                int margin= submodes[submodeID].lbr_pitch;
                if (margin!=0)
                {
                    pit_min = ol_pitch-margin+1;
                    if (pit_min < min_pitch)
                        pit_min = min_pitch;
                    pit_max = ol_pitch+margin;
                    if (pit_max > max_pitch)
                        pit_max = max_pitch;
                } 
                else 
                {
                    pit_min = pit_max = ol_pitch;
                }
            } 
            else 
            {
                pit_min = min_pitch;
                pit_max = max_pitch;
            }

            /* Pitch synthesis */
            pitch = submodes[submodeID].ltp.unquant(excBuf, extIdx, pit_min, ol_pitch_coef,  
                              subframeSize, pitch_gain, bits);
      
            /* Unquantize the innovation */
            int q_energy, ivi=sub*subframeSize;
            float ener;
         
            for (i=ivi;i<ivi+subframeSize;i++)
                innov[i]=0.0f;

            /* Decode sub-frame gain correction */
            if (submodes[submodeID].have_subframe_gain==3)
            {
                q_energy = bits.unpack(3);
                ener     = (float) (ol_gain*Math.exp(exc_gain_quant_scal3[q_energy]));
            } 
            else if (submodes[submodeID].have_subframe_gain==1)
            {
                q_energy = bits.unpack(1);
                ener     = (float) (ol_gain*Math.exp(exc_gain_quant_scal1[q_energy]));
            } 
            else {
                ener     = ol_gain;
            }
               
            if (submodes[submodeID].innovation!=null)
            {
                /* Fixed codebook contribution */
                submodes[submodeID].innovation.unquant(innov, ivi, subframeSize, bits);
            } 

            /* De-normalize innovation and update excitation */
            for (i=ivi;i<ivi+subframeSize;i++)
                innov[i]*=ener;

            /*  Vocoder mode */
            if (submodeID==1) 
            {
                float g=ol_pitch_coef;
           
                for (i=0;i<subframeSize;i++)
                    excBuf[extIdx+i]=0;
                while (voc_offset<subframeSize)
                {
                    if (voc_offset>=0)
                        excBuf[extIdx+voc_offset]=(float)Math.sqrt(1.0f*ol_pitch);
                    voc_offset+=ol_pitch;
                }
                voc_offset -= subframeSize;

                g=.5f+2*(g-.6f);
                if (g<0)
                    g=0;
                if (g>1)
                    g=1;
                for (i=0;i<subframeSize;i++)
                {
                    float itmp=excBuf[extIdx+i];
                    excBuf[extIdx+i]=.8f*g*excBuf[extIdx+i]*ol_gain + .6f*g*voc_m1*ol_gain + .5f*g*innov[ivi+i] - .5f*g*voc_m2 + (1-g)*innov[ivi+i];
                    voc_m1 = itmp;
                    voc_m2=innov[ivi+i];
                    voc_mean = .95f*voc_mean + .05f*excBuf[extIdx+i];
                   excBuf[extIdx+i]-=voc_mean;
                }
            } 
            else {
                for (i=0;i<subframeSize;i++)
                    excBuf[extIdx+i]+=innov[ivi+i];
            }
         
            /* Decode second codebook (only for some modes) */
            if (submodes[submodeID].double_codebook!=0)
            {           
                for (i=0;i<subframeSize;i++)
                    innov2[i]=0;
                submodes[submodeID].innovation.unquant(innov2, 0, subframeSize, bits);
                for (i=0;i<subframeSize;i++)
                    innov2[i]*=ener*(1/2.2);
                for (i=0;i<subframeSize;i++)
                    excBuf[extIdx+i] += innov2[i];
            }
  

            for (i=0;i<subframeSize;i++)
                frmBuf[spIdx+i]=excBuf[extIdx+i];

            /* Signal synthesis */
            if (submodes[submodeID].comb_gain>0)
                filters.comb_filter(excBuf, extIdx, frmBuf, spIdx, subframeSize, 
                    pitch, pitch_gain, submodes[submodeID].comb_gain);

            /* Use enhanced LPC filter */
            filters.filter_mem2(frmBuf, spIdx, awk2, awk1, subframeSize, lpcSize, mem_sp, lpcSize);
            filters.filter_mem2(frmBuf, spIdx, awk3, interp_qlpc, subframeSize, lpcSize, mem_sp, 0);
        }
   
        /*Copy output signal*/
        out[0] = frmBuf[frmIdx];
        for (i=1;i<frameSize;i++)
            out[i]=frmBuf[frmIdx+i];

        /* Store the LSPs for interpolation in the next frame */
        for (i=0;i<lpcSize;i++)
            old_qlsp[i] = qlsp[i];

        /* The next frame will not be the first (Duh!) */
        first = 0;
        
        return (0);
    }
    
  /**
   *
   */
  public int  getFrameSize()
  {
    return (frameSize);   
  }
    
  /**
   * Returns the Pitch Gain array
   */
  public float[] getPiGain()
  {
    return pi_gain;
  }
  
  /**
   * Returns the excitation array
   */
  public float[] getExc()
  {
    float[] excTmp = new float[frameSize];
    System.arraycopy(excBuf, excIdx, excTmp, 0, frameSize);
    return excTmp;
  }
  
  /**
   * Returns the innovation array
   */
  public float[] getInnov()
  {
    return innov;
  }
}