
package org.kolen.smtpclient;

import javax.microedition.rms.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

import org.kolen.Base64;
import java.lang.*;

//import MailAddress;

class Settings {

    private static final String RECORD_STORE_NAME = "SMTPClientSettings";
    private static final int RID_SMTP_SERVER = 1;
    private static final int RID_DIRECT = 2;
    private static final int RID_FROM = 3;
    private static final int RID_MAILBOOK = 4;
    private static final int RID_AUTH_SETTINGS = 5;
    private static final int RID_FULL_NAMES = 6;

    private static final int RID_END = 7;

    private RecordStore rs = null;

    private String smtp_server;
    private boolean direct;
    private String from;

    private int     auth_mode;
    private String  auth_login;
    private String  auth_password;
    private String  auth_encoded_string;
    private int full_names;

    // Initial capacity: 16
    public Vector mailbook;

    public Settings()
    {
	try{
	    open();
	}
	catch (RecordStoreException e) {}
    }

    private void open()
	throws RecordStoreException
    {
	rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
	int numrecords = rs.getNumRecords();
	if (numrecords < RID_END)
	{
	    int i;
	    for (i=numrecords; i<RID_END; i++)
		rs.addRecord(null, 0, 0 );
	}
	byte[] smtp_server_bytes = rs.getRecord(RID_SMTP_SERVER);
	byte[] direct_bytes = rs.getRecord(RID_DIRECT);
	byte[] from_bytes = rs.getRecord(RID_FROM);
	byte[] full_names_bytes = rs.getRecord(RID_FULL_NAMES);

	this.smtp_server = smtp_server_bytes!=null? new String(smtp_server_bytes) : "localhost";
	this.direct = (direct_bytes!=null && direct_bytes[0]==1);
	this.from = from_bytes!=null? new String(from_bytes) : "dude@localhost";
	this.full_names = (full_names_bytes!=null && full_names_bytes[0]==1)?1:0;

	load_mailbook();
	auth_read();
    }

    public String get_smtp_server()
    {
	return smtp_server;
    }
    public void set_smtp_server(String val)
    {
	try{
	    rs.setRecord(RID_SMTP_SERVER, val.getBytes(), 0, val.length() );
	    smtp_server = val;
	} catch (RecordStoreException rs) {}
    }

    public String get_from()
    {
	return from;
    }
    public void set_from(String val)
    {
	try{
	    rs.setRecord(RID_FROM, val.getBytes(), 0, val.length() );
	    from = val;
	} catch (RecordStoreException rs) {}
    }


    public boolean get_direct()
    {
	return direct;
    }
    public void set_direct(boolean val)
    {
	byte[] bts = new byte[1];
	if (val) 
	    bts[0] = 1;	
	else
	    bts[0] = 0;
	try{
	    rs.setRecord(RID_DIRECT, bts, 0, 1);
	    direct = val;
	} catch (RecordStoreException rs) {}
    }



    private void save_mailbook()
	throws RecordStoreException
    {
	ByteArrayOutputStream ba = new ByteArrayOutputStream();
	DataOutputStream os = new DataOutputStream(ba);
	try {
	    for (Enumeration e = mailbook.elements(); e.hasMoreElements(); )
		{
		    os.writeUTF((String)e.nextElement());
		}
	    rs.setRecord(RID_MAILBOOK, ba.toByteArray(), 0, ba.size());	    
	}
	catch (IOException e) {}
    }

    private void load_mailbook()
	throws RecordStoreException
    {
	mailbook = new Vector(16);
	byte[] mb_bytes = rs.getRecord(RID_MAILBOOK);
	if (mb_bytes == null) return;
	ByteArrayInputStream ba = new ByteArrayInputStream(mb_bytes);
	DataInputStream is = new DataInputStream(ba);

	try {
	    while (true)
		mailbook.addElement(is.readUTF());
	}
	catch (EOFException e) {}
	catch (IOException ie) {}
    }   

    public Enumeration enum_mailbook()
    {
	return mailbook.elements();
    }
    
    public void mailbook_add(String address)
    {
	try {
	    mailbook.addElement(address);
	    save_mailbook();
	}
	catch (RecordStoreException e) {}
    }

    public void mailbook_set(int index, String address)
    {
	try {
	    mailbook.setElementAt(address, index);
	    save_mailbook();
	}
	catch (RecordStoreException e) {}
    }

    public String mailbook_get(int index)
    {
	return (String)mailbook.elementAt(index);
    }

    public void mailbook_delete(int index)
    {
	try {
	    mailbook.removeElementAt(index);
	    save_mailbook();
	}
	catch (RecordStoreException e) {}
    }

    public int get_auth_mode()
    { return auth_mode; }
    public String get_auth_login()
    { return auth_login; }
    public String get_auth_password()
    { return auth_password; }
    public String get_auth_encoded_string()
    { return auth_encoded_string; }

    public int get_full_names()
    { return full_names; }
    public void set_full_names(int val)
    {
	System.out.println("Set value: "+val);
	byte[] bts = new byte[1];
	bts[0] = (byte) val;
	try{
	    rs.setRecord(RID_FULL_NAMES, bts, 0, 1);
	    full_names = val;
	} catch (RecordStoreException rs) {}
    }

    public void auth_set(int mode, String login, String password)
    {
	auth_mode = mode;
	auth_login = login;
	auth_password = password;

	//	String encoded_login = Base64.encode(login);
	//	String encoded_password = Base64.encode(password);
	//	auth_encoded_string = encoded_login+encoded_login+encoded_password;
	auth_encoded_string = Base64.encode(login+"\0"+login+"\0"+password);

	ByteArrayOutputStream ba = new ByteArrayOutputStream();
	DataOutputStream os = new DataOutputStream(ba);
	try {
	    os.writeInt(mode);
	    os.writeUTF(login);
	    os.writeUTF(password);
	    os.writeUTF(auth_encoded_string);

	    rs.setRecord(RID_AUTH_SETTINGS, ba.toByteArray(), 0, ba.size());
	}
	catch (Exception e) {e.printStackTrace();}	
    }
    
    private void auth_read()
    {
	//Defaults:
	auth_mode = 0;
	auth_login = "";
	auth_password = "";
	auth_encoded_string = "";

	try {
	    byte [] bytes = rs.getRecord(RID_AUTH_SETTINGS);;
	    if (bytes == null) return;
	    ByteArrayInputStream ba = new ByteArrayInputStream(bytes);
	    DataInputStream is = new DataInputStream(ba);

	    auth_mode = is.readInt();
	    auth_login = is.readUTF();
	    auth_password = is.readUTF();
	    auth_encoded_string = is.readUTF();
	}
	catch (Exception e) { e.printStackTrace(); }	
    }
}
