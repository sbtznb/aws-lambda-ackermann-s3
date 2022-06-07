# aws-lambda-ackermann-s3
Stack-based AWS Lambda Ackermann function implementation that reads from and writes to AWS S3

## Set up Lambda Function 

1. In AWS, create an new Lambda function, setting runtime to Java. Assign a basic execution role that based on *AWSLambdaBasicExecutionRole* and *AmazonS3FullAccess* permissions. Edit the *Runtime Settings*and set *Handler* to `de.zenbit.aws.AckermannS3Handler`. 

2. Clone repository and build the deployment artifact (JAR) using *Maven*.

```bash
cd ./aws-lambda-ackermann-s3
mvn clean package
```

3. Deploy the JAR file to AWS Lambda using the AWS CLI. Of course, may also deploy the JAR file using the Lambda console In the Code Source pane, choose Upload from and then .zip file.

```bash
cd ./target
aws lambda update-function-code --function-name YOUR-FUNCTION-NAME --zip-file fileb://aws-lambda-ackermann-s3-0.0.1-SNAPSHOT.jar
```

4. Test your function using using the AWS CLI or the Lambda console using JSON input.

```bash
aws lambda invoke --function-name YOUR-FUNCTION-NAME --cli-binary-format raw-in-base64-out --payload '{"m":3,"n":2}' out.json
cat out.json
> 29
```

## Set up S3 Trigger

1. Create an S3 bucket

```bash
aws s3api create-bucket --bucket YOUR-BUCKET-NAME --region eu-central-1 --create-bucket-configuration LocationConstraint=eu-central-1
```

2. Add a trigger to your Lambda function
    * Type **S3 trigger**
    * Prefix **input/**
    * Suffix **.json**

::: warning
Be carefeful setting up the trigger to avoid recursive invocations between S3 and Lambda.
:::

## Final Test

Upload a file `ackermann-computation.json` into the *input*-folder in your S3-bucket, e.g.;

```json
{
  "m": 2,
  "n": 2
}
```

Your Lambda function should be triggered and process the input.

A new folder "output" will be created containing an output file with the same name as the input file. The output file contains the result of the computation:

```json
{
  "m": 2,
  "n": 2,
  "result": 7
}
```
