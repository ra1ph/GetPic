package com.ra1ph.getpic.service;


import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 25.06.13
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public class GetpicExtensionProvider implements PacketExtensionProvider {

    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        GetPicEvent getpicEvent = new GetPicEvent();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("id"))
                    getpicEvent.setPacketID(parser.nextText());
                if (parser.getName().equals(GetPicEvent.ERROR_TAG))
                getpicEvent.setError(parser.nextText());
                if(parser.getName().equals(GetPicEvent.GET_PIC_REQUEST))
                    getpicEvent.setGetPictureRequest(true);

            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("x")) {
                    done = true;
                }
            }
        }
        return getpicEvent;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
