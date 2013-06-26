package com.ra1ph.getpic.service;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 25.06.13
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class GetPicEvent implements PacketExtension {
    public final static String NAMESPACE="jabber:x:getpic";
    public final static String ELEMENT_ROOT="x";
    public static final String GET_PIC_REQUEST = "getpicrequest";
    public static final String ERROR_TAG = "error";

    private String id;
    private String error=null;
    private boolean getpicture=false;

    public void setPacketID(String id){
        this.id=id;
    }

    public String getPacketID(){
        return id;
    }

    @Override
    public String getElementName() {
        return ELEMENT_ROOT;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setError(String error){
        this.error = error;
    }

    public String getError(){
        return error;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append(
                "\">");
        // Note: Cancellation events don't specify any tag. They just send the packetID

        // Add the offline tag if the sender requests to be notified of offline events or if
        // the target is offline
       /* if (isOffline())
            buf.append("<").append(MessageEvent.OFFLINE).append("/>");
        // Add the delivered tag if the sender requests to be notified when the message is
        // delivered or if the target notifies that the message has been delivered
        if (isDelivered())
            buf.append("<").append(MessageEvent.DELIVERED).append("/>");
        // Add the displayed tag if the sender requests to be notified when the message is
        // displayed or if the target notifies that the message has been displayed
        if (isDisplayed())
            buf.append("<").append(MessageEvent.DISPLAYED).append("/>");
        // Add the composing tag if the sender requests to be notified when the target is
        // composing a reply or if the target notifies that he/she is composing a reply
        if (isComposing())
            buf.append("<").append(MessageEvent.COMPOSING).append("/>");
        // Add the id tag only if the MessageEvent is a notification message (not a request) */
        if(isGetPictureRequest())
            buf.append("<").append(GetPicEvent.GET_PIC_REQUEST).append("/>");
        if (getError() != null)
            buf.append("<error>").append(getError()).append("</error>");
        if (getPacketID() != null)
            buf.append("<id>").append(getPacketID()).append("</id>");
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    public boolean isGetPictureRequest() {
        return getpicture;  //To change body of created methods use File | Settings | File Templates.
    }

    public void setGetPictureRequest(boolean getpicture){
        this.getpicture = getpicture;
    }
}
