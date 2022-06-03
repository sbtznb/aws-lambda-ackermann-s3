package de.zenbit.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.RequestParametersEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.ResponseElementsEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.UserIdentityEntity;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.GsonBuilder;

class AckermannS3HandlerTest {

    @Test
    void testHandleRequest() throws IOException {
        
        Context context = mock(Context.class);
        
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        
        String jsonContent = "{\"m\":3,\"n\":1}";
        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        S3ObjectInputStream s3ObjectInputStreamMock = new S3ObjectInputStream(inputStream, null);
        
        S3Object s3ObjectMock = mock(S3Object.class);
        when(s3ObjectMock.getObjectContent()).thenReturn(s3ObjectInputStreamMock);
        
        AmazonS3 s3ClientMock = mock(AmazonS3.class);
        when(s3ClientMock.getObject(Mockito.any(GetObjectRequest.class))).thenReturn(s3ObjectMock);
        
        mockStatic(AmazonS3ClientBuilder.class);
        when(AmazonS3ClientBuilder.defaultClient()).thenReturn(s3ClientMock);

        S3EventNotificationRecord record = new S3EventNotificationRecord("eu-central-1",
                "ObjectCreated:Put",
                "aws:s3",
                "2022-05-21T00:30:12.456Z",
                "2.1",
                new RequestParametersEntity("174.255.255.156"),
                new ResponseElementsEntity("nBbLJ4PAHhdvxmplPvtCgTrWCqf/KtonyV93l9rcoMLeIWJxpS9x9P8u01+Tj0OdbAoGs+VGvEvWl/Sg1NW5uEsVO25Laq7L", "AF2D7AB6002E898D"),
                new S3Entity("682bbb7a-xmpl-4843ca-94b1-7f77c4d6dbf0",
                 new S3BucketEntity("ackermann-test-bucket",
                   new UserIdentityEntity("BA3XMPLFAF2AI3E"),
                   "arn:aws:s3:::" + "ackermann-bucket"),
                 new S3ObjectEntity("input/ackermann-computation.json",
                   Long.valueOf(1),
                   "d132690b6c65b6d1629721dcfb49b883",
                   "1.0",
                   "005E64A65DF093B26D"),
                 "1.0"),
                new UserIdentityEntity("AWS:AIDAINPONIXMPLT3IKHL2"));
        
        List<S3EventNotificationRecord> records = new ArrayList<S3EventNotificationRecord>();
        records.add(record);
        S3Event event = new S3Event(records);
        
        AckermannS3Handler handler = new AckermannS3Handler();
        String result = handler.handleRequest(event, context);
        
        assertThat(result, is("OK"));
        
        // Verify computation input is read and result is written
        verify(s3ClientMock).getObject(new GetObjectRequest("ackermann-test-bucket", "input/ackermann-computation.json"));
        verify(s3ClientMock).putObject("ackermann-test-bucket", "output/ackermann-computation.json", "{\"m\":3,\"n\":1,\"result\":13}");
        
        // Verify logging
        verify(logger).log("Event: " + new GsonBuilder().setPrettyPrinting().create().toJson(event));
        verify(logger).log("Computing Ackermann(3, 1)");
        verify(logger).log("Writing to: ackermann-test-bucket/output/ackermann-computation.json: {\"m\":3,\"n\":1,\"result\":13}");
    }

}