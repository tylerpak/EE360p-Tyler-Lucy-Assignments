import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
        public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you mapper function
            ArrayList<String> tok = new ArrayList<>();
            String str = value.toString();
            str = str.toLowerCase();
            str = str.replaceAll("[^A-Za-z0-9]", " ");
            StringTokenizer itr = new StringTokenizer(str);
            while(itr.hasMoreTokens()) {
                tok.add(itr.nextToken());
            }
            for(int i = 0; i < tok.size(); i++) {
                for(int j = 0; j < tok.size(); j++) {
                    if(i != j) {
                        Tuple t = new Tuple(new Text(tok.get(j)), new IntWritable(1));
                        context.write(new Text(tok.get(i)), t);
                    }
                }
            }
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
        public void reduce(Text key, Iterable<Tuple> tuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you combiner function
            Map<String,Integer> tup = new HashMap<>();
            for(Tuple t: tuples) {
                String str = t.getQueryword().toString();
                if(tup.containsKey(str)) {
                    tup.put(str, 1 + tup.get(str));
                } else {
                    tup.put(str, 1);
                }
            }
            for(Map.Entry<String, Integer> entry: tup.entrySet()) {
                Tuple t = new Tuple(new Text(entry.getKey()), new IntWritable(entry.getValue()));
                context.write(key,t);
            }
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out each edge and its weight
	    Text value = new Text();
            for(String neighbor: map.keySet()){
                String weight = map.get(neighbor).toString();
                value.set(" " + neighbor + " " + weight);
                context.write(key, value);
            }
            //   Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = new Job(conf, "EID1_EID2"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        
	// set local combiner class
        job.setCombinerClass(TextCombiner.class);
	// set reducer class        
	job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //job.setMapOutputKeyClass(?.class);
        //job.setMapOutputValueClass(?.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    // You may define sub-classes here. Example:
    // public static class MyClass {
    //
    // }
}


