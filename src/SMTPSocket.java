
package org.kolen.smtpclient;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.io.ConnectionNotFoundException;
import java.io.IOException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class SMTPSocket implements Runnable
{
    private DataOutputStream out;
    private DataInputStream in;
    private StreamConnection c;
    private boolean isconnected = false;

    private boolean debugging = false;

    private Throbber throbber;

    private String to;
    private String subj;
    private String body;

    private Settings settings;

    public boolean working = true;

    public SMTPSocket(String to_, String subj_, String body_, Throbber throbber_, Settings settings_)
    {
	to = to_;
	subj = subj_;
	body = body_;
	throbber = throbber_;
	settings = settings_;
    }
 
    public void connect(String host)
	throws IOException,ConnectionNotFoundException
    {
	c = (StreamConnection)Connector.open("socket://" + host + ":25",
					     Connector.READ_WRITE);

	out = c.openDataOutputStream();
	in = c.openDataInputStream();
	isconnected = true;
  
    }

    private String readline()
	throws java.io.IOException
    {
	StringBuffer s = new StringBuffer();
	int b;
	while ((b=in.readUnsignedByte()) != -1)
	    {
		if ((char)b == '\n') break;
		s.append((char) b);
	    }	
	return new String(s);
    }

    private void write(String s)
	throws java.io.IOException
    {
	out.write(s.getBytes("utf-8"));
	if (debugging) throbber.info(">> "+ s);
    }

    private void skipInput()
	throws java.io.IOException, SMTPException
    {
	int avail;

	while (in.available() != 0) {
	    readResponse();
	}
	
    }

    private void readResponse()
	throws java.io.IOException, SMTPException
    {
	String s;
	s = readline();
	if (debugging) throbber.info("<< "+s + "\n");
	if (s.charAt(0) == '4' || s.charAt(0) == '5') {
	    throw new SMTPException(s);
	};
    }

    public void run(){
	try {
	    int s;	    
	    working = true;

	    String server;

	    String to_part = MailAddress.address_parse(to)[0];
	    if (settings.get_direct()) {
		int index;
		index=to_part.indexOf("@");
		if (index == -1)
		    throw new SMTPException("Invalid 'to' email address");
		server = to_part.substring(index+1);
	    } else {
		server = settings.get_smtp_server();
	    }

	    throbber.info("Using " + server);
	    connect(server);
	    throbber.info("Connected\n");

	    readResponse();
	    write("HELO pensioner.su\r\n");
	    skipInput();

	    if (settings.get_auth_mode() == 1 && !settings.get_direct()) {
		write("AUTH PLAIN " + settings.get_auth_encoded_string() + "\r\n");
		readResponse();
	    }

	    write("MAIL FROM: <"+ settings.get_from() +">\r\n");
	    readResponse();
	    write("RCPT TO: " + MailAddress.address_concat(to_part, "") +  "\r\n");
	    readResponse();
	    write("DATA\r\n");
	    readResponse();
	    write("From: <"+ settings.get_from() +">\r\n" +
		  "To: " + to + "\r\n" +
		  "Subject: " + subj + "\r\n" +
		  "Content-Type: text/plain;\r\n" +
		  "\tcharset=\"utf-8\"\r\n" +
		  "\r\n" +
		  lfToCrLf(body) + "\r\n" +
		  "\r\n.\r\n"
		  );
	    readResponse();
	    write("RSET\r\n");
	    write("QUIT\r\n");

	    working = false;

	    skipInput();
	    throbber.setDone();
	}
	catch (SMTPException ex) {
	    throbber.error(ex.toString());
	}
	catch (java.io.IOException e) {
	    throbber.error(e.toString());
	}
       
    }

    public static String lfToCrLf (String src)
    {
	StringBuffer b = new StringBuffer();
	int idx = 0, lastidx = 0;
	try {
	while ((idx=src.indexOf('\n', idx)) != -1) {
	    if (idx>0 && src.charAt(idx-1) != '\r') {
		b.append(src.substring(lastidx, idx) + "\r\n");
	    } else {
		b.append(src.substring(lastidx, idx) + "\n");
	    }		
	    ++idx;
	    lastidx = idx;
	}
	b.append(src.substring(lastidx));
	}
	catch (StringIndexOutOfBoundsException e) {
	    System.out.println("idx: "+idx+", lastidx: "+lastidx);
	}
	return b.toString();
    }

    public void disconnect()
    {
	try {
	    c.close();
	    in.close();
	    out.close();
	}
	catch (java.io.IOException e) {}
    }

}

