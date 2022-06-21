package org.jenkinsci.plugins.scm_filter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jenkinsci.plugin.gitea.GiteaSCMSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitDetail;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

public class GiteaCommitMessageBranchBuildStrategy extends CommitMessageBranchBuildStrategy {

    @DataBoundConstructor
    public GiteaCommitMessageBranchBuildStrategy(@CheckForNull String pattern) {
        super(pattern);
    }

    @Override
    @CheckForNull
    public String getMessage(SCMSource source, SCMRevision revision) throws CouldNotGetCommitDataException {
        GiteaCommitDetail commit = GiteaUtils.getCommit(source, revision);
        return commit.getCommit().getMessage();
    }

    @Extension
    public static class DescriptorImpl extends RegexFilterBranchBuildStrategyDescriptor {

        @Override
        @Nonnull
        public String getDisplayName() {
            return CommitMessageBranchBuildStrategy.getDisplayName();
        }

        /**
         * {@inheritDoc}
         * this is currently never called for organization folders, see JENKINS-54468
         */
        @Override
        public boolean isApplicable(@Nonnull SCMSourceDescriptor sourceDescriptor) {
            return GiteaSCMSource.DescriptorImpl.class.isAssignableFrom(sourceDescriptor.getClass());
        }
    }
}
