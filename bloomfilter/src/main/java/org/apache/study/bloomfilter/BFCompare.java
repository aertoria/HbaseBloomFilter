package org.apache.study.bloomfilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 
 * @author ethan.wang
 *
 */
public class BFCompare {
	final public static int BFSize=10000;
	
	public static void main(String[] args) {
		//Prepare  random numbers
		Random rand = new Random();
		List<Integer> randomList=new ArrayList<Integer>();
		for(int i=0;i<BFCompare.BFSize;i++){
			int n = rand.nextInt(15000) + 1;
			randomList.add(n);
		}
//		System.out.println(randomList);
		
		BF old_bf=new Old_BF();
		randomList.forEach(in -> old_bf.put(in));
		old_bf.checkHowManyCollide();

		BF new_bf=new New_BF();
		new_bf.setRange(Collections.min(randomList.stream().map(i -> FNV(i)).collect(Collectors.toList())), Collections.max(randomList.stream().map(i -> FNV(i)).collect(Collectors.toList())));
		randomList.forEach(in -> new_bf.put(in));
		new_bf.checkHowManyCollide();
		
		BF new_bf2=new New_BF2();
		new_bf2.setRange(Collections.min(randomList.stream().map(i -> FNV(i)).collect(Collectors.toList())), Collections.max(randomList.stream().map(i -> FNV(i)).collect(Collectors.toList())));
		randomList.forEach(in -> new_bf2.put(in));
		new_bf2.checkHowManyCollide();

	}
	
	public static int FNV(final int in){
		final byte[] bytes=ByteBuffer.allocate(4).putInt(in).array();
//		return java.util.Arrays.hashCode(bytes);
		return bytes.hashCode();
	}
	
	

}


interface BF{
	void put(int in);
	
	void checkHowManyCollide();
	
	default void setRange(int minHash, int maxHash){}
}

class Old_BF implements BF{
	Byte[] bits=new Byte[BFCompare.BFSize];
	
	public void put(int in) {
		int hash=BFCompare.FNV(in);
		bits[hash%BFCompare.BFSize]=0;
	}

	public void checkHowManyCollide() {		
		Optional<Integer> sum=new ArrayList<Byte>(Arrays.asList(bits))
		.stream()
		.map(bit -> {
			return bit!=null?1:0;
		})
		.reduce((r1,r2)->(r1+r2));
		
		float occupiedRate=(float)(sum.get())/(float)BFCompare.BFSize;
		System.out.println("OLD BF. Occupied Rate: "+occupiedRate+" \t Collision Rate:"+ (1-occupiedRate));
	}
}



class New_BF implements BF{
	Byte[] bits=new Byte[BFCompare.BFSize];
	int min;
	int max;
	
	int delta=0;
	int threshold=0;
	
	
	public void put(int in) {			
		int hash=BFCompare.FNV(in)-delta; 
		if (hash<=threshold){
			bits[hash%BFCompare.BFSize]=0;
			return;
		}
		//redistribute the reminders
		int re_hash=BFCompare.FNV(hash);
		bits[re_hash%BFCompare.BFSize]=0;
	}

	public void checkHowManyCollide() {		
		Optional<Integer> sum=new ArrayList<Byte>(Arrays.asList(bits))
		.stream()
		.map(bit -> {
			return bit!=null?1:0;
		})
		.reduce((r1,r2)->(r1+r2));
		
		float occupiedRate=(float)(sum.get())/(float)BFCompare.BFSize;
		System.out.println("NEW BF. Occupied Rate: "+occupiedRate+" \t Collision Rate:"+ (1-occupiedRate));
	}
	
	@Override
	public void setRange(int minHash, int maxHash){
		this.delta=minHash%BFCompare.BFSize;//Align the min end with begining of the bf	
		this.threshold=maxHash-delta-(maxHash-delta)%BFCompare.BFSize;
		
	}
}
	
	



class New_BF2 implements BF{
	Byte[] bits;
	int real_size=BFCompare.BFSize;
	
	public void put(int in) {
		int hash=BFCompare.FNV(in);
		bits[hash%real_size]=0;
	}

	public void checkHowManyCollide() {		
		Optional<Integer> sum=new ArrayList<Byte>(Arrays.asList(bits))
		.stream()
		.map(bit -> {
			return bit!=null?1:0;
		})
		.reduce((r1,r2)->(r1+r2));
		
		float occupiedRate=(float)(sum.get())/(float)BFCompare.BFSize;
		System.out.println("NEW BF2. Occupied Rate: "+occupiedRate+" \t Collision Rate:"+ (1-occupiedRate));
	}
	
	@Override
	public void setRange(int minHash, int maxHash){
		int diff=Math.abs(maxHash-minHash);
		int estimateMuliplier=diff/BFCompare.BFSize;
		if(estimateMuliplier>0)real_size=(diff)/estimateMuliplier;
		System.out.println(real_size);
		bits=new Byte[real_size];
	}
}




