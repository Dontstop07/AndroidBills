package ru.kirill.checksfirstpage.mail.send;

import android.util.Base64;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * Created by oleg on 01.06.13.
 */
public class MailSender {
    InputStream is;
    OutputStream os;
    private LinkedList<Observer> observers;
    private MailSendProperties properties;
    byte readBuffer[];
    final byte answ2[] = "2".getBytes();
    final byte answ3[] = "3".getBytes();
    public MailSender()    {
        is = null;
        os = null;
        readBuffer = new byte[1024];
    }

    public void setProperties(MailSendProperties mailSendProperties) {
        properties = mailSendProperties;
    }

    public void observerRegister(Observer observer) {
        getObservers().add(observer);
    }

    public void observerRemove(Observer observer)
    {
        if(observers != null)
            observers.remove(observer);
    }

    private void echo(String s)
    {
        if(observers != null)
            observersNotify(s);
    }

    void read_smtp_answer()
            throws Exception
    {
        int i = is.read(readBuffer);
        String s = new String(readBuffer, 0, i);
        if(i == 0)
        {
            echo("smtp answer: Unknown error." + s);
            throw new Exception("Unknown error.\n");
        }
        if(readBuffer[0] != answ2[0] && readBuffer[0] != answ3[0])
        {
            echo("smtp answer: SMTP failed:." + s);
            throw new Exception("SMTP failed: " + s + "\n");
        } else
        {
            echo("smtp answer:\n" + s);
            return;
        }
    }

    void write_smtp_response(String s)
            throws Exception
    {
        byte abyte0[] = "\r\n".getBytes();
        os.write(s.getBytes("cp1251"));
        os.write(abyte0);
    }

    public void connect()
            throws Exception
    {
        Socket socket = null;
        echo("open socket ... ");
        try {
            InetAddress serverAddress = InetAddress.getByName(properties.getSMTPServerAddress());
            socket = new Socket(serverAddress, properties.getSMTPport());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        echo("open InputStream ... ");
        is = socket.getInputStream();
        echo("open OutputStream ... ");
        os = socket.getOutputStream();
        read_smtp_answer();
        echo("write EHLO ");
        write_smtp_response("EHLO " + properties.getLoginName());
        read_smtp_answer();
        echo("Authentication ... ");
        String loginName64 = base64Encode(properties.getLoginName());
        echo("user: " + properties.getLoginName() + " base 64: '" + loginName64);
        write_smtp_response("AUTH LOGIN");
        read_smtp_answer();
        echo("login ... ");
        write_smtp_response(loginName64);
        read_smtp_answer();
        echo("password ... ");
        write_smtp_response(base64Encode(properties.getLoginPassword()));
        read_smtp_answer();
        echo("connected.");
    }

    private String base64Encode(String sToEncode) {
        byte[] bytesToEncode = sToEncode.getBytes(Charset.forName("windows-1251"));
        return Base64.encodeToString(bytesToEncode, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public void disconnect()
            throws Exception
    {
        echo("Close connection ... ");
        if(os != null) {
            write_smtp_response("QUIT");
            read_smtp_answer();
        }
    }

    public void send(String s, String s1, String s2, String s3)
            throws Exception
    {
        try
        {
            echo("mail from ... ");
            write_smtp_response("MAIL FROM: " + s);
            read_smtp_answer();
            echo("Check recipient address ... ");
            write_smtp_response("RCPT TO: " + s1);
            read_smtp_answer();
            echo("Send message text ... ");
            write_smtp_response("DATA");
            read_smtp_answer();
            echo("message body ... ");
            StringBuffer stringbuffer = new StringBuffer();
            stringbuffer.append("From: ").append(s1).append("\r\n");
            stringbuffer.append("To: ").append(s1).append("\r\n");
            stringbuffer.append("Subject: ").append(s2).append("\r\n");
            stringbuffer.append("Date: ").append(new Date()).append("\r\n");
            stringbuffer.append(s3).append("\r\n.");

/*
 *     message = "From: " + sender + "\r\n" + message; // добавляем заголовок сообщения "адрес отправителя"
      message = "To: " + reciever + "\r\n" + message; // добавляем заголовок сообщения "адрес получателя"
      message = "Subject: " + subject + "\r\n" + message; // заголовок "тема сообщения"
      message = "Date: " + new Date() + "\r\n" + message; // заголовок "дата отправки"

 */
            write_smtp_response(stringbuffer.toString());
            read_smtp_answer();
            echo("Mail sended.");
        }
        catch(Exception exception)
        {
            echo("Error send mail: " + exception.toString());
            throw exception;
        }
    }

    private List getObservers()
    {
        return getObservers(true);
    }

    private List getObservers(boolean flag)
    {
        if(observers == null)
            observers = new LinkedList<Observer>();
        return observers;
    }

    private void observersNotify(String s) {
        if(observers != null) {
            for(Observer observer: observers) {
                observer.update(null, s);
            }
        }
    }

}
