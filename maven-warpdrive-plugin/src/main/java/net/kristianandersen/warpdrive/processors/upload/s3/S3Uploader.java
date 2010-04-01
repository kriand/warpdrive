/*
   Copyright 2010 Kristian Andersen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.kristianandersen.warpdrive.processors.upload.s3;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multithread.S3ServiceSimpleMulti;
import org.jets3t.service.security.AWSCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 23, 2010
 * Time: 10:45:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class S3Uploader {

    private final WarpDriveMojo mojo;

    public S3Uploader(WarpDriveMojo mojo) {
        this.mojo = mojo;
    }

    public void uploadFiles(Set<File> files) throws Exception {

        Properties credentials = new Properties();
        credentials.load(new FileInputStream(mojo.s3Credentials));

        String accessKey = credentials.getProperty("accessKey");
        String secretKey = credentials.getProperty("secretKey");

        if (accessKey == null) {
            mojo.getLog().error("AccessKey could not be found in credentials file");
            return;
        }

        if (secretKey == null) {
            mojo.getLog().error("SecretKey could not be found in credentials file");
            return;
        }

        S3Service s3Service = createS3Service(accessKey, secretKey);
        S3Bucket s3Bucket = s3Service.getBucket(mojo.s3Bucket);
        grantReadAccessForAllUsersForBucket(s3Service, s3Bucket);
        S3Object[] s3Objects = createS3Objects(s3Bucket, files);
        S3ServiceSimpleMulti multithreadedService = createMultithreadedS3Service(s3Service);
        multithreadedService.putObjects(s3Bucket, s3Objects);
    }

    private S3Service createS3Service(String accessKey, String secretKey) throws S3ServiceException {
        AWSCredentials awsCredentials = new AWSCredentials(accessKey, secretKey);
        return new RestS3Service(awsCredentials); 
    }

    private S3ServiceSimpleMulti createMultithreadedS3Service(S3Service s3Service) {
        return new S3ServiceSimpleMulti(s3Service);
    }

    private void grantReadAccessForAllUsersForBucket(S3Service s3Service, S3Bucket s3Bucket) throws S3ServiceException {
        AccessControlList bucketAcl = s3Service.getBucketAcl(s3Bucket);
        bucketAcl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
        s3Bucket.setAcl(bucketAcl);
        s3Service.putBucketAcl(s3Bucket);
    }

    private S3Object[] createS3Objects(S3Bucket s3Bucket, Set<File> files) throws IOException, NoSuchAlgorithmException {

        List<S3Object> s3ObjectList = new ArrayList<S3Object>();

        Calendar expirationDate = getExpirationDate();

        for (File file : files) {
            if (!file.isDirectory()) {
                S3Object s3Object = createS3ObjectFromFile(file, s3Bucket, expirationDate);
                if (s3Object != null) {
                    s3ObjectList.add(s3Object);
                }
            }
        }       
        return (S3Object[]) s3ObjectList.toArray(new S3Object[s3ObjectList.size()]);
    }

    private S3Object createS3ObjectFromFile(File file, S3Bucket s3Bucket, Calendar expires) throws IOException, NoSuchAlgorithmException {

        String relativePath = getRelativePath(file);

        if (file.getName().toLowerCase().endsWith(".css")) {
            S3Object cssObject = getBasicS3Object(s3Bucket, relativePath, file, expires);
            cssObject.setContentType("text/css");
            if (file.getName().toLowerCase().endsWith(".gz.css")) {
                cssObject.setContentEncoding("gzip");
            }
            return cssObject;
        }

        if (file.getName().toLowerCase().endsWith(".js")) {
            S3Object jsObject = getBasicS3Object(s3Bucket, relativePath, file, expires);
            jsObject.setContentType("application/x-javascript");
            if (file.getName().toLowerCase().endsWith(".gz.js")) {
                jsObject.setContentEncoding("gzip");
            }
            return jsObject;
        }

        S3Object imgObject = null;
        for (String ext : new String[]{".gif", ".jpg", ".jpeg", ".png"}) {
            if (file.getName().toLowerCase().endsWith(ext)) {
                imgObject = getBasicS3Object(s3Bucket, relativePath, file, expires);
                imgObject.setContentType("image/" + ext.substring(1));
            }
        }
        return imgObject;
    }

    private String getRelativePath(File file) {
        return file.getPath().substring(mojo.webappTargetDir.length() + 1);
    }

    private S3Object getBasicS3Object(S3Bucket s3Bucket, String relativePath, File file, Calendar expires) throws IOException, NoSuchAlgorithmException {
        S3Object s3Object = new S3Object(file);
        s3Object.setAcl(s3Bucket.getAcl());
        s3Object.setKey(relativePath);
        s3Object.addMetadata("Expires", htmlExpiresDateFormat().format(expires.getTime()));
        s3Object.addMetadata("Cache-Control", "public, max-age=" + 946080000000L + ";public;must-revalidate;");
        return s3Object;
    }

    private DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }

    private Calendar getExpirationDate() {
        Calendar expiration = GregorianCalendar.getInstance();
        expiration.add(Calendar.YEAR, 30);
        return expiration;
    }

}
