package com.ra1ph.getpic.utils;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 17.06.13
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class NickUtils {
    public static String getNickFromId(String user_id){
        return user_id.split("@")[0];
    }
}
