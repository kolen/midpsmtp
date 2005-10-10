//
// $Id: SMTPClient.java,v 1.5 2005/10/10 16:14:52 kolen Exp $
//  
//  midpsmtp - MIDP SMTP Client
//
package org.kolen.smtpclient;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import java.util.Enumeration;
import java.io.IOException;

public class SMTPClient extends MIDlet implements CommandListener {
    private Display display;

    private Form mainForm;

    private Command cmdExit = new Command("Exit", Command.EXIT, 2);
    private Command cmdSend = new Command("Send", Command.OK, 1);
    private Command cmdThrobberCancel = new Command("Cancel", Command.CANCEL, 1);
    private Command cmdAbout = new Command("About", Command.SCREEN, 50);

    private Command cmdOptions = new Command("Options", Command.SCREEN, 20);
    private Command cmdOptionsOK = new Command("OK", Command.OK, 2);
    private Command cmdOptionsCancel = new Command("Cancel", Command.CANCEL, 2);

    private Command cmdMailbook = new Command("Address book", Command.SCREEN, 15);
    private Command cmdMailbookOK = new Command("Select", Command.OK, 1);
    private Command cmdMailbookCancel = new Command("Cancel", Command.CANCEL, 2);
    private Command cmdMailbookInsert = new Command("New addr", Command.SCREEN, 3);
    private Command cmdMailbookEdit = new Command("Edit", Command.SCREEN, 4);
    private Command cmdMailbookDelete = new Command("Delete", Command.SCREEN, 5);

    private Command cmdMailbookNewOK = new Command("OK", Command.OK, 1);
    private Command cmdMailbookNewCancel = new Command("Cancel", Command.CANCEL, 2);

    private Command cmdGenericOK = new Command("OK", Command.OK, 1);

    private Throbber throbber;
    private Settings settings = new Settings();
    private SMTPSocket smtpsocket;
    private Thread sendingThread;

    private boolean firsttime = true;

    TextField txtTo = new TextField("To", "", 256, TextField.EMAILADDR);
    TextField txtSubj = new TextField("Subj", "", 256, TextField.ANY);
    TextField txtBody = new TextField("- Message -", "", 256, TextField.ANY);

    private int mailbookEditingIndex = -1;

    private List listMailbook;

    private Image imgMailbook = null;
    private Image imgMailbookNone = null;
    private Image imgIcon = null;

    public SMTPClient() {
	display = Display.getDisplay(this);
    }

    public void startApp()
    {
	if (firsttime) {	    
	    mainForm = new Form("SMTP Client");

	    mainForm.addCommand(cmdExit);
	    mainForm.addCommand(cmdSend);
	    mainForm.addCommand(cmdOptions);
	    mainForm.addCommand(cmdMailbook);
	    mainForm.addCommand(cmdAbout);

	    mainForm.append(txtTo);
	    mainForm.append(txtSubj);
	    mainForm.append(txtBody);

	    mainForm.setCommandListener(this);

	    loadImages();

	    display.setCurrent(mainForm);	   
	    
	    firsttime = false;
	}
	else {
	    
	}
    }

    public void destroyApp(boolean unconditional) {}

    public void pauseApp() {}

