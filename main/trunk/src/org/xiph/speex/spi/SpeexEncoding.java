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
  /**
   * Specifies Speex encoded data.
   */
  public static final AudioFormat.Encoding SPEEX = new SpeexEncoding("Speex");
  public static final AudioFormat.Encoding SPEEX_Q1 = new SpeexEncoding("Speex_quality_1");
  public static final AudioFormat.Encoding SPEEX_Q2 = new SpeexEncoding("Speex_quality_2");
  public static final AudioFormat.Encoding SPEEX_Q3 = new SpeexEncoding("Speex_quality_3");
  public static final AudioFormat.Encoding SPEEX_Q4 = new SpeexEncoding("Speex_quality_4");
  public static final AudioFormat.Encoding SPEEX_Q5 = new SpeexEncoding("Speex_quality_5");
  public static final AudioFormat.Encoding SPEEX_Q6 = new SpeexEncoding("Speex_quality_6");
  public static final AudioFormat.Encoding SPEEX_Q7 = new SpeexEncoding("Speex_quality_7");
  public static final AudioFormat.Encoding SPEEX_Q8 = new SpeexEncoding("Speex_quality_8");
  public static final AudioFormat.Encoding SPEEX_Q9 = new SpeexEncoding("Speex_quality_9");
  public static final AudioFormat.Encoding SPEEX_VBR1 = new SpeexEncoding("Speex_VBR_quality_1");
  public static final AudioFormat.Encoding SPEEX_VBR2 = new SpeexEncoding("Speex_VBR_quality_2");
  public static final AudioFormat.Encoding SPEEX_VBR3 = new SpeexEncoding("Speex_VBR_quality_3");
  public static final AudioFormat.Encoding SPEEX_VBR4 = new SpeexEncoding("Speex_VBR_quality_4");
  public static final AudioFormat.Encoding SPEEX_VBR5 = new SpeexEncoding("Speex_VBR_quality_5");
  public static final AudioFormat.Encoding SPEEX_VBR6 = new SpeexEncoding("Speex_VBR_quality_6");
  public static final AudioFormat.Encoding SPEEX_VBR7 = new SpeexEncoding("Speex_VBR_quality_7");
  public static final AudioFormat.Encoding SPEEX_VBR8 = new SpeexEncoding("Speex_VBR_quality_8");
  public static final AudioFormat.Encoding SPEEX_VBR9 = new SpeexEncoding("Speex_VBR_quality_9");

  /**
   * Constructs a new encoding.
   */
  public SpeexEncoding(String name)
  {
    super(name);
  }
}
