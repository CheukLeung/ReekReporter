/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cheuk.reekreporter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;
import cheuk.reekreporter.resource.ReekReport;
import cheuk.reekreporter.resource.SourceFile;
import hudson.FilePath;
import hudson.model.Action;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author cheuk
 */
public class ReekPublisher extends Publisher{
    
    public String reekDir;
    HashMap<String, SourceFile> sourceFileHash;
    
    @DataBoundConstructor
    public ReekPublisher(String reekDir) {
        this.reekDir = reekDir;
    }
    
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    

    @Override
    @SuppressWarnings("null")
    public boolean perform (AbstractBuild build, Launcher launcher, BuildListener listener){
        listener.getLogger().println("Running Reek result reporter");
        List<ReekReport> reekReports = ReekReportParser.doParse(build.getWorkspace().child(reekDir));
        listener.getLogger().println("" + ReekReport.getSize() + " warning(s) are found.");
        
        sourceFileHash = new HashMap<String, SourceFile>();
        setSourceFileHash(build, reekReports);
        
        final ReekResult result = new ReekResult(build, reekReports, sourceFileHash);
        final ReekBuildAction action = ReekBuildAction.load(build, result);
        build.getActions().add(action);
        
        return true;
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project){
        return new ReekProjectAction(project);
    }
    
    @Override
    public ReekPublisher.DescriptorImpl getDescriptor(){
        return (ReekPublisher.DescriptorImpl)super.getDescriptor();
    }

    private void setSourceFileHash(AbstractBuild<?, ?> build, List<ReekReport> reekReports) {
        Iterator<ReekReport> it = reekReports.iterator();
        while (it.hasNext()){
            ReekReport report = it.next();
            SourceFile sourceFile;
            String fileName = report.getSource();
            if (sourceFileHash.containsKey(fileName)){
                sourceFile = sourceFileHash.get(fileName);
            }
            else {
                sourceFile = new SourceFile(build, fileName);
                sourceFileHash.put(fileName, sourceFile);
            }
            sourceFile.addReport(report);
            
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type){
            return true;
        }
        
        @Override
        public String getDisplayName(){
            return "Reek result reporter";
        }
    }
}
