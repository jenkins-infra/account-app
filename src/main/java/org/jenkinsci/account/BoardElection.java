package org.jenkinsci.account;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.naming.NamingException;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BoardElection {

    private final Date open;
    private final Date close;
    private final Application app;
    private final String[] candidates;
    private final String electionLogDir;
    private final String resultsDirectory;
    private final HashMap<String,Integer> results;
    private int seniority;
    public int total_votes;

    public BoardElection(Application application, Parameters params) throws Exception {
        this.app = application;
        seniority = params.seniority();
        results = new HashMap();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        open = format.parse(params.electionOpen());
        close = format.parse(params.electionClose());
        candidates = params.electionCandidates().split(",");
        electionLogDir = params.electionLogDir();

        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        resultsDirectory = electionLogDir + "/" + date.format(DateTimeFormatter.BASIC_ISO_DATE);

        File dir = new File( resultsDirectory );
        dir.mkdirs();
    }

    public String anonymizeUser(String userid) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(userid.getBytes());
        byte[] digest = md.digest();
        System.out.print(DatatypeConverter.printHexBinary(digest));
        return DatatypeConverter.printHexBinary(digest);
    }

    public HttpResponse doResults() throws NamingException, IOException {
        File[] elections = new File(electionLogDir).listFiles(File::isDirectory);
        return HttpResponses.forwardToView(this,"results.jelly").with("elections",elections);
    }

    public HttpResponse doResult(@QueryParameter String election) throws NamingException, IOException {
        // Election string content validation
        if ( election.isEmpty() || election.equals(".") || election.equals("..")){
            return new HttpRedirect("/election/results");
        }

        System.out.print(isCurrentOpenElection(election));

        if (isCurrentOpenElection(election)){
            return new HttpRedirect("/election/ongoing");
        }else{
            System.out.print("false");
        }


        File directory = new File( electionLogDir + "/" + election );

        if (!directory.isDirectory() || !directory.exists()){
            return new HttpRedirect("/election/results");
        }
        File[] votes = directory.listFiles();

        for (HashMap.Entry<String,Integer> entry: results.entrySet()){
            results.replace(entry.getKey(),0);
        }

        int counter = 0;

        for (File vote : votes){
            if (vote.isFile()) {
                FileReader fr = new FileReader(vote);
                BufferedReader br = new BufferedReader(fr);
                StringBuffer sb = new StringBuffer();
                String line;
                // Reset Counter
                int i = 0;
                while (( line = br.readLine()) != null && i < 3){
                    if ( results.containsKey(line.toString())){
                        int value = results.get(line) + 1;
                        results.replace(line.toString(), value);
                    }
                    else {
                        results.put(line.toString(), 1);
                    }
                    i++;
                    counter++;
                }
                fr.close();
            }
        }
        total_votes = counter;
        return HttpResponses.forwardToView(this,"result.jelly").with("results",results);
    }

    public HttpResponse doVote(@QueryParameter String vote) throws NamingException, IOException {

        if ( isSeniorMember()) {
            final Myself user = Myself.current();
            String anonymizedUser = null;
            if (user == null) {
                throw new UserError("Need to be authenticated");
            }

            try {
                anonymizedUser = anonymizeUser(user.userId);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if (!isOpen()) {
                throw new UserError("Election is closed");
            }

            List<String> selected = new ArrayList<>();

            for (String id : vote.split(",")) {
                selected.add(candidates[Integer.parseInt(id)]);
            }

            try {
                String filename = String.format("%s/%s", resultsDirectory.toString(), anonymizedUser);
                PrintWriter file = new PrintWriter(filename, "UTF-8");
                for (int i = 0; i < selected.size(); i++) {
                    file.println(selected.get(i));
                }
                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return new HttpRedirect("done");
        } else {
            return new HttpRedirect("seniority");
        }
    }

    public HttpResponse doSeniority() throws NamingException, IOException {
        return HttpResponses.forwardToView(this,"seniority.jelly");
    }

    public String[] getCandidates() {
        return candidates;
    }

    public int getSeniority() { return seniority; }

    public String getSeniorityDate() {
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.minusMonths(seniority).format(DateTimeFormatter.ISO_DATE);
    }

    public String getElectionDate() {
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public int convertPercent(int score,int totalVotes){
        return (int)(((float)score/(float)totalVotes) * 100);
    }

    public boolean isSeniorMember(){
        if ( app.getMyself().registrationDate != null ) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDate registrationDate = LocalDate.parse(app.getMyself().registrationDate,formatter);
            LocalDate election_date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return registrationDate.isBefore(election_date.minusMonths(seniority));
        }
        else {
            /*
            We assume that if a user doesn't have a registrationDate from ldap,
            it means that the user was created before this commit -> 8a9ac102
            */
            return true;
        }
    }

    public boolean isCurrentOpenElection(String election){
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.BASIC_ISO_DATE).toString().equals(election);

    }

    public boolean isOpen() {
        final long now = System.currentTimeMillis();
        return open != null && close != null && open.getTime() <= now && close.getTime() >= now;
    }

}
