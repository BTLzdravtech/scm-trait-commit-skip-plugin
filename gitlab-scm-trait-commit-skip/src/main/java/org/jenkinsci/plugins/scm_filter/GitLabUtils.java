package org.jenkinsci.plugins.scm_filter;

import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource;
import io.jenkins.plugins.gitlabbranchsource.MergeRequestSCMRevision;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import static io.jenkins.plugins.gitlabbranchsource.helpers.GitLabHelper.apiBuilder;

public final class GitLabUtils {

	private GitLabUtils() {
		// utils class, no instances
	}

	public static Commit getCommit(SCMSource source, SCMRevision revision) throws CouldNotGetCommitDataException {
		String hash = null;
		if (AbstractGitSCMSource.SCMRevisionImpl.class.isAssignableFrom(revision.getClass())){
			hash = ((AbstractGitSCMSource.SCMRevisionImpl) revision).getHash();
		}
		if (MergeRequestSCMRevision.class.isAssignableFrom(revision.getClass())){
			hash = ((MergeRequestSCMRevision) revision).getHeadHash();
		}
		if (hash == null) {
			throw new CouldNotGetCommitDataException("Unknown revision class ["+revision.getClass()+"] or null hash");
		}

		if (!GitLabSCMSource.class.isAssignableFrom(source.getClass())){
			throw new IllegalArgumentException("SCM Source ["+source.getClass()+"] is not a GitLabSCMSource ");
		}
		GitLabSCMSource glSource = (GitLabSCMSource) source;

		try (GitLabApi gitLabApi = apiBuilder(glSource.getOwner(), glSource.getServerName())) {
			return gitLabApi.getCommitsApi().getCommit(glSource.getProjectPath(), hash);
		} catch (GitLabApiException e) {
			throw new CouldNotGetCommitDataException(e);
		}
	}
}
