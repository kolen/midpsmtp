
package org.kolen.smtpclient;

import javax.microedition.lcdui.*;

class OptionsForm extends Form
{
    public TextField   itemSMTPServer;
    public ChoiceGroup itemDirectSend;
    public TextField   itemFromAddress;
    public ChoiceGroup itemAuthMode;
    public TextField   itemAuthLogin;
    public TextField   itemAuthPassword;
    public ChoiceGroup itemFullNames;

    Settings settings;

    public OptionsForm(Settings settings)
    {
	super ("Options");
	String[] ll1={"Direct"};
	String[] ll2={"Show e-mails"};
	String[] authmodes={"None", "Plain"};

	this.settings = settings;

	itemSMTPServer = new TextField("SMTP Server", settings.get_smtp_server(), 256, TextField.ANY);
	itemDirectSend = new ChoiceGroup("Direct send", ChoiceGroup.MULTIPLE, ll1, null);
	itemDirectSend.setSelectedIndex(0, settings.get_direct());
	itemFromAddress = new TextField("From address", settings.get_from(), 256, TextField.EMAILADDR);

	itemAuthMode = new ChoiceGroup("Auth mode", ChoiceGroup.POPUP, authmodes, null);
	itemAuthMode.setSelectedIndex(settings.get_auth_mode(), true);
	itemAuthLogin = new TextField("Login", settings.get_auth_login(), 256, TextField.ANY);
	itemAuthPassword = new TextField("Password", settings.get_auth_password(), 32, TextField.PASSWORD);

	itemFullNames = new ChoiceGroup("Address book", ChoiceGroup.MULTIPLE, ll2, null);
	itemFullNames.setSelectedIndex(0, settings.get_full_names() == 1);

	append(itemSMTPServer);
	append(itemDirectSend);
	append(itemFromAddress);

	append(itemAuthMode);
	append(itemAuthLogin);
	append(itemAuthPassword);	
	
	append(itemFullNames);
    }

    public void saveOptions()
    {
	settings.set_smtp_server  (itemSMTPServer.getString());
	settings.set_direct       (itemDirectSend.isSelected(0));
	settings.set_from         (itemFromAddress.getString());

	settings.auth_set         (itemAuthMode.getSelectedIndex(),
				   itemAuthLogin.getString(),
				   itemAuthPassword.getString()
				   );
	settings.set_full_names   (itemFullNames.isSelected(0)?1: 0);
    }
}
