package org.jenkinsci.account;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.naming.NamingException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final String log;

    public BoardElection(Application application, Parameters params) throws Exception {
        this.app = application;
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        open = format.parse(params.electionOpen());
        close = format.parse(params.electionClose());
        candidates = params.electionCandidates().split(",");
        log = params.electionLogDir() + "/" + params.electionClose().replaceAll("/","") + ".yaml";
        File f = new File(log);
        f.createNewFile();
    }

    @RequirePOST
    public HttpResponse doVote(@QueryParameter String vote) throws NamingException, IOException {

        final Myself user = Myself.current();
        if (user == null) {
            throw new UserError("Need to be authenticated");
        }

        if (!isOpen())  {
            throw new UserError("Election is closed");
        }

        List<String> selected = new ArrayList<>();
        // TODO check user is "old enough"
        for (String id : vote.split(",")) {
            selected.add(candidates[Integer.parseInt(id)]);
        }
        // TODO build a nicer yaml structure document
        YamlReader reader = new YamlReader(new FileReader(log));
        Object object = new Object();
        object = reader.read();
        if (null == object){
            String init_content = "election_close: " + close;
            YamlReader init = new YamlReader(init_content);
            object = init.read();
        }

        Map map = (Map)object;
        map.put(user.userId,StringUtils.join(selected, ","));

        YamlWriter writer = new YamlWriter(new FileWriter(log, false));
        writer.write(map);
        writer.close();

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
