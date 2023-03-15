package org.jenkinsci.plugins.scm_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource;
import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSourceContext;
import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSourceRequest;
import io.jenkins.plugins.gitlabbranchsource.MergeRequestSCMHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.impl.trait.Selection;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * @author witokondoria
 */
public class GitLabCommitSkipTrait extends CommitSkipTrait {

    /**
     * Constructor for stapler.
     */
    @DataBoundConstructor
    public GitLabCommitSkipTrait() {
        super();
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withFilter(new ExcludeCommitPRsSCMHeadFilter());
    }

    /**
     * Our descriptor.
     */
    @Extension
    @Selection
    @Symbol("gitHubCommitSkipTrait")
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends CommitSkipTraitDescriptorImpl {

        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitLabSCMSourceContext.class;
        }

        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GitLabSCMSource.class;
        }
    }

    /**
     * Filter that excludes pull requests according to its last commit message (if it contains [ci skip] or [skip ci], case insensitive).
     */
    private static class ExcludeCommitPRsSCMHeadFilter extends ExcludeByMessageSCMHeadFilter {

        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException {
            if (scmHead instanceof MergeRequestSCMHead) {
                Iterable<MergeRequest> mergeRequests = ((GitLabSCMSourceRequest) scmSourceRequest).getMergeRequests();
                for (MergeRequest mergeRequest : mergeRequests) {
                    if (("MR-" + mergeRequest.getIid()).equals(scmHead.getName())) {
                        Project gitLabProject = ((GitLabSCMSourceRequest) scmSourceRequest).getGitlabProject();
                        String message;
                        GitLabApi gitLabApi = ((GitLabSCMSourceRequest) scmSourceRequest).getGitLabApi();
                        if (gitLabApi != null) {
                            try {
                                message = gitLabApi.getMergeRequestApi().getCommits(gitLabProject, mergeRequest.getIid().intValue()).get(0).getMessage();
                            } catch (GitLabApiException e) {
                                throw new IOException("Failed to retrieve commit message for " + mergeRequest.getTitle(), e);
                            }
                            return super.containsSkipToken(message.toLowerCase());
                        }
                    }
                }
            }
            return false;
        }
    }
}
