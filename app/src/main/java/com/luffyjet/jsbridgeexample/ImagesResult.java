package com.luffyjet.jsbridgeexample;

import com.google.gson.Gson;

import java.util.List;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/20
 * Project : branch_1009
 * Site : http://www.luffyjet.com
 */

public class ImagesResult {
    public List<String> localIds;
    public boolean status;
    public ImagesResult(boolean b) {
        status = b;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
