
package org.kolen;

import java.lang.System;

public class Base64
{    
    public static String encode(String buf)
    {
	int len;
	StringBuffer outbuf;
	byte enc[] = new byte [64];
	int i = 0, j = 0;
	int in0, in1, in2;

	len = buf.length();

	for(; i < 26; i++) enc[i] = (byte)('A' + j++); j = 0;
	for(; i < 52; i++) enc[i] = (byte)('a' + j++); j = 0;
	for(; i < 62; i++) enc[i] = (byte)('0' + j++);
	enc[i++] = '+';
	enc[i++] = '/';

	outbuf = new StringBuffer();

	i = 0;
	while(i < len-2){
	    in0 = buf.charAt(i++);
	    in1 = buf.charAt(i++);
	    in2 = buf.charAt(i++);

	    outbuf.append((char)enc[(in0 >> 2) & 0x3f]);
	    outbuf.append((char)enc[((in0 << 4) | (in1 >> 4)) & 0x3f]);
	    outbuf.append((char)enc[((in1 << 2) | (in2 >> 6)) & 0x3f]);
	    outbuf.append((char)enc[in2 & 0x3f]);
	}
	if((len - i) == 1){
	    in0 = buf.charAt(i++);
	    outbuf.append((char)enc[(in0 >> 2) & 0x3f]);
	    outbuf.append((char)enc[(in0 << 4) & 0x3f]);
	    outbuf.append('=');
	    outbuf.append('=');
	}else if((len - i) == 2){
	    in0 = buf.charAt(i++);
	    in1 = buf.charAt(i++);
	    outbuf.append((char)enc[(in0 >> 2) & 0x3f]);
	    outbuf.append((char)enc[((in0 << 4) | (in1 >> 4)) & 0x3f]);
	    outbuf.append((char)enc[(in1 << 2) & 0x3f]);
	    outbuf.append('=');
	}
  
	return outbuf.toString();
    }
}
