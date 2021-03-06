/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Search {

/*******************************************************************************
*                           INSTANCE VARIABLES                                 *
*******************************************************************************/

/*******************************************************************************
*                           STATIC VARIABLES                                   *
*******************************************************************************/

	public static FitnessFunction problem;

	public static Chromo[] member;
	public static Chromo[] child;

	public static Chromo bestOfGenChromo;
	public static double bestOfGenR;
	public static double bestOfGenG;
	public static Chromo bestOfRunChromo;
	public static double bestOfRunR;
	public static double bestOfRunG;
	public static Chromo bestOverAllChromo;
	public static double bestOverAllR;
	public static double bestOverAllG;

	public static double sumRawFitness;
	public static double sumRawFitness2;	// sum of squares of fitness
	public static double sumNoveltyFitness;
	public static double sumNoveltyFitness2;
	public static double sumSclFitness;
	public static double sumProFitness;
	public static double defaultBest;
	public static double defaultWorst;

	public static double averageRawFitness;
	public static double averageNoveltyFitness;
	public static double stdevRawFitness;
	public static double stdevNoveltyFitness;

	public static int G;
	public static int R;
	public static Random r = new Random();
	private static double randnum;

	private static int memberIndex[];
	private static double memberFitness[];
	private static int TmemberIndex;
	private static double TmemberFitness;

	private static double fitnessStats[][];  // 0=Avg, 1=Best

	public static boolean trans;
	public static boolean osc;

/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/


/*******************************************************************************
*                             MEMBER METHODS                                   *
*******************************************************************************/


/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

	public static void main(String[] args) throws java.io.IOException{

		Calendar dateAndTime = Calendar.getInstance();
		Date startTime = dateAndTime.getTime();

	//  Read Parameter File
		System.out.println("\nParameter File Name is: " + args[0] + "\n");
		Parameters parmValues = new Parameters(args[0]);

	//  Write Parameters To Summary Output File
		String summaryFileName = Parameters.expID + "_summary.txt";
		FileWriter summaryOutput = new FileWriter(summaryFileName);
		parmValues.outputParameters(summaryOutput);

	//	Set up Fitness Statistics matrix
		fitnessStats = new double[2][Parameters.generations];
		for (int i=0; i<Parameters.generations; i++){
			fitnessStats[0][i] = 0;
			fitnessStats[1][i] = 0;
		}

	//	Problem Specific Setup - For new new fitness function problems, create
	//	the appropriate class file (extending FitnessFunction.java) and add
	//	an else_if block below to instantiate the problem.

		if (Parameters.problemType.equals("NM")){
				problem = new NumberMatch();
		}
		else if (Parameters.problemType.equals("OM")){
				problem = new OneMax();
		}
		else if (Parameters.problemType.equals("DL1")){
				problem = new DynamicLandscapes1();
		}
		else System.out.println("Invalid Problem Type");

		System.out.println(problem.name);

	//	Initialize RNG, array sizes and other objects
		r.setSeed(Parameters.seed);
		memberIndex = new int[Parameters.popSize];
		memberFitness = new double[Parameters.popSize];
		member = new Chromo[Parameters.popSize];
		child = new Chromo[Parameters.popSize];
		bestOfGenChromo = new Chromo();
		bestOfRunChromo = new Chromo();
		bestOverAllChromo = new Chromo();

		if (Parameters.minORmax.equals("max")){
			defaultBest = 0;
			defaultWorst = 999999999999999999999.0;
		}
		else{
			defaultBest = 999999999999999999999.0;
			defaultWorst = 0;
		}

		bestOverAllChromo.rawFitness = defaultBest;

		//  Start program for multiple runs
		for (R = 1; R <= Parameters.numRuns; R++){

			bestOfRunChromo.rawFitness = defaultBest;
			System.out.println();

			//	Initialize First Generation
			for (int i=0; i<Parameters.popSize; i++){
				member[i] = new Chromo();
				child[i] = new Chromo();
			}

			KModes kmodes = new KModes();

			//	Begin Each Run
			for (G=0; G<Parameters.generations; G++){

				sumProFitness = 0;
				sumSclFitness = 0;
				sumRawFitness = 0;
				sumNoveltyFitness = 0;
				sumRawFitness2 = 0;
				sumNoveltyFitness2 = 0;
				bestOfGenChromo.rawFitness = defaultBest;
                bestOfGenChromo.noveltyFitness = 0;

                //Parameters.changeMutationType(G);

				//	Test Fitness of Each Member

				// Do dynamics
				if(Parameters.dynamics == 0) // Do nothing
				{
					// Do nothing
				}
				else if(Parameters.dynamics == 1) // Translate
				{
					if(G % 5 == 0)
						trans = true;
					else
						trans = false;
				}
				else if(Parameters.dynamics == 2) // Oscellate
				{
					if(G % 20 == 0)
						osc = true;
					else
						osc = false;
				}
				else if(Parameters.dynamics == 3) // Translate and oscellate
				{
					if(G % 20 == 0)
						osc = true;
					else
						osc = false;

					if(G % 5 == 0)
						trans = true;
					else
						trans = false;
				}

				for (int i=0; i<Parameters.popSize; i++){

					member[i].rawFitness = 0;
					member[i].sclFitness = 0;
					member[i].proFitness = 0;

					problem.doRawFitness(member[i], G);

					sumRawFitness = sumRawFitness + member[i].rawFitness;
					sumRawFitness2 = sumRawFitness2 +
						member[i].rawFitness * member[i].rawFitness;

					if (Parameters.minORmax.equals("max")){
						if (member[i].rawFitness > bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness > bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness > bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
					else {
						if (member[i].rawFitness < bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness < bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness < bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
				}

				// Test Novelty Score of each individual
				for(int i=0; i<Parameters.popSize; i++){
					problem.assessNovelty(member[i]);
					sumNoveltyFitness += member[i].noveltyFitness;
					sumNoveltyFitness2 = sumNoveltyFitness2 +
						member[i].noveltyFitness * member[i].noveltyFitness;
				}

				// Accumulate fitness statistics
				fitnessStats[0][G] += sumRawFitness / Parameters.popSize;
				fitnessStats[1][G] += bestOfGenChromo.rawFitness;

				averageRawFitness = sumRawFitness / Parameters.popSize;
				averageNoveltyFitness = sumNoveltyFitness / Parameters.popSize;
				stdevRawFitness = Math.sqrt(
							Math.abs(sumRawFitness2 -
							sumRawFitness*sumRawFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							);

				stdevNoveltyFitness = Math.sqrt(
							Math.abs(sumNoveltyFitness2 -
							sumNoveltyFitness*sumNoveltyFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							);

				// Output generation statistics to screen
				System.out.println(R + "\t" + G +  "\t" + bestOfGenChromo.rawFitness + "\t" + averageRawFitness + "\t"
				+ stdevRawFitness + "\t\t" + bestOfGenChromo.noveltyFitness + "\t" + averageNoveltyFitness + "\t" + stdevNoveltyFitness);

				// Output generation statistics to summary file
				summaryOutput.write(" R ");
				Hwrite.right(R, 3, summaryOutput);
				summaryOutput.write(" G ");
				Hwrite.right(G, 3, summaryOutput);
				Hwrite.right(bestOfGenChromo.rawFitness, 11, 3, summaryOutput);
				Hwrite.right(averageRawFitness, 11, 3, summaryOutput);
				Hwrite.right(stdevRawFitness, 11, 3, summaryOutput);
				Hwrite.right((int)bestOfGenChromo.noveltyFitness, 7, summaryOutput);
				Hwrite.right(averageNoveltyFitness, 11, 3, summaryOutput);
				Hwrite.right(stdevNoveltyFitness, 11, 3, summaryOutput);
				summaryOutput.write("\n");


		// *********************************************************************
		// **************** SCALE FITNESS OF EACH MEMBER AND SUM ***************
		// *********************************************************************

				switch(Parameters.scaleType){

				case 0:     // No change to raw fitness
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = member[i].rawFitness + .000001;
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 1:     // Fitness not scaled.  Only inverted.
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = 1/(member[i].rawFitness + .000001);
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 2:     // Fitness scaled by Rank (Maximizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=Parameters.popSize-1; i>0; i--){
						for (int j=0; j<i; j++){
							if (memberFitness[j] > memberFitness[j+1]){
								TmemberIndex = memberIndex[j];
								TmemberFitness = memberFitness[j];
								memberIndex[j] = memberIndex[j+1];
								memberFitness[j] = memberFitness[j+1];
								memberIndex[j+1] = TmemberIndex;
								memberFitness[j+1] = TmemberFitness;
							}
						}
					}
					//  Copy ordered array to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				case 3:     // Fitness scaled by Rank (minimizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=1; i<Parameters.popSize; i++){
						for (int j=(Parameters.popSize - 1); j>=i; j--){
							if (memberFitness[j-i] < memberFitness[j]){
								TmemberIndex = memberIndex[j-1];
								TmemberFitness = memberFitness[j-1];
								memberIndex[j-1] = memberIndex[j];
								memberFitness[j-1] = memberFitness[j];
								memberIndex[j] = TmemberIndex;
								memberFitness[j] = TmemberFitness;
							}
						}
					}
					//  Copy array order to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				default:
					System.out.println("ERROR - No scaling method selected");
				}


		// *********************************************************************
		// ****** PROPORTIONALIZE SCALED FITNESS FOR EACH MEMBER AND SUM *******
		// *********************************************************************

				for (int i=0; i<Parameters.popSize; i++){
					member[i].proFitness = member[i].sclFitness/sumSclFitness;
					sumProFitness = sumProFitness + member[i].proFitness;
				}

		// *********************************************************************
		// ************ CROSSOVER AND CREATE NEXT GENERATION *******************
		// *********************************************************************

				Chromo parent1;
				Chromo parent2;

				//  Assumes always two offspring per mating
				for (int i=0; i<Parameters.popSize; i=i+2){

					//	Select Two Parents

					// Get the list of species
					List species = new ArrayList(KModes.speciesList.keySet());
					boolean [] checkedSpecies = new boolean[Parameters.species];
					// Get randum species
					int species1 = r.nextInt(species.size());

					while(KModes.speciesList.get(species.get(species1)).size() == 0) // Prevent grabbing of empty species
					{
						species1 = r.nextInt(species.size());
					}
					checkedSpecies[species1] = false;

					parent1 = Chromo.selectParent(KModes.speciesList.get(species.get(species1))); // Run tourney to find members in species

					if(r.nextDouble() < Parameters.crossbreedRate || KModes.speciesList.get(species.get(species1)).size() == 1) // Crossbreed with different species if under cbrate or only 1 member in species
					{
						int species2 = r.nextInt(species.size());
						while(KModes.speciesList.get(species.get(species2)).size() == 0 || species1 == species2) // Prevent grabbing of empty species or previously used species
						{
							species2 = r.nextInt(species.size());

							// Ensure that there is an available species, if not use species1
							checkedSpecies[species2] = true;
							int quickCount = 0;
							for(int k = 0; k < checkedSpecies.length; k++)
							{
								if(checkedSpecies[k])
									quickCount++;
							}
							if(quickCount == (species.size() - 1))
							{
								species2 = species1;
								break;
							}
						}
						parent2 = Chromo.selectParent(KModes.speciesList.get(species.get(species2)));
					}
					else
					{
						parent2 = parent1;
						while (parent2 == parent1){
							parent2 = Chromo.selectParent(KModes.speciesList.get(species.get(species1)));
						}
					}

					//System.out.println("\nSpecies1 : " + parent1.speciesKey + "\nSpecies2 : " + parent2.speciesKey);

					//	Crossover Two Parents to Create Two Children
					randnum = r.nextDouble();
					if (randnum < Parameters.xoverRate){
						Chromo.mateParents(parent1, parent2, child[i], child[i+1]);
					}
					else {
						Chromo.mateParents(parent1, child[i]);
						Chromo.mateParents(parent2, child[i+1]);
					}
				} // End Crossover

				//	Mutate Children
				for (int i=0; i<Parameters.popSize; i++){
					child[i].doMutation();
				}

				//	Swap Children with Last Generation
				for (int i=0; i<Parameters.popSize; i++){
					Chromo.copyB2A(member[i], child[i]);
				}

				KModes.speciate();

			} //  Repeat the above loop for each generation

			Hwrite.left(bestOfRunR, 4, summaryOutput);
			Hwrite.right(bestOfRunG, 4, summaryOutput);

			problem.doPrintGenes(bestOfRunChromo, summaryOutput);

			System.out.println(R + "\t" + "B" + "\t"+ (int)bestOfRunChromo.rawFitness);

		} //End of a Run

		Hwrite.left("B", 8, summaryOutput);

		problem.doPrintGenes(bestOverAllChromo, summaryOutput);

		//	Output Fitness Statistics matrix
		summaryOutput.write("Gen                 AvgFit              BestFit \n");
		for (int i=0; i<Parameters.generations; i++){
			Hwrite.left(i, 15, summaryOutput);
			Hwrite.left(fitnessStats[0][i]/Parameters.numRuns, 20, 2, summaryOutput);
			Hwrite.left(fitnessStats[1][i]/Parameters.numRuns, 20, 2, summaryOutput);
			summaryOutput.write("\n");
		}

		summaryOutput.write("\n");
		summaryOutput.close();

		System.out.println();
		System.out.println("Start:  " + startTime);
		dateAndTime = Calendar.getInstance();
		Date endTime = dateAndTime.getTime();
		System.out.println("End  :  " + endTime);

	} // End of Main Class

}   // End of Search.Java ******************************************************
