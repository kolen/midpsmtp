
package org.kolen.smtpclient;

import javax.microedition.lcdui.*;

class Throbber extends Form
{
    Command exitcmd;
    Command okcmd;
    public Throbber(Command exitcmd, Command okcmd)
    {
	super("Sending...");
	this.exitcmd = exitcmd;
	this.okcmd = okcmd;
	addCommand(exitcmd);
    }

    public void error(String description)
    {
	append(description);
    }

    public void info(String description)
    {
	append(description);
    }

    public void setDone()
    {
	append("Done");
	removeCommand(exitcmd);
	addCommand(okcmd);
    }
}
