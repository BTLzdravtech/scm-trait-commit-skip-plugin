package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import javax.annotation.Nonnull;

import jenkins.scm.api.SCMHead;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMRevision;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMRevision;

import hudson.Extension;
import hudson.Util;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

public class BitbucketCommitAuthorBranchBuildStrategy extends CommitAuthorBranchBuildStrategy {

    @DataBoundConstructor
    public BitbucketCommitAuthorBranchBuildStrategy(String pattern) {
        super(pattern);
    }

    @Override
    public String getAuthor(SCMSource source, SCMRevision currRevision) throws CouldNotGetCommitDataException {
        SCMRevision revision = currRevision;
        if (currRevision instanceof PullRequestSCMRevision) {
            PullRequestSCMRevision<?> pr = (PullRequestSCMRevision<?>) currRevision;
            revision = pr.getPull();
        }
        if (revision instanceof BitbucketGitSCMRevision) {
            BitbucketGitSCMRevision bbRevision = (BitbucketGitSCMRevision) revision;
            return Util.fixEmpty(bbRevision.getAuthor());
        }

        throw new CouldNotGetCommitDataException("Revision class is not a BitbucketGitSCMRevision");
    }

    @Extension
    public static class DescriptorImpl extends RegexFilterBranchBuildStrategyDescriptor {

        @Override
        @Nonnull
        public String getDisplayName() {
            return CommitAuthorBranchBuildStrategy.getDisplayName();
        }

        /**
         * {@inheritDoc}
         * this is currently never called for organization folders, see JENKINS-54468
         */
        @Override
        public boolean isApplicable(@Nonnull SCMSourceDescriptor sourceDescriptor) {
            return BitbucketSCMSource.DescriptorImpl.class.isAssignableFrom(sourceDescriptor.getClass());
        }
    }
}
