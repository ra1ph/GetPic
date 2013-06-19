package com.ra1ph.getpic.service;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 19.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class GetpicIQProvider implements IQProvider {
    @Override
    public IQ parseIQ(final XmlPullParser xmlPullParser) throws Exception {
        IQ iq = new IQ() {
            @Override
            public String getChildElementXML() {
                return xmlPullParser.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        return iq;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
