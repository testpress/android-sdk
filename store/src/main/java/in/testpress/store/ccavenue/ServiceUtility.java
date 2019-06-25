package in.testpress.store.ccavenue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;


public class ServiceUtility {

    public java.util.Map readProperties(String pFilePath)throws IOException {
        java.util.Map vPropertyMap=new LinkedHashMap();
        Set vTckey;
        Iterator<String> vTcPropItr;
        Properties vTCProperties=null;
        try {
            vTCProperties = new Properties();
            vTCProperties.load(ServiceUtility.class.getResourceAsStream(pFilePath));
            vTckey = vTCProperties.keySet();
            vTcPropItr = vTckey.iterator();
            vPropertyMap=new LinkedHashMap();
            while(vTcPropItr.hasNext()){
                String vKey=vTcPropItr.next();
                vPropertyMap.put(vKey, vTCProperties.get(vKey));
            }
        }finally{
            vTcPropItr=null;
            vTckey=null;
            vTCProperties=null;
        }
        return vPropertyMap;
    }

    public static Object chkNull(Object pData){
        return (pData==null?"":pData);
    }

    public static java.util.Map tokenizeToHashMap(String msg, String delimPairValue, String delimKeyPair) throws Exception {
        java.util.Map keyPair = new HashMap();
        ArrayList respList = new ArrayList();
        String part = "";
        StringTokenizer strTkn = new StringTokenizer(msg, delimPairValue, true);
        while (strTkn.hasMoreTokens())
        {
            part = (String)strTkn.nextElement();
            if(part.equals(delimPairValue)) {
                part=null;
            }
            else {
                respList = tokenizeToArrayList(part,delimKeyPair);
                if(respList.size() == 2)keyPair.put(respList.get(0), respList.get(1));
                else if (respList.size() == 1) keyPair.put(respList.get(0), null);
            }
            if(part == null) continue;
            if(strTkn.hasMoreTokens()) strTkn.nextElement();
        }
        return keyPair.size() > 0 ? keyPair : null;
    }

    public static ArrayList tokenizeToArrayList(String msg, String delim) throws Exception {
        ArrayList respList = new ArrayList();
        String varName = null;
        String varVal = null;
        int index = msg.indexOf(delim);
        varName = msg.substring(0,index);
        if((index+1)!=msg.length())
            varVal = msg.substring(index+1,msg.length());
        respList.add(varName);
        respList.add(varVal);
        return respList.size() > 0 ? respList : null;
    }

    public static String addToPostParams(String paramKey, String paramValue){
        if(paramValue!=null)
            return paramKey.concat(Constants.PARAMETER_EQUALS).concat(paramValue)
                    .concat(Constants.PARAMETER_SEP);
        return "";
    }

    public static int randInt(int min, int max) {
        // Usually this should be a field rather than a method variable so
        // that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
