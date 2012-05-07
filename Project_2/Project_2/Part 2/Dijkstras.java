package graphAlgorithm;


import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Dijkstras {

  public static class TokenizerMapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text node = new Text();
    private Text cost = new Text();
    private Text destinations = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	Text tempDestinations = new Text();
        Text tempCost = new Text();
        int hops = 0;
      StringTokenizer iterator = new StringTokenizer(value.toString());
	  //here it is run till there are more tokens.
	  //Everytime the start position set as node
	  //Everytime the cost is set and the destination node
      while (iterator.hasMoreTokens()) {
      node.set(iterator.nextToken());
      cost.set(iterator.nextToken());
      if(iterator.hasMoreTokens())
      destinations.set(iterator.nextToken());
      }
      hops = Integer.parseInt(cost.toString())+1; // here the cost is incremented by 1 
      if(destinations!=null) {
      StringTokenizer iterator1 = new StringTokenizer(destinations.toString(),":");
      while (iterator1.hasMoreTokens()) {
    	  tempDestinations.set(iterator1.nextToken());
    	  tempCost.set(Integer.toString(hops));
        context.write(tempDestinations,tempCost);
      }
      }
      context.write(node,cost);
      context.write(node,destinations);
    }
  }
  
  // the reducer function basically gets the input and calculates the minimum distance between the given nodes
  //everytime an input is given where the default is set to 10000
  
  public static class IntSumReducer 
       extends Reducer<Text,Text,Text,Text> {
      private Text result = new Text();
	  String res = "";
	  String temp = "";
	  
	 //This function reduces and finds the minimum value everytime the input is given 
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
    	int minimumCost = 10000; //The minimum cost is set to 10000
      for (Text value : values) {
        if(value.toString().contains(":")) {
        	temp = value.toString();
        }
        else {
        	minimumCost = Math.min(Integer.parseInt(value.toString()), minimumCost); // minimum cost found
        }
      }
      res = minimumCost + " " + temp;
      result.set(res);
      context.write(key, result);
    }
  }
//This function defines the various jobs
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: Dijkstras <in> <out>");
      System.exit(2);
    }
    int converge = 0;
    String outputFile=otherArgs[0];
	//Here we are running the iterarion for a 15 iterations
	//Here everytime the out put is again given as the input to the new job till the minimum distance is found
    while(converge<15) {
    Job job = new Job(conf, "shortestPathDijkstras");// The job is created with the given name "shortestPathDijikstras"
    job.setJarByClass(Dijkstras.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(outputFile));
    outputFile = otherArgs[1] + converge;
    FileOutputFormat.setOutputPath(job, new Path(outputFile));
    converge++;
    job.waitForCompletion(true);
    }
  }
}
