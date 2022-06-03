package de.zenbit.aws;

import static de.zenbit.aws.Ackermann.ackermann;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AckermannS3Handler implements RequestHandler<S3Event, String> {

    private static final String INPUT_SUFFIX = ".json";
    private static final String INPUT_PREFIX = "input";
    private static final String OUTPUT_PREFIX = "output";
    
    final private AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    public String handleRequest(S3Event s3event, Context context) {

        final LambdaLogger logger = context.getLogger();
        
        logger.log("Event: " + new GsonBuilder().setPrettyPrinting().create().toJson(s3event));
        
        // Read S3 event
        final S3EventNotificationRecord record = s3event.getRecords().get(0);
        final String srcBucket = record.getS3().getBucket().getName();
        final String srcKey = record.getS3().getObject().getUrlDecodedKey();
        
        // Being cautious as we are writing in the same bucket we are being triggered from 
        if (!srcKey.startsWith(INPUT_PREFIX + "/") || !srcKey.endsWith(INPUT_SUFFIX)){
            throw new IllegalArgumentException("Circuit breaker. Check prefix/suffix in Lambda trigger!");
        }
        
        // Read computation request (JSON to POJO) using AmazonS3.getObject
        final S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
        final AckermannComputation computation = new Gson().fromJson(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8), AckermannComputation.class);
        
        // Compute Ackermann function
        long m = computation.getM();
        long n = computation.getN();
       
        logger.log(String.format("Computing Ackermann(%s, %s)", m, n));
        computation.setResult(ackermann(m, n));
        
        // Write computation result (POJO to JSON) using AmazonS3.putObject
        final String dstKey = srcKey.replaceFirst(INPUT_PREFIX, OUTPUT_PREFIX);
        final String result = new Gson().toJson(computation);
        logger.log(String.format("Writing to: %s/%s: %s", srcBucket, dstKey, result));
        
        s3Client.putObject(srcBucket, dstKey, result);
        
        return "OK";
    }
    
    
    
}
