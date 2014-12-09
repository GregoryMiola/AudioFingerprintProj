package com.tcc.audiofingerprintproj;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class MyFragmentPagerAdapter extends FragmentPagerAdapter{
 
    final int PAGE_COUNT = 2;
    MyFragment myFragment;
    MyFragmentList myFragmentList;
    
    /** Constructor of the class */
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    /** This method will be invoked when a page is requested to create */
    @Override
    public Fragment getItem(int arg0) {
    	Bundle data = new Bundle();
        data.putInt("current_page", arg0+1);
        
        switch(arg0){
	        case 0:
	        	myFragment = new MyFragment(myFragmentList);
	            myFragment.setArguments(data);
	            return myFragment;
	        case 1:
	        	myFragmentList = new MyFragmentList();
	        	myFragmentList.setArguments(data);
	            return myFragmentList;
            default:
            	return null;
        }
    }
 
    /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
        case 0:
        	return "Captura";
        case 1:
        	return "Lista de Músicas";
    	default:
    		return "";
        }
    }
}