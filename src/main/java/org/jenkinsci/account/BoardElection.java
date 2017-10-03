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

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BoardElection {

    private final Date open;
    private final Date close;
    private final Application app;
    private final String[] candidates;
    private final String log_dir;
    private final String election_dir;
    private final HashMap<String,Integer> results;
    public int total_votes;

    public BoardElection(Application application, Parameters params) throws Exception {
        this.app = application;
        results = new HashMap();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        open = format.parse(params.electionOpen());
        close = format.parse(params.electionClose());
        candidates = params.electionCandidates().split(",");
        log_dir = params.electionLogDir();

        LocalDate date = close.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        election_dir = log_dir + "/" + date.format(DateTimeFormatter.BASIC_ISO_DATE);

        File dir = new File( election_dir );
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
        File directory = new File( election_dir );
        File[] votes = directory.listFiles();

        for (HashMap.Entry<String,Integer> entry: results.entrySet()){
            results.replace(entry.getKey(),0);
        }

        int counter = 0;

        System.out.format("Total votes: %s", counter);

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
        return HttpResponses.forwardToView(this,"results.jelly").with("results",results);
    }

    public HttpResponse doVote(@QueryParameter String vote) throws NamingException, IOException {

        final Myself user = Myself.current();
        String anonymized_user = null;
        if (user == null) {
            throw new UserError("Need to be authenticated");
        }

        try {
            anonymized_user = anonymizeUser(user.userId);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        if (!isOpen()) {
            throw new UserError("Election is closed");
        }

        List<String> selected = new ArrayList<>();
        // TODO check user is "old enough"
        for (String id : vote.split(",")) {
            selected.add(candidates[Integer.parseInt(id)]);
        }
        /*
        // TODO build a nicer yaml structure document
        Object object = new Object();
        String init_content = "election_close: " + close.toString();
        YamlReader init = new YamlReader(init_content);
        object = init.read();


        Map map = (Map)object;
        // map.put(anonymized_user,StringUtils.join(selected, ","));
        map.put("vote", selected);

        // Write vote result to yaml file
        String log = String.format("%s/%s.yaml", election_dir.toString(), anonymized_user);

        YamlWriter writer = new YamlWriter(new FileWriter(log, false));
        writer.write(map);
        writer.close();
        */

        try {
            String filename = String.format("%s/%s", election_dir.toString(), anonymized_user);
            PrintWriter file = new PrintWriter( filename ,"UTF-8");
            for (int i = 0; i < selected.size(); i++){
                file.println(selected.get(i));
            }
            file.close();

        } catch (IOException e){
            e.printStackTrace();
        }

        // TODO store
        return new HttpRedirect("done");
    }

    public String[] getCandidates() {
        return candidates;
    }

    public boolean isOpen() {
        final long now = System.currentTimeMillis();
        return open != null && close != null && open.getTime() <= now && close.getTime() >= now;
    }

}
