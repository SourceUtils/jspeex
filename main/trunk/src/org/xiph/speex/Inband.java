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
 * Class: Inband.java                                                         *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 14th July 2003                                                       *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

/**
 * Speex in-band and User in-band controls.
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class Inband
{
  private Stereo stereo;

  /**
   * Constructor.
   * @param stereo
   */
  public Inband (Stereo stereo)
  {
    this.stereo = stereo;
  }

  /**
   * Speex in-band request (submode=14).
   * @param bits - Speex bits buffer.
   * @return 0 is successful, 1 otherwise.
   */
  public int speexInbandRequest(Bits bits)
  {
    int code = bits.unpack(4);
    int tmp;
    switch (code) {
      case 0: // asks the decoder to set perceptual enhancment off (0) or on (1)
        tmp = bits.unpack(1);
        break;
      case 1: // asks (if 1) the encoder to be less "aggressive" due to high packet loss
        tmp = bits.unpack(1);
        break;
      case 2: // asks the encoder to switch to mode N
        tmp = bits.unpack(4);
        break;
      case 3: // asks the encoder to switch to mode N for low-band
        tmp = bits.unpack(4);
        break;
      case 4: // asks the encoder to switch to mode N for high-band
        tmp = bits.unpack(4);
        break;
      case 5: // asks the encoder to switch to quality N for VBR
        tmp = bits.unpack(4);
        break;
      case 6: // request acknowledgement (0=no, 1=all, 2=only for inband data)
        tmp = bits.unpack(4);
        break;
      case 7: // asks the encoder to set CBR(0), VAD(1), DTX(3), VBR(5), VBR+DTX(7)
        tmp = bits.unpack(4);
        break;
      case 8: // transmit (8-bit) character to the other end
        tmp = bits.unpack(8);
        break;
      case 9: // intensity stereo information
        // setup the stereo decoder; to skip: tmp = bits.unpack(8); break;
        return stereo.init(bits); // read 8 bits
      case 10: // announce maximum bit-rate acceptable (N in byets/second)
        tmp = bits.unpack(16);
        break;
      case 11: // reserved
        tmp = bits.unpack(16);
        break;
      case 12: // Acknowledge receiving packet N
        tmp = bits.unpack(32);
        break;
      case 13: // reserved
        tmp = bits.unpack(32);
        break;
      case 14: // reserved
        tmp = bits.unpack(64);
        break;
      case 15: // reserved
        tmp = bits.unpack(64);
        break;
      default:
    }
    return 1;
  }
  
  /**
   * User in-band request (submode=13).
   * @param bits - Speex bits buffer.
   * @return 0 is successful, 1 otherwise.
   */
  public int userInbandRequest(Bits bits)
  {
    return 1;
  }
}
