package com.ra1ph.getpic.service;

import java.io.File;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import com.ra1ph.getpic.Constants;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.Writable;
import com.ra1ph.getpic.message.Message.MessageType;
import com.ra1ph.getpic.users.User;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class XMPPTask extends com.ra1ph.getpic.AsyncTask<Void, Void, Void> implements PacketListener, MessageListener, ChatManagerListener, FileTransferListener {

	private static final long TIME_SLEEP = 100;
	ConnectionConfiguration config;
	XMPPConnection connection;
	ChatManager chatManager;
	DBHelper helper;
	String login,pass;
	Context context;
	ConcurrentLinkedQueue<com.ra1ph.getpic.message.Message> messages;
	AtomicBoolean isActive=new AtomicBoolean();
	
	public XMPPTask(Context context, String login, String pass) {
		// TODO Auto-generated constructor stub
		this.login=login;
		this.pass=pass;
		this.context=context;
		isActive.set(true);
		messages = new ConcurrentLinkedQueue<com.ra1ph.getpic.message.Message>();
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		connect(login,pass);
		helper = DBHelper.getInstance(context);
		
		
		while(isActive.get()){
			com.ra1ph.getpic.message.Message mes=null;
				mes = messages.poll();				
			if(mes!=null){
				if(mes.type==MessageType.TEXT){
					Message message = new Message();
					message.setBody(mes.body);
					message.setTo(mes.user_id);
					connection.sendPacket(message);
				}else if(mes.type==MessageType.IMAGE){
					fileTransfer(new File(context.getCacheDir(),mes.body), mes.user_id);
				}
				
			} else
				try {
					Thread.sleep(TIME_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public void connect(String login, String pass) {
		configure(ProviderManager.getInstance());

		config = new ConnectionConfiguration("jabber.ru", 5222, "jabber.ru");
		config.setDebuggerEnabled(true);

		XMPPConnection.DEBUG_ENABLED = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			config.setTruststoreType("AndroidCAStore");
			config.setTruststorePassword(null);
			config.setTruststorePath(null);
		} else {
			config.setTruststoreType("BKS");
			String path = System.getProperty("javax.net.ssl.trustStore");
			if (path == null)
				path = System.getProperty("java.home") + File.separator + "etc"
						+ File.separator + "security" + File.separator
						+ "cacerts.bks";
			config.setTruststorePath(path);
		}

		connection = new XMPPConnection(config);
		//connection.DEBUG_ENABLED = true;
		try {
			connection.connect();
			connection.login(login, pass);
			new ServiceDiscoveryManager(connection);
			FileTransferManager manager = new FileTransferManager(connection);
			manager.addFileTransferListener(this);
			connection.addPacketListener(this, null);
			chatManager = connection.getChatManager();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Chat createChat(String user_id) {
		return chatManager.createChat(user_id, new MessageListener() {

			@Override
			public void processMessage(Chat chat, Message message) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void fileTransfer(File file, String user_id) {
		
		FileTransferManager manager = new FileTransferManager(connection);
		OutgoingFileTransfer transfer = manager
				.createOutgoingFileTransfer(user_id);
		try {
			transfer.sendFile(file, "foto");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		while (!transfer.isDone()) {
			if (transfer.getStatus().equals(FileTransfer.Status.error)) {
				System.out.println("ERROR!!! " + transfer.getError());
			} else if (transfer.getStatus().equals(
					FileTransfer.Status.cancelled)
					|| transfer.getStatus().equals(FileTransfer.Status.refused)) {
				System.out.println("Cancelled!!! " + transfer.getError());
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (transfer.getStatus().equals(FileTransfer.Status.refused)
				|| transfer.getStatus().equals(FileTransfer.Status.error)
				|| transfer.getStatus().equals(FileTransfer.Status.cancelled)) {
			System.out
					.println("refused cancelled error " + transfer.getError());
		} else {
			System.out.println("Success");
		}
	}

	public void configure(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		/*
		 * pm.addIQProvider("open","http://jabber.org/protocol/ibb", new
		 * IBBProviders.Open());
		 * 
		 * pm.addIQProvider("close","http://jabber.org/protocol/ibb", new
		 * IBBProviders.Close());
		 * 
		 * pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new
		 * IBBProviders.Data());
		 */

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}

	public void addMessage(com.ra1ph.getpic.message.Message mes){
		messages.add(mes);
	}


	@Override
	public void fileTransferRequest(final FileTransferRequest request) {
		// TODO Auto-generated method stub
		new Thread(){
	         @Override
	         public void run() {
	            IncomingFileTransfer transfer = request.accept();
	            File mf = Environment.getExternalStorageDirectory();
	            String name = UUID.randomUUID().toString();
	            File file = new File(context.getCacheDir(),name);
	            try{
	                transfer.recieveFile(file);
	                while(!transfer.isDone()) {
	                   try{
	                      Thread.sleep(1000L);
	                   }catch (Exception e) {
	                      Log.e(Constants.DEBUG_TAG, e.getMessage());
	                   }
	                   if(transfer.getStatus().equals(FileTransfer.Status.error)) {
	                      Log.e(Constants.DEBUG_TAG, transfer.getError() + "");
	                   }
	                   if(transfer.getException() != null) {
	                      transfer.getException().printStackTrace();
	                   }
	                }
	                com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(request.getRequestor(),name,com.ra1ph.getpic.message.Message.DIRECTION_IN);
	                mes.type = MessageType.IMAGE;
	                helper.addWritable(mes);
	                User user = new User(request.getRequestor(),name);
	                helper.addWritable(user);
	                
	             }catch (Exception e) {
	                Log.e(Constants.DEBUG_TAG, e.getMessage());
	            }
	         };
	       }.start();
	}


	@Override
	public void chatCreated(Chat chat, boolean arg1) {
		// TODO Auto-generated method stub
		chat.addMessageListener(this);
	}


	@Override
	public void processMessage(Chat arg0, Message arg1) {
		// TODO Auto-generated method stub
			Message message = arg1;
			if(message.getBody()!=null){
			com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(
					message.getFrom(), message.getBody(),
					com.ra1ph.getpic.message.Message.DIRECTION_IN);
			helper.addWritable(mes);
			}
	}


	@Override
	public void processPacket(Packet arg0) {
		// TODO Auto-generated method stub
		if(arg0 instanceof Message){
			Log.d(Constants.DEBUG_TAG,"Packet in!");
			Message message = (Message) arg0;
			if(message.getBody()!=null){
			com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(
					message.getFrom(), message.getBody(),
					com.ra1ph.getpic.message.Message.DIRECTION_IN);
			helper.addWritable(mes);
			}
		}
	}

}