    public void commandAction(Command c, Displayable d) {
	
	// ------- cmdExit ------- : Exit from main menu

	if (c == cmdExit) {
	    destroyApp(false);
	    notifyDestroyed();
	} 

	// ------- cmdSend ------- : Send

	else if (c == cmdSend) {
	    try {
		throbber = new Throbber(cmdThrobberCancel, cmdGenericOK);
		throbber.setCommandListener(this);

		smtpsocket = new SMTPSocket(
					    txtTo.getString(),
					    txtSubj.getString(),
					    txtBody.getString(),
					    throbber,
					    settings
					    );
		sendingThread = new Thread(smtpsocket);

		showThrobber(true);

		sendingThread.start();
	    }
	    catch (java.io.IOException e) {}
	}

	// ------- cmdThrobberCancel -------: Cancel running network op

	else if (c == cmdThrobberCancel) {
	    if (sendingThread.isAlive() && smtpsocket.working)
		smtpsocket.disconnect();
	    showThrobber(false);
	}

	// ------- cmdOptions ------- : Go to options menu

	else if (c == cmdOptions) {
	    OptionsForm f = new OptionsForm(settings);
	    f.addCommand(cmdOptionsOK);
	    f.addCommand(cmdOptionsCancel);
	    f.setCommandListener(this);

	    display.setCurrent(f);
	}

	// ------- cmdOptionsOK --------: Save options

	else if (c == cmdOptionsOK) {
	    OptionsForm f = (OptionsForm)d;
	    f.saveOptions();
	    display.setCurrent(mainForm);
	}

	// -------- cmdOptionsCancel --------: Exit from options menu

	else if (c == cmdOptionsCancel) {
	    display.setCurrent(mainForm);	    
	}

	// -------- cmdMailbook --------: Enter mailbook menu

	else if (c == cmdMailbook) {
	    List l = new List("Address book", List.IMPLICIT);
	    Enumeration e = settings.enum_mailbook();
	    l.append("(Cancel)", imgMailbookNone);
	    while (e.hasMoreElements())
		{
		    String address_full = (String)e.nextElement();
		    String [] address_parts;
		    address_parts = MailAddress.address_parse(address_full);
		    if (settings.get_full_names() == 1) {
			l.append(address_full, imgMailbook);
		    }
		    else if (address_parts[1].trim().length() != 0) {
			l.append(address_parts[1], imgMailbook);
		    } else {
			l.append(address_parts[0], imgMailbook);
		    }
		}
	    l.setSelectCommand(cmdMailbookOK);
	    l.addCommand(cmdMailbookOK);	    
	    l.addCommand(cmdMailbookCancel);
	    l.addCommand(cmdMailbookInsert);
	    l.addCommand(cmdMailbookEdit);
	    l.addCommand(cmdMailbookDelete);
	    l.setCommandListener(this);
	    listMailbook = l;
	    display.setCurrent(l);
	}

	// -------- cmdMailbookOK ---------: Selection in mailbook

	else if (c == cmdMailbookOK) {
	    List l = (List)d;
	    int selected;
	    selected = l.getSelectedIndex();
	    if (selected > 0) {
		String email;
		email = settings.mailbook_get(selected-1);
		txtTo.setString(email);
	    }
	    display.setCurrent(mainForm);
	}

	// -------- cmdMailbookCancel --------: Exit from mailbook

	else if (c == cmdMailbookCancel) {
	    display.setCurrent(mainForm);
	}

	// -------- cmdMailbookInsert --------: Add new address
	// -------- cmdMailbookEdit ---------: Edit address
	
	else if (c == cmdMailbookInsert || c == cmdMailbookEdit) {

	    if (c == cmdMailbookEdit) {
		List l = (List)d;
		mailbookEditingIndex = l.getSelectedIndex() - 1;
		if (mailbookEditingIndex < -1) mailbookEditingIndex = -1;
	    }
	    else
		mailbookEditingIndex = -1;

	    Form f = new Form(mailbookEditingIndex == -1 ? 
			      "New address" : "Edit address"
			      );
	    String def;
	    if (mailbookEditingIndex == -1) def = "";
	    else def = settings.mailbook_get(mailbookEditingIndex);

	    String [] def2 = MailAddress.address_parse(def);

	    f.append(new TextField("e-mail", def2[0], 256, TextField.EMAILADDR));
	    f.append(new TextField("name",   def2[1], 256, TextField.ANY));
	    f.addCommand(cmdMailbookNewOK);
	    f.addCommand(cmdMailbookNewCancel);
	    f.setCommandListener(this);
	    display.setCurrent(f);
	}

	// -------- cmdMailbookDelete ---------

	else if (c == cmdMailbookDelete) {
	    List l = (List)d;
	    int i;
	    i = l.getSelectedIndex() - 1;
	    if (i >= 0) {
		settings.mailbook_delete(i);
		l.delete(i+1);
	    }
	}

	// -------- cmdMailbookNewOK / cmdMailbookNewCancel --------

	else if (c == cmdMailbookNewOK) {
	    Form f = (Form)d;
	    TextField ff =  (TextField)f.get(0);
	    TextField ff1 = (TextField)f.get(1);
	    String email = ff.getString();
	    String name  = ff1.getString();

	    String full = MailAddress.address_concat(email, name);

	    if (mailbookEditingIndex == -1) {
		settings.mailbook_add(full);
		listMailbook.append(full, imgMailbook);
	    } else {
		listMailbook.set(mailbookEditingIndex+1, full, imgMailbook);
		settings.mailbook_set(mailbookEditingIndex, full);
	    }

	    display.setCurrent(listMailbook);
	}	
	else if (c == cmdMailbookNewCancel) {
	    display.setCurrent(listMailbook);
	}

	// --------- cmdAbout ---------------
	
	else if (c == cmdAbout) {
	    Form f = new Form("About");
	    f.append(new ImageItem(
				   "MIDP SMTP Client",
				   imgIcon,
				   ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_NEWLINE_AFTER,
				   ""
				   ));
	    f.append(getAppProperty("MIDlet-Version"));
	    f.append("Project webpage: http://midpsmtp.sourceforge.net");
	    f.get(f.size()-1).setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_BEFORE);
	    f.addCommand(cmdGenericOK);
	    f.setCommandListener(this);
	    display.setCurrent(f);
	}

	// --------- cmdGenericOK -----------------
	else if (c == cmdGenericOK) {
	    display.setCurrent(mainForm);
	}

    }

    public void showThrobber(boolean show)
    {
	if (show)
	    display.setCurrent(throbber);
	else
	    display.setCurrent(mainForm);
    }

    private void loadImages()
    {
	try {
	    imgMailbook = Image.createImage("/img/mailbook.png");
	    imgMailbookNone = Image.createImage("/img/no.png");
	    String micon = getAppProperty("MIDlet-Icon");
	    if (micon != null)
		imgIcon = Image.createImage(getAppProperty("MIDlet-Icon"));
	}
	catch (IOException e) {}
    }

}

