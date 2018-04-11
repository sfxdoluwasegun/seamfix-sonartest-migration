package com.sf.uservice.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.sf.uservice.beans.ApplicationBean;

/**
 * Handles communication with Amazon Web Service.
 * 
 * @author KOMOO
 */

@Stateless
public class AmazonManager {

	private final Logger log = Logger.getLogger(getClass());
	
	@Inject
	private ApplicationBean appBean;

	/**
	 * An Amazon wrapper library to upload data easily.
	 * 
	 * @param file
	 * @param key
	 * @param directory
	 */
	public void uploadFileSingleOperation(File file, String key) throws Exception {

		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(appBean.getAwskey(), appBean.getAwssecret());
		PutObjectRequest putObjectRequest = new PutObjectRequest(appBean.getAwsbucket(), key, file);

		AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
		try{
			amazonS3.putObject(putObjectRequest);
		} finally {
			
		}
	}
	
	/**
	 * An Amazon wrapper library to confirm the existence 
	 * of a previously uploaded data easily.
	 * 
	 * @param key
	 * @return true if exists
	 */
	public boolean fileExists(String key){
		boolean status = false;
		
		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(appBean.getAwskey(), appBean.getAwssecret());
		GetObjectRequest getObjectRequest = new GetObjectRequest(appBean.getAwsbucket(), key);

		AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
		
		try{
			S3Object s3Object = amazonS3.getObject(getObjectRequest);
			if(s3Object != null)
				status = true;
			
		}catch (AmazonS3Exception e){
			log.error("Error Message:" + e.getMessage());
			log.error("Error Code:" + e.getErrorCode());
			log.error("Error Type:" + e.getErrorType());
			status = false;
		}
		
		return status;
	}
	
	/**
	 * When you download an object, you get all of object's metadata and a stream from which to read the contents. 
	 * You should read the content of the stream as quickly as possible because the data is streamed directly from Amazon S3 
	 * and your network connection will remain open until you read all the data or close the input stream.
	 * 
	 * @param key
	 * @return bytes
	 */
	public byte[] fetchFileDataSingleOperation(String key){

		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(appBean.getAwskey(), appBean.getAwssecret());
		GetObjectRequest getObjectRequest = new GetObjectRequest(appBean.getAwsbucket(), key);

		AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);

		InputStream inputStream = null;

		try{
			S3Object s3Object = amazonS3.getObject(getObjectRequest);
			inputStream = s3Object.getObjectContent();

			try {
				return IOUtils.toByteArray(inputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
		}catch (AmazonS3Exception e){
			log.error("Error Message:" + e.getMessage());
			log.error("Error Code:" + e.getErrorCode());
			log.error("Error Type:" + e.getErrorType());
		}finally {
			IOUtils.closeQuietly(inputStream);
		}

		return ArrayUtils.EMPTY_BYTE_ARRAY;
	}

	/**
	 * Multipart upload allows you to upload a single object as a set of parts. 
	 * Each part is a contiguous portion of the object's data. You can upload these object parts independently and in any order. 
	 * If transmission of any part fails, you can retransmit that part without affecting other parts. 
	 * After all parts of your object are uploaded, Amazon S3 assembles these parts and creates the object. 
	 * In general, when your object size reaches 100 MB, you should consider using multipart uploads instead of uploading the object in a single operation.
	 * 
	 * @param file
	 * @param key
	 * @param directory
	 */
	public void uploadFileMultipartOperation(File file, 
			String key, String directory) throws Exception {

		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(appBean.getAwskey(), appBean.getAwssecret());

		TransferManager transferManager = new TransferManager(basicAWSCredentials);
		PutObjectRequest putObjectRequest = new PutObjectRequest(appBean.getAwsbucket(), key, file);

		try {
			transferManager.upload(putObjectRequest);
		} catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} finally {
			File parentDir = new File(directory);
			if (parentDir.exists())
				FileUtils.deleteQuietly(parentDir);
			else
				FileUtils.deleteQuietly(file);
		}
	}
	
	/**
	 * An Amazon wrapper library to delete previously uploaded data easily.
	 *  
	 * @param objectKey
	 */
	@Asynchronous
	public void deleteFileSingleOperation(String objectKey){
		
		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(appBean.getAwskey(), appBean.getAwssecret());
		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(appBean.getAwsbucket(), objectKey);
		
		AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
		
		try{
			amazonS3.deleteObject(deleteObjectRequest);
		}
		catch(AmazonServiceException ase){
			log.error("Caught an AmazonServiceException, delete object operation was rejected with an error response for some reason.");
			log.error("Error Message:    " + ase.getMessage());
			log.error("AWS Error Code:   " + ase.getErrorCode());
			log.error("Error Type:       " + ase.getErrorType());
		}
	}

}