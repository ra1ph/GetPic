package com.ra1ph.getpic.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ra1ph.getpic.*;
import com.ra1ph.getpic.utils.DialogManager;
import com.ra1ph.getpic.utils.Notificator;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
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
import org.jivesoftware.smackx.packet.*;
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

import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.Writable;
import com.ra1ph.getpic.image.EXIFProcessor;
import com.ra1ph.getpic.map.MapTask;
import com.ra1ph.getpic.map.MapTask.MapRequest;
import com.ra1ph.getpic.message.Message.MessageType;
import com.ra1ph.getpic.users.User;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class XMPPTask extends com.ra1ph.getpic.AsyncTask<Integer, Void, Void>
        implements PacketListener, MessageListener, ChatManagerListener,
        FileTransferListener {

    private static final String GET_XML = "<query xmlns='jabber:iq:getpic'/>";

    public static final int ACTION_LOGIN = 0x0010;
    public static final int ACTION_REGISTER = 0x0020;
    private static final long TIME_SLEEP = 100;
    private static final String SERVER = "31.131.18.161";
    private static final int REPLY_TIMEOUT = 30000;
    private static final int MAX_TRY_COUNT = 5;
    private static final int MAX_CONNECT_COUNT = 10;
    ConnectionConfiguration config;
    XMPPConnection connection;
    ChatManager chatManager;
    DBHelper helper;
    String login, pass, email;
    Context context;
    ConcurrentLinkedQueue<com.ra1ph.getpic.message.Message> messages;
    AtomicBoolean isActive = new AtomicBoolean();
    AtomicBoolean isLogout = new AtomicBoolean();
    Notificator notificator;

    public XMPPTask(Context context, String login, String pass) {
        // TODO Auto-generated constructor stub
        this.login = login;
        this.pass = pass;
        this.context = context;
        isActive.set(true);
        messages = new ConcurrentLinkedQueue<com.ra1ph.getpic.message.Message>();

        notificator = Notificator.getInstance(context);

    }

    @Override
    protected Void doInBackground(Integer... params) {
        // TODO Auto-generated method stub
        SmackConfiguration.setPacketReplyTimeout(10000);
        connect();
        if (params[0] == ACTION_LOGIN) {
            login(login, pass);
            helper = DBHelper.getInstance(context);

            while (isActive.get()) {
                if (isLogout.get()) {
                    sendLogoutBroadcast();
                    isActive.set(false);
                    return null;
                }
                if (!connection.isConnected()) connect();
                if (!connection.isAuthenticated()) login(login, pass);

            /*Presence p = new Presence(Presence.Type.available);
            p.setStatus("Available");
            connection.sendPacket(p);     */

                com.ra1ph.getpic.message.Message mes = null;
                mes = messages.poll();
                if (mes != null) {
                    if (mes.type == MessageType.TEXT) {
                        Message message = new Message();
                        message.setBody(mes.body);
                        message.setTo(mes.user_id);
                        connection.sendPacket(message);
                        sendChatBroadcast(ChatActivity.MESSAGE_SENDED);
                    } else if (mes.type == MessageType.IMAGE) {
                        fileTransfer(new File(context.getExternalCacheDir(), mes.body),
                                mes.user_id);
                    } else if (mes.type == MessageType.GETPIC) {
                        getPicture();
                    }
                    helper.addWritable(mes);

                } else
                    try {
                        Thread.sleep(TIME_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        } else if (params[0] == ACTION_REGISTER) register(login, pass, email);
        return null;
    }

    private void getPicture() {
        //To change body of created methods use File | Settings | File Templates.
        /*IQ iq = new IQ() {
            @Override
            public String getChildElementXML() {
                return GET_XML;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        iq.setTo(MainActivity.BOT_JID);
        connection.sendPacket(iq);  */

        GetPicEvent event = new GetPicEvent();
        event.setGetPictureRequest(true);
        Message msg = new Message();
        msg.addExtension(event);
        msg.setTo(MainActivity.BOT_JID);
        msg.setFrom(connection.getUser());
        connection.sendPacket(msg);

    }

    public void register(String username, String pass, String email) {
        AccountManager am = new AccountManager(connection);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("username", username);
        attributes.put("password", pass);
        attributes.put("email", email);
        if (am.supportsAccountCreation()) {
            try {
                am.createAccount(username, pass, attributes);
                sendRegBroadcast(RegisterActivity.SUCCESS);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d(Constants.DEBUG_TAG, "Register failed!!");
                e.printStackTrace();
            }
        } else {
            Log.d(Constants.DEBUG_TAG, "Server is not support register!!!");
            sendRegBroadcast(RegisterActivity.FAIL);
        }
    }

    public void connect() {
        configure(ProviderManager.getInstance());

        SmackConfiguration.setPacketReplyTimeout(REPLY_TIMEOUT);
        config = new ConnectionConfiguration(SERVER, 5222, SERVER);
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

        config.setReconnectionAllowed(true);
        connection = new XMPPConnection(config);
        // connection.DEBUG_ENABLED = true;
        try {
            connection.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            isActive.set(false);
            XMPPService.sendBroadcast(context, LoginActivity.CONNECTION_FAIL);
            return;
        }
        if (!connection.isConnected()) {
            isActive.set(false);
            XMPPService.sendBroadcast(context, LoginActivity.CONNECTION_FAIL);
            return;
        }

    }

    public void login(String login, String pass) {
        try {
            connection.login(login, pass);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            isActive.set(false);
            XMPPService.sendBroadcast(context, LoginActivity.FAIL);
            return;
        }
        if (connection.isAuthenticated())
            XMPPService.sendBroadcast(context, LoginActivity.SUCCESS);
        else {
            isActive.set(false);
            XMPPService.sendBroadcast(context, LoginActivity.FAIL);
            return;
        }

        new ServiceDiscoveryManager(connection);
        FileTransferManager manager = new FileTransferManager(connection);
        manager.addFileTransferListener(this);
        connection.addPacketListener(this, null);
        chatManager = connection.getChatManager();
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
        int try_count = 0;
        while (try_count < MAX_TRY_COUNT) {
            try_count++;
            OutgoingFileTransfer transfer = manager
                    .createOutgoingFileTransfer(user_id);
            try {
                transfer.sendFile(file, "foto");
            } catch (Exception e) {
                e.printStackTrace();
            }
            int count_connect=0;
            double progress=0;
            while ((!transfer.isDone())&&(count_connect<MAX_CONNECT_COUNT)) {
                count_connect++;
                if(progress!=transfer.getProgress()){
                    progress=transfer.getProgress();
                    count_connect=0;
                }
                if (transfer.getStatus().equals(FileTransfer.Status.in_progress)) {
                    double percents = ((int) (transfer.getProgress() * 10000)) / 100.0;
                    //percents is 100.0 after 1 cycle
                    sendBroadcast(MainActivity.PROGRESS_UPDATE, (int) percents);
                    Log.i(Constants.DEBUG_TAG, "Filetransfer Progress: " + percents + " Status: " + transfer.getStatus().toString());
                } else if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                    System.out.println("ERROR!!! " + transfer.getError());
                } else if (transfer.getStatus().equals(
                        FileTransfer.Status.cancelled)
                        || transfer.getStatus().equals(FileTransfer.Status.refused)) {
                    System.out.println("Cancelled!!! " + transfer.getError());
                }
                if(transfer.getException()!=null){
                    Log.d(Constants.DEBUG_TAG,transfer.getException().getMessage());
                    transfer.cancel();
                    break;
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
                break;
            }
        }
        sendBroadcast(MainActivity.PHOTO_SENDED);
    }

    public void configure(ProviderManager pm) {

        //GetPicService
        pm.addExtensionProvider(GetPicEvent.ELEMENT_ROOT, GetPicEvent.NAMESPACE, new GetpicExtensionProvider());

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

    public void addMessage(com.ra1ph.getpic.message.Message mes) {
        messages.add(mes);
    }

    @Override
    public void fileTransferRequest(final FileTransferRequest request) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                IncomingFileTransfer transfer = request.accept();
                String name = UUID.randomUUID().toString();
                File file = new File(context.getExternalCacheDir(), name);
                try {
                    transfer.recieveFile(file);
                    boolean isOK = true;
                    while (!transfer.isDone()) {
                        try {
                            Thread.sleep(1000L);
                        } catch (Exception e) {
                            isOK = false;
                            Log.e(Constants.DEBUG_TAG, e.getMessage());
                        }
                        if (transfer.getStatus().equals(
                                FileTransfer.Status.error)) {
                            isOK = false;
                            Log.e(Constants.DEBUG_TAG, transfer.getError() + "");
                        }
                        if (transfer.getException() != null) {
                            isOK = false;
                            transfer.getException().printStackTrace();
                        }
                    }
                    if (isOK) {
                        try {
                        } catch (Exception e) {

                        }
                        com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(
                                request.getDescription(), name,
                                com.ra1ph.getpic.message.Message.DIRECTION_IN,
                                Writable.ADD);
                        mes.type = MessageType.IMAGE;
                        helper.addWritable(mes);
                        User user = new User(request.getDescription(), name,
                                Writable.ADD);
                        helper.addWritable(user);
                        loadMap(name);
                        sendBroadcast(MainActivity.PHOTO_RECEIVED);
                        Log.d(Constants.DEBUG_TAG, "is OK");
                        notificator.notifyImage(request.getRequestor());
                    }

                } catch (Exception e) {
                    Log.e(Constants.DEBUG_TAG, e.getMessage());
                }
            }

            ;
        }.start();
    }

    private void loadMap(String filename) {
        String path = context.getCacheDir().getPath() + "/" + filename;
        EXIFProcessor exif = new EXIFProcessor(path);
        MapRequest request = new MapRequest();
        request.latitude = exif.getLatitude();
        request.longitude = exif.getLongitude();
        request.zoom = 3;
        MapTask mapTask = new MapTask(context, request, filename);
        mapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if (message.getBody() != null) {
            com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(
                    message.getFrom(), message.getBody(),
                    com.ra1ph.getpic.message.Message.DIRECTION_IN, Writable.ADD);
            helper.addWritable(mes);
        }
    }

    @Override
    public void processPacket(Packet arg0) {
        // TODO Auto-generated method stub
        if (arg0 instanceof Message) {
            Log.d(Constants.DEBUG_TAG, "Packet in!");
            Message message = (Message) arg0;
            if (message.getExtension(GetPicEvent.NAMESPACE) != null) {
                GetPicEvent event = (GetPicEvent) message.getExtension(GetPicEvent.NAMESPACE);
                if (event.getError() != null) {
                    Log.d(Constants.DEBUG_TAG, event.getError());
                    sendErrorBroadcast(event.getError());
                    sendBroadcast(MainActivity.PHOTO_RECEIVED);
                }
            } else if (message.getBody() != null) {
                com.ra1ph.getpic.message.Message mes = new com.ra1ph.getpic.message.Message(
                        message.getFrom(), message.getBody(),
                        com.ra1ph.getpic.message.Message.DIRECTION_IN,
                        Writable.ADD);
                helper.addWritable(mes);
                notificator.notifyText(message.getFrom(), message.getBody());
            }
        }
    }

    private void sendBroadcast(int ACTION) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.KEY_ACTION, ACTION);
        context.sendBroadcast(intent);
    }

    private void sendErrorBroadcast(String ERROR) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.KEY_ACTION, MainActivity.ERROR);
        intent.putExtra(MainActivity.ERROR_CODE, ERROR);
        context.sendBroadcast(intent);
    }

    private void sendBroadcast(int ACTION, int progress) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.KEY_ACTION, ACTION);
        intent.putExtra(MainActivity.PROGRESS_VALUE, progress);
        context.sendBroadcast(intent);
    }

    private void sendLogoutBroadcast() {
        Intent intent = new Intent(SuperActivity.LOGOUT_BROADCAST_ACTION);
        context.sendBroadcast(intent);
    }

    private void sendRegBroadcast(int ACTION) {
        Intent intent = new Intent(RegisterActivity.REG_BROADCAST_ACTION);
        intent.putExtra(RegisterActivity.REG, ACTION);
        context.sendBroadcast(intent);
    }

    private void sendChatBroadcast(int ACTION) {
        Intent intent = new Intent(ChatActivity.BROADCAST_ACTION);
        intent.putExtra(ChatActivity.KEY_ACTION, ACTION);
        context.sendBroadcast(intent);
    }

}
