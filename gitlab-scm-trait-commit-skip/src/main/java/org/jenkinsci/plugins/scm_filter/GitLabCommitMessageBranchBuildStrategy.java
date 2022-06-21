package org.jenkinsci.plugins.scm_filter;

import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.gitlab4j.api.models.Commit;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

public class GitLabCommitMessageBranchBuildStrategy extends CommitMessageBranchBuildStrategy {

    @DataBoundConstructor
    public GitLabCommitMessageBranchBuildStrategy(@CheckForNull String pattern) {
        super(pattern);
    }

    @Override
    @CheckForNull
    public String getMessage(SCMSource source, SCMRevision revision) throws CouldNotGetCommitDataException {
        Commit commit = GitLabUtils.getCommit(source, revision);
        return commit.getMessage();
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
            return GitLabSCMSource.DescriptorImpl.class.isAssignableFrom(sourceDescriptor.getClass());
        }
    }
}
