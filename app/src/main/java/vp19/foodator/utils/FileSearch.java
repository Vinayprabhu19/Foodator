package vp19.foodator.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by Vinay Prabhu on 23-Jan-18.
 */

public class FileSearch implements FileFilter{
    //Valid File Extensions
    private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
    /**
     * Implemented method to search if a file is image
     * @param file
     * @return
     */
    public  boolean accept(File file)
    {
        for (String extension : okFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Search a directory and recursively obtain other directories
     * @param directory
     * @return
     */
    public  ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file=new File(directory);
        File[] listfiles=file.listFiles();
        for(int i=0;i<listfiles.length;i++){
            if(listfiles[i].isDirectory() && !listfiles[i].isHidden() ){//&& checkDirectoryForImage(listfiles[i].getAbsolutePath())
                pathArray.add(listfiles[i].getName());
            }
        }
        return  pathArray;
    }
    /**
     * Check if the directory has atleast one image
     * @param directory
     * @return
     */
    public boolean checkDirectoryForImage(String directory){
        File file=new File(directory);
        File[] listfiles=file.listFiles();
        for(int i=0;i<listfiles.length;i++){
            if(listfiles[i].isFile() && accept(listfiles[i])){
                return true;
            }
        }
        return false;
    }

    /**
     * Search directory and obtain the files inside that directory
     * @param directory
     * @return
     */
    public  ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file=new File(directory);
        File[] listfiles=file.listFiles();
        for(int i=0;i<listfiles.length;i++){
            if(listfiles[i].isFile() && accept(listfiles[i])){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return  pathArray;
    }
}
