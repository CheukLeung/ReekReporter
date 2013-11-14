/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cheuk.reekreporter;

import cheuk.reekreporter.resource.ReekReport;
import cheuk.reekreporter.resource.SourceFile;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.util.ChartUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author cheuk
 */
public class ReekResult implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private AbstractBuild<?, ?> owner;
    private List<ReekReport> reekReports;
    private HashMap<String, SourceFile> sourceFileHash;
            
    public ReekResult(AbstractBuild <?, ?> owner, List<ReekReport> reekReports, HashMap<String, SourceFile> sourceFileHash){
        this.owner = owner;
        this.reekReports = reekReports;
        this.sourceFileHash = sourceFileHash;
    }
    
    public ReekResult getPreviousresult(){
        ReekBuildAction previousAction = getPreviousAction();
        ReekResult previousResult = null;
        if (previousAction != null){
            previousResult = previousAction.getResult();
        }
        
        return previousResult;
    }

    private ReekBuildAction getPreviousAction() {
        AbstractBuild<?, ?> previousBuild = owner.getPreviousBuild();
        if (previousBuild != null){
            return previousBuild.getAction(ReekBuildAction.class);
        }
        return null;
    }
    
    public AbstractBuild<?, ?> getOwner(){
        return owner;
    }
    
    public List<ReekReport> getReekReports(){
        return reekReports;
    }
    
    public HashMap<String, SourceFile> getSourceFileHash(){
        return sourceFileHash;
    }
    
    @SuppressWarnings("unused")
    public Object getDynamic(final String link, final StaplerRequest request,
                            final StaplerResponse response) throws IOException{
        String linkModified = link.replaceAll("=", "/");

        if (linkModified.startsWith("source.")){
            if (!owner.getProject().getACL().hasPermission(Item.WORKSPACE)){
                response.sendRedirect2("nosourcepermission");
                return null;
            }
            if (sourceFileHash.containsKey(linkModified.replaceFirst("source.", ""))){
                return sourceFileHash.get(linkModified.replaceFirst("source.", ""));
            }
        }
        return null;
    }
    
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null){
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }
        
        AbstractBuild<?, ?> build = getOwner();
        Calendar timestamp = build.getTimestamp();
        
        if (req.checkIfModified(timestamp, rsp)){
            return;
        }
        
        ReekBuildAction buildAction = owner.getAction(ReekBuildAction.class);
        if (buildAction != null){
            buildAction.doGraph(req, rsp);
        }
    }
}
