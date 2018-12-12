package cloudsim.ext.datacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import cloudsim.VirtualMachine;
import java.util.*;
public class ACO extends VmLoadBalancer{
	 double[][] per;
	 double a=2;
//	static final double b=1;
	 double oup=1;
	 double ef=0.5;
	 int nants=100;
	
	Ant[] ants;
	DatacenterController dcbLocal;
	
	public ACO(DatacenterController d)
	{
		super();
//		System.out.println("IN ACO");
		dcbLocal=d;
	}

	public int getNextAvailableVm() {
		per=new double[dcbLocal.vmlist.size()+1][dcbLocal.vmlist.size() + 1];
		ants = new Ant[nants];
		for (int i=0;i<ants.length; i++)
		{
			ants[i]=new Ant(per);

			ants[i].SendAnt();
			per=ants[i].UpdateGlobalPheromones();
//			Print(per);
			evaporate();
		}
		
		Ant ansAnt=new Ant(per);
		int vmAll=ansAnt.FetchFinalVm();
		allocatedVm(vmAll);
		return vmAll;
	}
	public void Print(double[][] per) {
		for(int i=0;i<per.length;i++) {
			for(int j=0;j<per.length;j++) {
				System.out.print(per[i][j]);
				System.out.print(" ");
			}
			System.out.println("");
		}
	}
	
	public void evaporate(){
		
		for (int i=0;i<per.length;i++)
		{
			for (int j=0;j<per.length;j++)
			{
				per[i][j] *=(1-ef);
			}
		}
	}
	
	public class Ant {
		double[][] per;
		boolean[] isVisited;
		List<Integer> path;
		int tempId;
		
		public Ant(double[][] ph) {
			per=ph;
			isVisited = new boolean[per.length];//number of vms
			tempId = isVisited.length-1;
			path=new ArrayList<Integer>();
		}
		
		public int SendAnt() {
			return ProcessAnt(true);
		}
		
		public int FetchFinalVm() {
			return ((VirtualMachine) dcbLocal.vmlist.get(ProcessAnt(false))).getVmId();
		}
		public int ProcessAnt(boolean update_per) {
			int CurrentVmId= tempId;
			int nextVmId= getNextVmNode(CurrentVmId);
			
			if(update_per) {
				UpdatePheromone(CurrentVmId, nextVmId);
			}
			while(nextVmId != CurrentVmId) {
				path.add(nextVmId);
				CurrentVmId =nextVmId;
				nextVmId= getNextVmNode(CurrentVmId);
				if(update_per) {
					UpdatePheromone(CurrentVmId,nextVmId);
				}
			}
			if(update_per) {
				UpdateGlobalPheromones();
			}
			return CurrentVmId;
		}
		
		public int getNextVmNode(int vmId) {
			double[] probab=computeProbability(vmId);
			Random rand = new Random();
			double randomization = rand.nextDouble();
			for (int i=0;i<probab.length;i++) {
				randomization= randomization - probab[i];
				if (randomization<=0) {
					return i;
				}
			}
			
			return -1;
		}
	
		public double[] computeProbability(int vmId) {
			double[] probability = new double[per.length-1];
			double sum=0.0;
			for (int i=0;i<probability.length;i++)
			{
				if (isVisited[i]) {
					probability[i]=0;
					continue;
				}
				
				probability[i]=scoreFunction(vmId, i);
				sum=sum+probability[i];
			}
			
			for (int i=0;i<probability.length;i++) {
				probability[i]=probability[i]/sum;
				
			}
			
			return probability;
		}
		
	
		public double scoreFunction(int prevVmId, int newVmId) {
			double maxBw=((VirtualMachine) dcbLocal.vmlist.get(newVmId)).getCharacteristics().getBw();//returns maximum possible bandwidth for that vm
		
			double currentBw= ((VirtualMachine) dcbLocal.vmlist.get(newVmId)).getBw();//returns bandwidth in use for current vm
			
			return Math.pow(per[prevVmId][newVmId], a)  + (maxBw - currentBw/maxBw); 
		}
		public void UpdatePheromone(int prevId, int newId) {
			per[prevId][newId]=per[prevId][newId]+oup;
		}
		
		
		public double[][] UpdateGlobalPheromones() {
			return per;
		}
	}
}

