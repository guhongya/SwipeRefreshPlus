package com.gu.swiperefreshplush;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void leeCode_1(){
        int []nums={3,2,4};
        int target=6;
        int length=nums.length;
        boolean flag=false;
        int []result=new int[2];
        for(int i=0;i<length;i++){
            for(int j=i+1;j<length;j++){
                if(nums[i]+nums[j]==target){
                    result[0]=i;
                    result[1]=j;
                    flag=true;
                    break;
                }
            }
            if(flag)
                break;
        }
        assertEquals(1,result[0]);
    }
}