/**
MIT License

Copyright (c) 2009 

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
 */


package sw4j.util;

import java.security.*;




/**
 * Computer hash value for String or byte array
 *
 * @author  Li Ding
 * 
 */
public class ToolHash {
    

	
	////////////////////////////////////////////////
	// functions
	////////////////////////////////////////////////
   
	public static String hash_mbox_sum_sha1(String szText){
		String szTemp = szText;
		if (!szTemp.toLowerCase().startsWith("mailto:"))
			szTemp= "mailto:"+szTemp;
		else{
			szTemp= "mailto:"+szTemp.substring("mailto:".length());
		}
		return hash_sum_sha1(szTemp.getBytes());
	}

    public static String hash_sum_sha1(byte [] block){
        return hash_sum(block, "SHA-1");
    }
    public static String hash_sum_md5(byte [] block){
        return hash_sum(block, "MD5");
    }
    
    private static String hash_sum(byte [] msg, String option){
        MessageDigest md; 
        try { 
            md = MessageDigest.getInstance(option); 
            
            byte [] digest = md.digest(msg);
            
            String szHash="";
            for (int i = 0; i < digest.length; i++) {
              int b = digest[i] & 0xff;
              String hex = Integer.toHexString(b);
              if (hex.length() == 1) 
                  hex = "0"+hex;
              szHash +=hex;

            }
            return szHash;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
