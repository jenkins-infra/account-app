package org.jenkinsci.account;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;

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
    private final String seats;
    private HashMap<String,String> results;
    private int seniority;
    private int totalVotes;

    public BoardElection(Application application, Parameters params) throws Exception {
        this.app = application;
        seniority = params.seniority();
        results = new HashMap();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        open = format.parse(params.electionOpen());
        close = format.parse(params.electionClose());
        candidates = params.electionCandidates().split(",");
        electionLogDir = params.electionLogDir();
        seats = params.seats();

        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        resultsDirectory = electionLogDir + "/" + date.format(DateTimeFormatter.BASIC_ISO_DATE);

        File dir = new File( resultsDirectory );
        dir.mkdirs();
    }

    public String anonymizeUser(String userid) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(userid.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest);
    }

    public HttpResponse doResults() throws NamingException, IOException {
        File[] elections = new File(electionLogDir).listFiles(File::isDirectory);
        return HttpResponses.forwardToView(this,"results.jelly").with("elections",elections);
    }

    public HttpResponse doResult(@QueryParameter String election) throws NamingException, IOException {
        if ( election.isEmpty() || election.equals(".") || election.equals("..")){
            return new HttpRedirect("/election/results");
        }

        if (isCurrentOpenedElection(election)){
            return new HttpRedirect("/election/ongoing");
        }

        File directory = new File( electionLogDir + "/" + election );

        if (!directory.isDirectory() || !directory.exists()){
            return new HttpRedirect("/election/results");
        }

        File[] votes = directory.listFiles();

        for (HashMap.Entry<String,String> entry: results.entrySet()){
            results.replace(entry.getKey(),"0");
        }


        String votesFilename =  electionLogDir + "/" + election + "/" + "votes.csv";
        // Clean existing votes.csv
        try{
            File collectedVotes = new File(votesFilename);
            if(! collectedVotes.delete()){
                // System.out.println("Delete operation on votes.csv failed.");
                System.out.format("Delete operation on %s failed.", votesFilename);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        int counter = 0;
        for (File vote : votes){
            if (vote.isFile() && !vote.getName().equals("votes.csv")) {
                FileReader fr = new FileReader(vote);
                BufferedReader br = new BufferedReader(fr);
                StringBuffer sb = new StringBuffer();
                String line;
                // We should never have more than one line in a vote file.
                while (( line = br.readLine()) != null ){
                    try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(votesFilename, true)))) {
                        out.println(line.toString());
                    }catch (IOException e) {
                        System.err.println(e);
                    }
                    counter++;
                }
                fr.close();
            }
        }
        totalVotes = counter;
        runSTV(votesFilename );
        return HttpResponses.forwardToView(this,"result.jelly").with("results",results);
    }

    private void runSTV(String votesFile) throws IOException {
        String[] cmd = {
                "python",
                "/opt/stv/stv.py",
                "-b", votesFile,
                "-s", seats,
        };
        results = new HashMap<String,String>();
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null){
            if (line.startsWith("(")){
                // Line example ('tom', 4, 52.0) where tom is the candidate, 4 is the round, and 52 is the score
                // Remove ( and ) from line on
                line = line.substring(1, line.length() - 1 );
                // Remove quote around candidate
                line = line.replace("'","");
                List<String> myList = new ArrayList<String>(Arrays.asList(line.split(",")));
                String candidate = myList.get(0);
                String score = String.valueOf(Math.round(Float.valueOf(myList.get(2))));
                results.put(candidate, score);
            }
        }
        br.close();
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
                String filename = String.format("%s/%s.csv", resultsDirectory.toString(), anonymizedUser);
                PrintWriter file = new PrintWriter(filename, "UTF-8");
                file.print(String.join(",",selected).concat("\n"));
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
    public int getTotalVotes(){ return totalVotes;}

    public String getSeniorityDate() {
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.minusMonths(seniority).format(DateTimeFormatter.ISO_DATE);
    }

    public String getElectionDate() {
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
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

    public boolean isCurrentOpenedElection(String election){
        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.BASIC_ISO_DATE).toString().equals(election);

    }

    public boolean isOpen() {
        final long now = System.currentTimeMillis();
        return open != null && close != null && open.getTime() <= now && close.getTime() >= now;
    }
}
