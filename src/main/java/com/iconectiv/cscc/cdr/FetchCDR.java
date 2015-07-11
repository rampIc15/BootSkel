package com.iconectiv.cscc.cdr;

import com.jcraft.jsch.*;

import java.net.CacheRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by rgovindprasad on 7/7/2015.
 */
public class FetchCDR {
    /*
            CDR path is of format :
            /cdr/cdr_shadow/CDR_njprdhub1_20150705003342_1.xfer_1436071024
     */
    public static final String CDR_PATH = "/cdr/cdr_shadow/CDR_njprdhub";

    private List<String> fileListPattern;

    private String server;

    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH");

    public FetchCDR(String server, String dateWithHr) {
        this.server = server;
        fileListPattern = createFileName(dateWithHr);
    }

    /*
    Given a date upto the the hour, get the files that are 1 hr before and
    1hr after.
    Assuming the date will be in the format :
        mm/dd/yy-hr
     */
    private List<String> createFileName(String dateInStr) {
        Date date;
        List<String> patternList = new ArrayList<>();
        try {
            date = dateFormat.parse(dateInStr);
        } catch (ParseException pe) {
            throw new RuntimeException("Invalid date : " + dateInStr);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int nowMonth = cal.get(Calendar.MONTH) + 1; //0 based
        int nowDay = cal.get(Calendar.DAY_OF_MONTH);
        int nowYear = cal.get(Calendar.YEAR);
        int nowHr = cal.get(Calendar.HOUR);
        //Now for given time
        patternList.add(CDR_PATH + "?" + "_" + nowYear + String.format("%02d", nowMonth) +
                String.format("%02d", nowDay) + String.format("%02d", nowHr) + "*");
        //1 hr back
        cal.add(Calendar.HOUR, -1);
        nowMonth = cal.get(Calendar.MONTH) + 1; //0 based
        nowDay = cal.get(Calendar.DAY_OF_MONTH);
        nowYear = cal.get(Calendar.YEAR);
        nowHr = cal.get(Calendar.HOUR);
        patternList.add(CDR_PATH + "?" + "_" + nowYear + String.format("%02d", nowMonth) +
                String.format("%02d", nowDay) + String.format("%02d", nowHr) + "*");
        //1 hr forward
        cal.add(Calendar.HOUR, 1);
        nowMonth = cal.get(Calendar.MONTH) + 1; //0 based
        nowDay = cal.get(Calendar.DAY_OF_MONTH);
        nowYear = cal.get(Calendar.YEAR);
        nowHr = cal.get(Calendar.HOUR);
        patternList.add(CDR_PATH + "?" + "_" + nowYear + String.format("%02d", nowMonth) +
                String.format("%02d", nowDay) + String.format("%02d", nowHr) + "*");

        return patternList;
    }

    private boolean getFile() {
        JSch jSch = new JSch();
        Session session = null;

        try {
            session = jSch.getSession("cscc", server, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("cscc");
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            for (String filePattern : fileListPattern) {
                System.out.print("Files in pattern : " + filePattern);
                List<ChannelSftp.LsEntry> fileList = sftpChannel.ls(filePattern);
                for (ChannelSftp.LsEntry fname : fileList) {
                    System.out.println("Fetching file : " + fname.getLongname());
                    sftpChannel.get("/cdr/cdr_shadow/" + fname.getFilename(), "workdir/" + fname.getFilename());
                }
            }
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException | SftpException excep) {
            excep.printStackTrace();
            System.out.println("Error : " + excep);
            return false;
        }
        return true;
    }

//    public static void main(String[] args) {
//
//        FetchCDR fetchCDR = new FetchCDR("10.107.1.52", "07/06/2015-11");
//        fetchCDR.getFile();
//
//    }
}