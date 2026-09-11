package com.muwenyan.simplemap.core.lod;
public final class LodSelectorTest {
 private LodSelectorTest(){}
 public static void main(String[] args){
  var selected=LodSelector.select(32,128,5);
  if(selected.isEmpty() || selected.get(0).level()!=0) throw new AssertionError("lod order");
  for(int i=1;i<selected.size();i++) if(selected.get(i).scale()<=selected.get(i-1).scale()) throw new AssertionError("lod scale");
  if(LodSelector.select(0,1,0).size()!=1) throw new AssertionError("center page");
  System.out.println("LOD_SELECTOR_PASS");
 }
}
