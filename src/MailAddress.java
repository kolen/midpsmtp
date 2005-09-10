
package org.kolen.smtpclient;
import java.lang.*;

class MailAddress {

    public static String [] address_parse (String address) {
	String [] result = new String[2];

	int firstBrace = address.indexOf('<');
	int lastBrace = address.lastIndexOf('>');

	if ((firstBrace == -1) || (lastBrace == -1))
	    {
		result[0] = address.trim();
		result[1] = "";
		return result;
	    }

	result[1] = address.substring(0, firstBrace).trim();
	result[0] = address.substring(firstBrace+1, lastBrace).trim();

	return result;
    }

    public static String address_concat (String email, String name) {
	return name + " <" + email + ">";
    }

    public static String address_fix (String email) {
	int firstBrace = email.indexOf('<');
	int lastBrace = email.lastIndexOf('>');

	if ((firstBrace == -1) || (lastBrace == -1))
	    return "<" + email + ">";
	else
	    return email;
    }
    
}
