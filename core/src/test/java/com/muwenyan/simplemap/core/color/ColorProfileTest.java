package com.muwenyan.simplemap.core.color;
public final class ColorProfileTest {
 private ColorProfileTest(){}
 public static void main(String[] args){
  int source=0x804080c0; int balanced=ColorProfile.BALANCED.apply(source);
  if(balanced!=source) throw new AssertionError("balanced");
  int vivid=ColorProfile.VIBRANT.apply(source);
  if((vivid>>>24)!=(source>>>24) || (vivid&255)<(source&255)) throw new AssertionError("profile");
  System.out.println("COLOR_PROFILE_PASS");
 }
}
