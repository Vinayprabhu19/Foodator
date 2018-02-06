/**
 *  Name : StringManipulation
 *  Type : Utility java class
 *  ContentView : None
 *  Authentication : None
 *  Purpose : To provide methods for manipulating various strings
 */
package vp19.foodator.utils;
public class StringManipulation {
    /**
     * Inputs username and returns the same with "." replaced by " "
     * @param username : Username of the user
     * @return expanded Username
     */
    public static String expandUserName(String username){
        return username.replace("."," ");
    }
    /**
     * Inputs username and returns the same with " " replaced by "."
     * @param username : Username of the user
     * @return condensed Username
     */
    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }

    /**
     * Checks if a string is null
     * @param str : String to checked
     * @return true if null , false if not
     */
    public static boolean isStringNull(String str) {
        str=str.replace(" ","");
        if(str.equals("")) {
            return true;
        }
        return false;
    }
    /**
     *Obtain various tags in the description
     * @param string: String which consists of normal text and tags starting with #
     * @return string consisting of various tags in the string seperated by blank space
     */
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
