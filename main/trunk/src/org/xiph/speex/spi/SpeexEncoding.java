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
 * Class: SpeexEncoding.java                                                  *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 12th July 2003                                                       *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.spi;

import javax.sound.sampled.AudioFormat;

/**
 * Encodings used by the Speex audio decoder.
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class SpeexEncoding
  extends AudioFormat.Encoding
{
  /** Specifies any Speex encoded data. */
  public static final SpeexEncoding SPEEX = new SpeexEncoding("SPEEX");
  public static final SpeexEncoding SPEEX_Q0 = new SpeexEncoding("SPEEX_quality_0", 0, false);
  public static final SpeexEncoding SPEEX_Q1 = new SpeexEncoding("SPEEX_quality_1", 1, false);
  public static final SpeexEncoding SPEEX_Q2 = new SpeexEncoding("SPEEX_quality_2", 2, false);
  public static final SpeexEncoding SPEEX_Q3 = new SpeexEncoding("SPEEX_quality_3", 3, false);
  public static final SpeexEncoding SPEEX_Q4 = new SpeexEncoding("SPEEX_quality_4", 4, false);
  public static final SpeexEncoding SPEEX_Q5 = new SpeexEncoding("SPEEX_quality_5", 5, false);
  public static final SpeexEncoding SPEEX_Q6 = new SpeexEncoding("SPEEX_quality_6", 6, false);
  public static final SpeexEncoding SPEEX_Q7 = new SpeexEncoding("SPEEX_quality_7", 7, false);
  public static final SpeexEncoding SPEEX_Q8 = new SpeexEncoding("SPEEX_quality_8", 8, false);
  public static final SpeexEncoding SPEEX_Q9 = new SpeexEncoding("SPEEX_quality_9", 9, false);
  public static final SpeexEncoding SPEEX_Q10 = new SpeexEncoding("SPEEX_quality_10", 10, false);
  public static final SpeexEncoding SPEEX_VBR0 = new SpeexEncoding("SPEEX_VBR_quality_0", 0, true);
  public static final SpeexEncoding SPEEX_VBR1 = new SpeexEncoding("SPEEX_VBR_quality_1", 1, true);
  public static final SpeexEncoding SPEEX_VBR2 = new SpeexEncoding("SPEEX_VBR_quality_2", 2, true);
  public static final SpeexEncoding SPEEX_VBR3 = new SpeexEncoding("SPEEX_VBR_quality_3", 3, true);
  public static final SpeexEncoding SPEEX_VBR4 = new SpeexEncoding("SPEEX_VBR_quality_4", 4, true);
  public static final SpeexEncoding SPEEX_VBR5 = new SpeexEncoding("SPEEX_VBR_quality_5", 5, true);
  public static final SpeexEncoding SPEEX_VBR6 = new SpeexEncoding("SPEEX_VBR_quality_6", 6, true);
  public static final SpeexEncoding SPEEX_VBR7 = new SpeexEncoding("SPEEX_VBR_quality_7", 7, true);
  public static final SpeexEncoding SPEEX_VBR8 = new SpeexEncoding("SPEEX_VBR_quality_8", 8, true);
  public static final SpeexEncoding SPEEX_VBR9 = new SpeexEncoding("SPEEX_VBR_quality_9", 9, true);
  public static final SpeexEncoding SPEEX_VBR10 = new SpeexEncoding("SPEEX_VBR_quality_10", 10, true);

  /** Default quality setting for the Speex encoding. */
  public static final int DEFAULT_QUALITY = 3;
  /** Default VBR setting for the Speex encoding. */
  public static final boolean DEFAULT_VBR = false;
  
  /** Quality setting for the Speex encoding. */
  protected int quality;
  /** Defines whether or not the encoding is Variable Bit Rate. */
  protected boolean vbr;
  
  /**
   * Constructs a new encoding.
   * @param name - Name of the Speex encoding.
   * @param quality - Quality setting for the Speex encoding.
   * @param vbr - Defines whether or not the encoding is Variable Bit Rate.
   */
  public SpeexEncoding(String name, int quality, boolean vbr)
  {
    super(name);
    this.quality = quality;
    this.vbr = vbr;
  }

  /**
   * Constructs a new encoding.
   * @param name - Name of the Speex encoding.
   */
  public SpeexEncoding(String name)
  {
    this(name, DEFAULT_QUALITY, DEFAULT_VBR);
  }
  
  /**
   * Returns the quality setting for the Speex encoding.
   * @return the quality setting for the Speex encoding.
   */
  public int getQuality()
  {
    return quality;
  }

  /**
   * Returns whether or not the encoding is Variable Bit Rate.
   * @return whether or not the encoding is Variable Bit Rate.
   */
  public boolean isVBR()
  {
    return vbr;
  }
}
