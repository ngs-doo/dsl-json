package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CompiledJson
public class GenericWithVariousTypes<A, B, C> {
	public List<A> list;
	public Set<B> set;
	public Map<B, Integer> map1;
	public ArrayList<C> arrayList;
	public A[] array;
	public Map<C, A> map2;
	public GenericWithVariousTypes<C, B, A> self;
	public GenericWithVariousTypes<B, B, A>[] selfArray;
	public List<GenericWithVariousTypes<A, B, A>> selfList;
}
