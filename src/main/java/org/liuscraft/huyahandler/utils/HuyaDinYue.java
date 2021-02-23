package org.liuscraft.huyahandler.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.liuscraft.huyahandler.HuyaHandlerMain;
import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class HuyaDinYue {
    private String userId;
    private int DinYueShu = 0;

    public int getDinYueShu() {
        return DinYueShu;
    }

    public boolean initDinYue(long userId){
        this.userId = String.valueOf(userId);
        DinYueShu = getNewDinYue();
        if (DinYueShu>0){
            return true;
        }
        return false;
    }
    public int getNewDinYue(){
        try {
            URL url = new URL("https://v.huya.com/u/"+userId);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"utf-8"));
            String nextString;
            while ((nextString = bufferedInputStream.readLine())!=null){
                int i = nextString.indexOf("<span>订阅：<em>");
                if (i!=-1){
                    i+=13;
                    i = Integer.parseInt(nextString.substring(i, nextString.indexOf("</em></span>",i)));
                    if (DinYueShu != 0){
                        if (i<DinYueShu){
                            i = 0;
                        }else {
                            i = i - DinYueShu;
                            DinYueShu += i;
                        }
                    }
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
