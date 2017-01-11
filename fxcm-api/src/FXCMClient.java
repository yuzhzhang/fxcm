import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.messaging.ITransportable;

public class FXCMClient {
    private static String cAccountMassID = "";
    private static String cOpenOrderMassID = "";
    private static String cOpenPositionMassID = "";
    private static String cClosedPositionMassID = "";
    private static String cTradingSessionStatusID = "";
    private static String email = "";
    
    private static Map<String, String> prices = new HashMap();
    private static Object locker = new Object();

    public static void main(String[] args) {
    	
    	prices.put("AUD/JPY", "");
    	prices.put("AUD/NZD", "");
    	prices.put("AUD/USD", "");
    	prices.put("EUR/AUD", "");
    	prices.put("EUR/CAD", "");
    	prices.put("EUR/CHF", "");
    	prices.put("EUR/GBP", "");
    	prices.put("EUR/JPY", "");
    	prices.put("EUR/NOK", "");
    	prices.put("EUR/SEK", "");
    	
    	prices.put("EUR/USD", "");
    	prices.put("GBP/USD", "");
    	prices.put("NZD/USD", "");
    	prices.put("USD/CAD", "");
    	prices.put("USD/CHF", "");
    	prices.put("USD/JPY", "");
    	prices.put("USD/NOK", "");
    	prices.put("USD/SEK", "");
    	
        final IGateway fxcmGateway = GatewayFactory.createGateway();

        fxcmGateway.registerGenericMessageListener(new IGenericMessageListener() {
            public void messageArrived(ITransportable aMessage) throws Exception {
                if (aMessage instanceof MarketDataSnapshot) {
                    MarketDataSnapshot incomingQuote = (MarketDataSnapshot) aMessage;
                    
                    String symbol = incomingQuote.getInstrument().getSymbol();
                    
                    synchronized(locker){
	                    if(prices.containsKey(symbol)){
	                    	prices.put(symbol, "[MKT," + symbol + "," + incomingQuote.getBidClose()+","+incomingQuote.getAskClose() +"]");
	                    	
	                    	//System.out.println(prices.get(symbol));
	                    }
                    }
                }
            }
        });

        try {
            String username = args[0];
            String password = args[1];
            String terminal = args[2];
            String server = args[3];

            FXCMLoginProperties properties = new FXCMLoginProperties(username, password, terminal, server);
            
            System.out.println("client: start logging in");
            fxcmGateway.login(properties);

            cTradingSessionStatusID = fxcmGateway.requestTradingSessionStatus();
            System.out.println(">>> requestTradingSessionStatus = " + cTradingSessionStatusID);
            cAccountMassID = fxcmGateway.requestAccounts();
            System.out.println(">>> requestAccounts = " + cAccountMassID);
            cOpenOrderMassID = fxcmGateway.requestOpenOrders();
            System.out.println(">>> requestOpenOrders = " + cOpenOrderMassID);
            cOpenPositionMassID = fxcmGateway.requestOpenPositions();
            System.out.println(">>> requestOpenPositions = " + cOpenPositionMassID);
            cClosedPositionMassID = fxcmGateway.requestClosedPositions();
            System.out.println(">>> requestClosedPositions = " + cClosedPositionMassID);

            System.out.println("client: done logging in\n");
            
            Socket socket = new Socket("127.0.0.1", 4669);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            while(true){
            	String str = in.readLine();
            	System.out.println(str);
            	if(str.indexOf("MKT") > -1){
            		synchronized(locker){
	                    for(Map.Entry<String, String> entry:prices.entrySet()){
	                    	output.writeChars(entry.getValue());
	                    }
            		}
            	}else if(str.indexOf("ORD") > -1){
            		doSendMail(str);
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doSendMail(String subject){

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "587");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator(){
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication("fxspotlivetrade@gmail.com", "123456ll");
                    }
        }
        );
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("fxspotlivetrade@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("liulinglll@gmail.com"));
            message.setSubject(subject);
            message.setText("");
            Transport.send(message);

        } catch (Exception e) {
        	System.out.println("sending email error: " + e.getMessage());
        }
    }
}
