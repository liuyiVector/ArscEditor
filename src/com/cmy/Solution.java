package com.cmy;

import java.util.*;

class Solution {
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(int x) { val = x; }
    }

    public boolean PredictTheWinner(int[] nums) {
        return helper(nums, 0, nums.length - 1) >= 0;
    }

    public int helper(int[] nums, int start, int end){
        if(start == end)
            return nums[start];
        else
            return Math.max(nums[start] - helper(nums, start + 1, end), nums[end] - helper(nums, start, end - 1));
    }

    public static void main(String[] args){
        //System.out.println(new Solution().isSubsequence("axc","ahbgdc"));
    }


}