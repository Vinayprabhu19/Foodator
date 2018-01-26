/**
 *  Name : FileSearch
 *  Type : Utility java class
 *  ContentView : None
 *  Authentication : None
 *  Purpose : Search for files in storage
 */
package vp19.foodator.utils;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class FileSearch extends Thread implements FileFilter{
    private static final String TAG = "FileSearch";
    //Valid File Extensions
    private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
    public ArrayList<String> pathArray;
    private String directory;
    public FileSearch(String directory){
        super("File Search Thread");
        pathArray=new ArrayList<>();
        this.directory=directory;
        start();
    }
    public void run(){
        Log.d(TAG, "run: Thread Started " + this);
        try{
            getDirectoryPaths();
        }
        catch (Exception e){
            Log.d(TAG, "run: Caught exception"+ e.getMessage());
        }
    }
    /**
     * Implemented method to search if a file is image
     * @param file : file to be checked
     * @return true if file is image else false
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
     * @return List of sub directories
     */
    public void getDirectoryPaths(){
        File file=new File(directory);
        File[] listfiles=file.listFiles();
        for(int i=0;i<listfiles.length;i++){
            if(listfiles[i].isDirectory()){
                if(checkDirectoryForImage(listfiles[i].getAbsolutePath()))
                    pathArray.add(listfiles[i].getName());
            }
        }
        Collections.sort(pathArray, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }
    /**
     * Check if the directory has atleast one image
     * @param directory : directory to be checked
     * @return : true or false
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
     * @param directory : directory to be checked
     * @return List of file paths
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
