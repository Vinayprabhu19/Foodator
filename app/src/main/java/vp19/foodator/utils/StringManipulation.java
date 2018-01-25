package vp19.foodator.utils;

/**
 * Created by Vinay Prabhu on 19-Jan-18.
 */

public class StringManipulation {
    public static String expandUserName(String username){
        return username.replace("."," ");
    }
    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }
    public static String getTags(String string){
        int index1=0,index2;
        string+=" ";
        String tags="";
        try{
            while(true){
                index1=string.indexOf("#");
                string=string.substring(index1);
                index2=string.indexOf(" ");
                tags= tags+string.substring(0,index2)+" ";
                string=string.substring(index2);
            }
        }
        catch(IndexOutOfBoundsException e){
            return tags;
        }
    }
}
