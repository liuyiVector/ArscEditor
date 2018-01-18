package com.cmy;

import java.lang.reflect.Array;
import java.util.*;

class Solution {
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(int x) { val = x; }
    }

//    public int findKthLargest(int[] nums, int k) {
//
//    }


    public String getString(int count, String s){
        String result = "";
        if(!s.contains("[")){
            for(int i = 0; i < count; i++)
                result += s;
            return result;
        }
        int start = -1, i = 0, c = 1;
        for(i = 0; i < s.length(); i++){
            if(Character.isDigit(s.charAt(i))){
                if(start == -1){
                    start = i;
                }
                if(!Character.isDigit(s.charAt(i+1))){
                    c = Integer.parseInt(s.substring(start, i + 1));
                    break;
                }
            }

        }
        int left = 0, index;
        String tmp = "";
        for(index = i; index < s.length(); index++){
            if(s.charAt(index) == '['){
                left++;
            }else if(s.charAt(index) == ']'){
                left--;
                if(left == 0){
                    tmp = getString(c, s.substring(i + 2, index));
                    break;
                }
            }
        }
        return getString(count, s.substring(0, start) + tmp + s.substring(index + 1, s.length()));

    }


    public String DFS(String s, int index){
        String results = "";
        int cnt = 0;
        while(index < s.length()){
            if(Character.isDigit(s.charAt(index))){
                cnt = cnt * 10 + (s.charAt(index) - '0');
            }
        }
        return null;
    }

    public String decodeString(String s) {
        return getString(1, s);
    }



    public List<Integer> grayCode(int n) {
        List<Integer> results = new ArrayList();
        return results;
    }

    public static void main(String[] args){
        System.out.println(new Solution().decodeString("2[abc]3[cd]ef"));
    }


}